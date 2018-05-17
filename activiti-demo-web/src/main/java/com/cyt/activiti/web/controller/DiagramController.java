package com.cyt.activiti.web.controller;

import com.alibaba.fastjson.JSON;
import com.cyt.activiti.service.diagram.ProcessInstanceDiagramService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * @author CaoYangTao
 * @date 2018/5/14  11:40
 */
@Controller
@RequestMapping("/diagram-test/")
public class DiagramController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiagramController.class);

    @Autowired
    private ProcessInstanceDiagramService processInstanceDiagramService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private IdentityService identityService;

    @RequestMapping("diagramView.htm")
    public void diagramView(HttpServletResponse response, String processInstanceId)
            throws ServletException, IOException {
        InputStream inputStream = processInstanceDiagramService.generateDiagram(processInstanceId);
        int i = inputStream.available();
        //byte数组用于存放图片字节数据
        byte[] buff = new byte[i];
        inputStream.read(buff);
        //记得关闭输入流
        inputStream.close();
        //设置发送到客户端的响应内容类型
        response.setContentType("image/png");
        OutputStream out = response.getOutputStream();
        out.write(buff);
        //关闭响应输出流
        out.close();
    }

    @RequestMapping("viewImage.htm")
    public String viewImage(ModelMap modelMap, String processInstanceId) {
        modelMap.put("processInstanceId", processInstanceId);
        return "viewImage";
    }


    /**
     * 获取各个节点的具体的信息
     *
     * @param processInstanceId 流程实例Id
     * @return
     */
    @RequestMapping("getProcessTrace.htm")
    @ResponseBody
    public List<Map<String, Object>> getProcessTrace(String processInstanceId) throws Exception {
        List<Map<String, Object>> activityInfos = new ArrayList<Map<String, Object>>();
        try {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().
                    processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
            ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(pd.getId());
            List<ActivityImpl> activitiList = processDefinition.getActivities();

            for (ActivityImpl activity : activitiList) {
                Map<String, Object> activityInfo = new HashMap<String, Object>(5);
                activityInfo.put("width", activity.getWidth());
                activityInfo.put("height", activity.getHeight());
                activityInfo.put("x", activity.getX());
                activityInfo.put("y", activity.getY());
                activityInfo.put("actId", activity.getId());

                // ActivityImpl 中没有getName方法，所以此处需要这样获取。
                activityInfo.put("name", activity.getProperty("name"));
                activityInfo.put("type", activity.getProperty("type"));

                ActivityBehavior activityBehavior = activity.getActivityBehavior();
                if (activityBehavior instanceof UserTaskActivityBehavior) {
                    putCandidate(processInstanceId, activityBehavior, activityInfo);
                }

                activityInfos.add(activityInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.info("获取各个节点的具体的信息成功：{}", JSON.toJSON(activityInfos).toString());
        return activityInfos;
    }

    private void putCandidate(String processInstanceId, ActivityBehavior activityBehavior,
                              Map<String, Object> activityInfo) {
        //已经处理的人
        List<String> handleCandidates = new ArrayList<String>();
        //接收人
        List<String> receiveCandidates = new ArrayList<String>();

        UserTaskActivityBehavior behavior = (UserTaskActivityBehavior) activityBehavior;
        List<HistoricTaskInstance> taskList = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).
                taskDefinitionKey(behavior.getTaskDefinition().getKey()).orderByTaskCreateTime().desc().list();
        for (HistoricTaskInstance historicTaskInstance : taskList) {
            if (StringUtils.isNotBlank(historicTaskInstance.getAssignee())) {
                StringBuffer sb = new StringBuffer();
                sb.append(historicTaskInstance.getAssignee());
                if (historicTaskInstance.getEndTime() != null) {
                    sb.append("（")
                            .append(new DateTime(historicTaskInstance.getEndTime()).toString("yyyy-MM-dd HH:mm:ss"))
                            .append("）");
                }
                handleCandidates.add(sb.toString());
            }
        }
        Expression assigneeExpression = behavior.getTaskDefinition().getAssigneeExpression();
        Set<Expression> userExpression = behavior.getTaskDefinition().getCandidateUserIdExpressions();
        Set<Expression> groupExpression = behavior.getTaskDefinition().getCandidateGroupIdExpressions();
        if (assigneeExpression != null) {
            receiveCandidates.add(assigneeExpression.getExpressionText());
        }
        if (userExpression != null) {
            for (Expression expression : userExpression) {
                receiveCandidates.add(expression.getExpressionText());
            }
        }
        if (groupExpression != null) {
            for (Expression expression : groupExpression) {
                String groupId = expression.getExpressionText();
                List<User> userList = identityService.createUserQuery().memberOfGroup(groupId).list();
                if (userList != null) {
                    for (User user : userList) {
                        receiveCandidates.add(user.getId());
                    }
                }
            }
        }

        activityInfo.put("handleCandidates", StringUtils.join(handleCandidates, "，"));
        activityInfo.put("receiveCandidates", StringUtils.join(receiveCandidates, "，"));
    }
}
