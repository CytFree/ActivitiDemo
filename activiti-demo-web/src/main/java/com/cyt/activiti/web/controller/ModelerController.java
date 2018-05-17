package com.cyt.activiti.web.controller;

/**
 * @author CaoYangTao
 * @date 2018/5/10  20:28
 */

import com.alibaba.fastjson.JSONObject;
import com.cyt.activiti.common.utils.UserUtil;
import com.cyt.activiti.common.utils.WebResponseUtil;
import com.cyt.activiti.common.vo.ModelVo;
import com.cyt.activiti.facade.ActivitiDemoFacade;
import com.cyt.activiti.facade.enums.ResponseCode;
import com.cyt.activiti.facade.request.ActivitiDemoRequest;
import com.cyt.activiti.facade.response.BaseResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/models/")
public class ModelerController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ModelerController.class);

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    ObjectMapper objectmapper;

    @Autowired
    private ActivitiDemoFacade activitiDemoFacade;

    /**
     * Model列表
     */
    @RequestMapping(value = "modelList.htm", method = {RequestMethod.POST, GET})
    public String runSignProcessList(ModelMap modelMap) {
        try {
            List<Model> list = repositoryService.createModelQuery().list();
            modelMap.put(ROW_LIST, list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "model/modelList";
    }

    /**
     * 新建一个空模型
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "newModel.htm", method = POST)
    public void newModel(HttpServletResponse response, @ModelAttribute("modelVo")ModelVo modelVo)
            throws UnsupportedEncodingException {
        JSONObject jsonObject;
        //初始化一个空模型
        try {
            Model model = repositoryService.newModel();

            int revision = 1;
            ObjectNode modelNode = objectmapper.createObjectNode();
            modelNode.put(ModelDataJsonConstants.MODEL_NAME, modelVo.getModelName());
            modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, modelVo.getModelDescription());
            modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);

            model.setName(modelVo.getModelName());
            model.setKey(modelVo.getModelKey());
            model.setMetaInfo(modelNode.toString());

            repositoryService.saveModel(model);
            String id = model.getId();

            //完善ModelEditorSource
            ObjectNode editorNode = objectmapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectmapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            repositoryService.addModelEditorSource(id, editorNode.toString().getBytes("utf-8"));

            jsonObject = WebResponseUtil.success();
        } catch (Exception e) {
            jsonObject = WebResponseUtil.error();
            e.printStackTrace();
        }
        responseJson(response, jsonObject);
    }

    /**
     * 根据Model部署流程
     */
    @RequestMapping(value = "deploy/{modelId}.htm")
    public String deploy(@PathVariable("modelId") String modelId, RedirectAttributes redirectAttributes) {
        try {
            Model modelData = repositoryService.getModel(modelId);
            ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
            byte[] bpmnBytes = null;

            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            bpmnBytes = new BpmnXMLConverter().convertToXML(model);

            String processName = modelData.getName() + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment().name(modelData.getName()).addString(processName, new String(bpmnBytes)).deploy();
            redirectAttributes.addFlashAttribute("message", "部署成功，部署ID=" + deployment.getId());
        } catch (Exception e) {
            logger.error("根据模型部署流程失败：modelId={}", modelId, e);
        }
        return "redirect:/models/modelList.htm";
    }

    /**
     * 导出model对象为指定类型
     *
     * @param modelId 模型ID
     * @param type    导出文件类型(bpmn\json)
     */
    @RequestMapping(value = "export/{modelId}/{type}.htm")
    public void export(@PathVariable("modelId") String modelId,
                       @PathVariable("type") String type,
                       HttpServletResponse response) {
        try {
            Model modelData = repositoryService.getModel(modelId);
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            byte[] modelEditorSource = repositoryService.getModelEditorSource(modelData.getId());

            JsonNode editorNode = new ObjectMapper().readTree(modelEditorSource);
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);

            // 处理异常
            if (bpmnModel.getMainProcess() == null) {
                response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
                response.getOutputStream().println("no main process, can't export for type: " + type);
                response.flushBuffer();
                return;
            }

            String filename = "";
            byte[] exportBytes = null;

            String mainProcessId = bpmnModel.getMainProcess().getId();

            if (type.equals("bpmn")) {

                BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
                exportBytes = xmlConverter.convertToXML(bpmnModel);

                filename = mainProcessId + ".bpmn20.xml";
            } else if (type.equals("json")) {

                exportBytes = modelEditorSource;
                filename = mainProcessId + ".json";

            }

            ByteArrayInputStream in = new ByteArrayInputStream(exportBytes);
            IOUtils.copy(in, response.getOutputStream());

            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            response.flushBuffer();
        } catch (Exception e) {
            logger.error("导出model的xml文件失败：modelId={}, type={}", modelId, type, e);
        }
    }

    @RequestMapping(value = "delete/{modelId}.htm")
    public String delete(@PathVariable("modelId") String modelId) {
        repositoryService.deleteModel(modelId);
        return "redirect:/models/modelList.htm";
    }

    @RequestMapping(value = "startProcess.htm", method = RequestMethod.POST)
    public void startProcess(String processDefinitionKey, HttpSession session,
                             HttpServletResponse response) {
        JSONObject jsonObject;
        ActivitiDemoRequest request = new ActivitiDemoRequest();
        request.setProcessDefinitionKey(processDefinitionKey);
        try {
            User user = UserUtil.getUserFromSession(session);
            request.setBusinessKey("defaultKey");
            request.setRequestUserAccount(user.getId());
            request.setVariableMap(new HashMap<String, Object>(5));
            BaseResponse baseResponse = activitiDemoFacade.startProcess(request);
            logger.info("开启流程返回结果，response{}：", baseResponse);

            if (baseResponse != null && ResponseCode.SUCCESS.getCode().equals(baseResponse.getResponseCode())) {
                logger.info("开启流程成功，参数{}：", request);
                jsonObject = WebResponseUtil.success();
            } else {
                logger.warn("开启流程失败，参数{}", request);
                jsonObject = WebResponseUtil.error();
            }
        } catch (Exception e) {
            logger.error("开启流程失败, 系统异常，参数{}：", request, e);
            jsonObject = WebResponseUtil.error();
        }
        responseJson(response, jsonObject);
    }
}