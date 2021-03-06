package com.cyt.activiti.web.controller.sign;

import com.alibaba.fastjson.JSONObject;
import com.cyt.activiti.common.enums.WebResponse;
import com.cyt.activiti.common.exception.BizServiceException;
import com.cyt.activiti.common.utils.UserUtil;
import com.cyt.activiti.common.utils.WebResponseUtil;
import com.cyt.activiti.common.vo.SignProcessVO;
import com.cyt.activiti.core.pojo.SignProcessDO;
import com.cyt.activiti.facade.pojo.WaitHandleTaskVO;
import com.cyt.activiti.facade.response.QueryObjResponse;
import com.cyt.activiti.service.sign.SignWorkFlowService;
import com.cyt.activiti.web.controller.BaseController;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author CaoYangTao
 * @date 2018/4/25  10:40
 */
@Controller
@RequestMapping("/sign/")
public class SignProcessController extends BaseController {
    @Autowired
    private SignWorkFlowService signWorkFlowService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    /**
     * 新增流程页面
     */
    @RequestMapping(value = "addSignProcessIndex.htm", method = GET)
    public String addSignProcessIndex() {
        return "sign/addSignProcess";
    }

    /**
     * 新增流程
     */
    @RequestMapping(value = "saveSignProcess.htm", method = RequestMethod.POST)
    public void saveSignProcess(@ModelAttribute("signProcessDO") SignProcessDO signProcessDO,
                                HttpServletResponse response, HttpSession session) {
        JSONObject jsonObject;
        try {
            String userId = UserUtil.getUserFromSession(session).getId();
            signProcessDO.setApplyUserId(userId);

            Map<String, Object> param = new HashMap<String, Object>(5);
            signWorkFlowService.saveSignProcess(signProcessDO, param);
            jsonObject = WebResponseUtil.success();
        } catch (BizServiceException e) {
            jsonObject = WebResponseUtil.response(WebResponse.BIZ_ERROR.getResponseCode());
            jsonObject.put(ERROR_MSG, e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            jsonObject = WebResponseUtil.response(WebResponse.SYSTEM_ERROR.getResponseCode());
            jsonObject.put(ERROR_MSG, "系统异常");
            e.printStackTrace();
        }
        responseJson(response, jsonObject);
    }

    /**
     * 新增流程页面
     */
    @RequestMapping(value = "runSignProcessIndex.htm", method = GET)
    public String runSignProcessIndex() {
        return "sign/runSignProcessIndex";
    }

    /**
     * 正在运行的签约流程
     *
     * @param pageStartIndex
     * @param pageSize
     */
    @RequestMapping(value = "runSignProcessList.htm", method = {RequestMethod.POST, GET})
    public String runSignProcessList(@RequestParam(value = "pageStartIndex", defaultValue = "0") Integer pageStartIndex,
                                     @RequestParam(value = "pageSize", defaultValue = "30") Integer pageSize, ModelMap modelMap) {
        try {
            QueryObjResponse<WaitHandleTaskVO> response = signWorkFlowService.queryAllRuningProcess(pageStartIndex, pageSize, null);
            if (response != null) {
                Integer total = response.getTotalCount();
                modelMap.put(ROW_LIST, response.getResultList());
                modelMap.put(TOTAL_SIZE, total);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "sign/runSignProcessList";
    }

    /**
     * 正在运行的签约流程
     *
     * @param pageStartIndex
     * @param pageSize
     */
    @RequestMapping(value = "completeSignProcessList.htm", method = {RequestMethod.POST, GET})
    public String completeSignProcessList(@RequestParam(value = "pageStartIndex", defaultValue = "0") Integer pageStartIndex,
                                          @RequestParam(value = "pageSize", defaultValue = "30") Integer pageSize, ModelMap modelMap) {
        try {
            List<SignProcessVO> resultList = signWorkFlowService.queryAllCompleteProcess(pageStartIndex, pageSize);
            int total = signWorkFlowService.countCompleteProcess();

            modelMap.put(TOTAL_SIZE, total);
            modelMap.put(ROW_LIST, resultList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "sign/completeSignProcessList";
    }

    @RequestMapping(value = "waitHandleSignProcessList.htm", method = {RequestMethod.POST, GET})
    public String waitHandleSignProcessList(@RequestParam(value = "pageStartIndex", defaultValue = "0") Integer pageStartIndex,
                                            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize, ModelMap modelMap,
                                            HttpSession session) {
        try {
            User user = UserUtil.getUserFromSession(session);
            if (user == null) {
                return "login";
            }

                QueryObjResponse<WaitHandleTaskVO> response = signWorkFlowService.queryAllRuningProcess(pageStartIndex, pageSize,
                        user.getId());
                if (response != null) {
                    Integer total = response.getTotalCount();
                    modelMap.put(ROW_LIST, response.getResultList());
                    modelMap.put(TOTAL_SIZE, total);
                }
            return "sign/runSignProcessList";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "sign/waitHandleSignProcessList";
    }

    @RequestMapping(value = "claimTask.htm", method = POST)
    public void claimTask(HttpServletResponse response, HttpSession session, String taskId) {
        JSONObject jsonObject;
        User user = UserUtil.getUserFromSession(session);
        if (user == null) {
            jsonObject = WebResponseUtil.logout();
            jsonObject.put(ERROR_MSG, WebResponse.SESSION_OUT.getDesc());
        } else {
            signWorkFlowService.claimTask(user.getId(), taskId);
            jsonObject = WebResponseUtil.success();
        }
        responseJson(response, jsonObject);
    }

    @RequestMapping(value = "handleTask.htm", method = POST)
    public void handleTask(HttpServletResponse response, String taskId) {
        JSONObject jsonObject;
        Map<String, Object> map = new HashMap<String, Object>(5);
        taskService.complete(taskId, map);
        jsonObject = WebResponseUtil.success();
        responseJson(response, jsonObject);
    }

    @RequestMapping(value = "deleteProcessInstance.htm", method = POST)
    public void deleteProcessInstance(HttpServletResponse response, String processInstanceId) {
        JSONObject jsonObject;
        runtimeService.deleteProcessInstance(processInstanceId, "就是想删除了");
        jsonObject = WebResponseUtil.success();
        responseJson(response, jsonObject);
    }

    @RequestMapping(value = "unClaimTask.htm", method = POST)
    public void unClaimTask(HttpServletResponse response, String taskId) {

        JSONObject jsonObject;
        try {
            signWorkFlowService.unClaimTask(taskId);
            jsonObject = WebResponseUtil.success();
        } catch (Exception e) {
            jsonObject = WebResponseUtil.error();
            e.printStackTrace();
        }
        responseJson(response, jsonObject);
    }
}
