package com.cyt.activiti.service.sign.impl;

import com.cyt.activiti.common.enums.AuditStatusEnum;
import com.cyt.activiti.common.exception.BizServiceException;
import com.cyt.activiti.common.vo.SignProcessVO;
import com.cyt.activiti.core.mapper.activiti.SignProcessDOMapper;
import com.cyt.activiti.core.pojo.SignProcessDO;
import com.cyt.activiti.facade.ActivitiDemoFacade;
import com.cyt.activiti.facade.enums.ResponseCode;
import com.cyt.activiti.facade.pojo.WaitHandleTaskVO;
import com.cyt.activiti.facade.request.ActivitiDemoQueryRequest;
import com.cyt.activiti.facade.response.QueryObjResponse;
import com.cyt.activiti.service.sign.SignWorkFlowService;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author CaoYangTao
 * @date 2018/4/25  10:20
 */
@Component
public class SignWorkFlowServiceImpl implements SignWorkFlowService {
    private static final String SIGN_PROCESS__INSTANCE_KEY = "addNetSignProcess";
    private static final Logger LOGGER = LoggerFactory.getLogger(SignWorkFlowServiceImpl.class);

    @Autowired
    private SignProcessDOMapper signProcessDOMapper;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ActivitiDemoFacade activitiDemoFacade;

    @Override
    public void saveSignProcess(SignProcessDO signProcessDO, Map<String, Object> param) {
        signProcessDO.setAuditStatus(Integer.valueOf(AuditStatusEnum.RISK_AUDIT_WAIT.getCode()));
        signProcessDO.setCreateTime(new Date());
        signProcessDO.setUpdateTime(new Date());

        int num = signProcessDOMapper.insertAndGetId(signProcessDO);
        if (num > 0) {
            //创建流程实例
            ProcessInstance processInstance = null;
            try {
                String businessKey = signProcessDO.getProcessId().toString();

                // 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
                identityService.setAuthenticatedUserId(signProcessDO.getApplyUserId());

                processInstance = runtimeService.startProcessInstanceByKey(SIGN_PROCESS__INSTANCE_KEY, businessKey, param);
                String processInstanceId = processInstance.getId();
                System.out.println("流程=" + processInstanceId + "启动成功");
            } finally {
                identityService.setAuthenticatedUserId(null);
            }
        } else {
            throw new BizServiceException("插入流程业务数据失败");
        }
    }

    @Override
    public QueryObjResponse<WaitHandleTaskVO> queryAllRuningProcess(Integer pageStartIndex, Integer pageSize,
                                                                    String op) {
        ActivitiDemoQueryRequest request = new ActivitiDemoQueryRequest();
        request.setPageSize(pageSize);
        request.setCurrentPage(pageStartIndex / pageSize + 1);
        if (StringUtils.isNotBlank(op)) {
            request.setRequestUserAccount(op);
        }
        QueryObjResponse<WaitHandleTaskVO> response = activitiDemoFacade.waitHandleTask(request);
        if (response != null && ResponseCode.SUCCESS.getCode().equals(response.getResponseCode())) {
            return response;
        }
        return null;
    }

    @Override
    public int countRuningProcess() {
        Long count = runtimeService.createProcessInstanceQuery().processDefinitionKey(SIGN_PROCESS__INSTANCE_KEY).active().count();
        return count.intValue();
    }

    @Override
    public List<SignProcessVO> queryWaitHandleSignProcessList(String userId, Integer pageStartIndex, Integer pageSize) {
        List<SignProcessVO> resultList = new ArrayList<SignProcessVO>();
        List<Task> taskList = taskService.createTaskQuery().active().taskCandidateOrAssigned(userId).listPage(pageStartIndex, pageSize);

        // 关联业务实体
        for (Task task: taskList) {
            String processInstanceId = task.getProcessInstanceId();
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).active().singleResult();
            if (processInstance == null) {
                continue;
            }
            String businessKey = processInstance.getBusinessKey();
            if (businessKey == null) {
                continue;
            }
            SignProcessDO signProcessDO = signProcessDOMapper.selectByPrimaryKey(new Long(businessKey));
            SignProcessVO signProcessVO = new SignProcessVO();
            BeanUtils.copyProperties(signProcessDO, signProcessVO);

            // 设置当前任务信息
            signProcessVO.setTask(task);
            signProcessVO.setProcessInstance(processInstance);
            signProcessVO.setProcessDefinition(getProcessDefinition(processInstance.getProcessDefinitionId()));
            resultList.add(signProcessVO);
        }

        return resultList;
    }

    @Override
    public int countWaitHandleSignProcess(String userId) {
        Long total = taskService.createTaskQuery().active().taskCandidateOrAssigned(userId).count();
        return total.intValue();
    }

    @Override
    public void claimTask(String userId, String taskId) {
        taskService.claim(taskId, userId);
    }

    @Override
    public void unClaimTask(String taskId) {
        taskService.unclaim(taskId);
    }

    @Override
    public void handleTask(String taskId, Map<String, Object> map) {
        Long processId = (Long) map.get("processId");
        Integer auditStatus = (Integer) map.get("auditStatus");
        if (processId != null) {
            SignProcessDO signProcessDO = signProcessDOMapper.selectByPrimaryKey(processId);
            if (signProcessDO != null) {
                map.put("agreemengtType", signProcessDO.getAgreementType());
                taskService.complete(taskId, map);

                signProcessDO.setAuditStatus(auditStatus);
                signProcessDO.setUpdateTime(new Date());
                signProcessDOMapper.updateByPrimaryKeySelective(signProcessDO);
            }
        }
    }

    @Override
    public List<SignProcessVO> queryAllCompleteProcess(Integer pageStartIndex, Integer pageSize) {
        List<HistoricProcessInstance> historicProcessInstanceList = historyService.createHistoricProcessInstanceQuery()
                .finished()
                //processDefinitionKey(SIGN_PROCESS__INSTANCE_KEY)
                .orderByProcessInstanceEndTime().desc().listPage(pageStartIndex, pageSize);
        List<SignProcessVO> resultList = new ArrayList<SignProcessVO>();
        // 关联业务实体
        for (HistoricProcessInstance historicProcessInstance : historicProcessInstanceList) {
            SignProcessVO signProcessVO = new SignProcessVO();
            signProcessVO.setHistoricProcessInstance(historicProcessInstance);
            signProcessVO.setProcessDefinition(getProcessDefinition(historicProcessInstance.getProcessDefinitionId()));
            resultList.add(signProcessVO);
        }
        return resultList;
    }

    @Override
    public int countCompleteProcess() {
        Long total = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(SIGN_PROCESS__INSTANCE_KEY).finished().count();
        return total.intValue();
    }

    /**
     * 查询流程定义对象
     *
     * @param processDefinitionId 流程定义ID
     * @return
     */
    protected ProcessDefinition getProcessDefinition(String processDefinitionId) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
        return processDefinition;
    }
}
