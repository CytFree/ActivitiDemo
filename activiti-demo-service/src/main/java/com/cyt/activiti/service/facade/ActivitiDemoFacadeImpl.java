package com.cyt.activiti.service.facade;

import com.cyt.activiti.common.exception.BizServiceException;
import com.cyt.activiti.facade.enums.ResponseCode;
import com.cyt.activiti.facade.request.ActivitiDemoQueryRequest;
import com.cyt.activiti.service.util.ParamValideUtil;
import com.cyt.activiti.facade.ActivitiDemoFacade;
import com.cyt.activiti.facade.pojo.WaitHandleTaskVO;
import com.cyt.activiti.facade.request.ActivitiDemoRequest;
import com.cyt.activiti.facade.response.BaseResponse;
import com.cyt.activiti.facade.response.QueryObjResponse;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author CaoYangTao
 * @date 2018/4/27  10:38
 */
@Service(value = "activitiDemoFacade")
public class ActivitiDemoFacadeImpl implements ActivitiDemoFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivitiDemoFacadeImpl.class);

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Override
    public BaseResponse startProcess(ActivitiDemoRequest request) {
        Lock lock = new ReentrantLock();
        lock.lock();
        BaseResponse response = new BaseResponse();
        try {
            ParamValideUtil.checkStartProcess(request);
            identityService.setAuthenticatedUserId(request.getRequestUserAccount());
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                    request.getProcessDefinitionKey(), request.getBusinessKey(), request.getVariableMap());

            LOGGER.info("流程key={}，业务主键={}，启动成功，流程实例ID={} ",
                    request.getProcessDefinitionKey(), request.getBusinessKey(), processInstance.getId());

            response.setResponseCode(ResponseCode.SUCCESS.getCode());
            response.setResponseMsg(ResponseCode.SUCCESS.getMsg());
        } catch (IllegalArgumentException e) {
            response.setResponseCode(ResponseCode.PARAM_ERROR.getCode());
            response.setResponseCode(e.getMessage());
            LOGGER.warn("参数【{}】，流程启动失败，参数错误：", request, e);
        } catch (Exception e) {
            response.setResponseCode(ResponseCode.SYSTEM_ERROR.getCode());
            response.setResponseMsg("系统异常");
            LOGGER.error("参数【{}】，流程启动失败，系统异常：", request, e);
        } finally {
            identityService.setAuthenticatedUserId(null);
            lock.unlock();
        }
        return response;
    }

    @Override
    public BaseResponse claimTask(ActivitiDemoRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            ParamValideUtil.checkClaimTask(request);
            Task task = taskService.createTaskQuery().active().taskId(request.getTaskId()).singleResult();
            if (task == null) {
                throw new BizServiceException("任务已处理");
            }
            if (StringUtils.isNotBlank(task.getAssignee())) {
                throw new BizServiceException("任务已被申领");
            }

            taskService.claim(request.getTaskId(), request.getRequestUserAccount());

            response.setResponseCode(ResponseCode.SUCCESS.getCode());
            response.setResponseMsg("任务申领成功");
        } catch (IllegalArgumentException e) {
            response.setResponseCode(ResponseCode.PARAM_ERROR.getCode());
            response.setResponseCode(e.getMessage());
            LOGGER.warn("参数【{}】，任务申领失败，参数错误：", request, e);
        } catch (BizServiceException e) {
            response.setResponseCode(ResponseCode.BIZ_ERROR.getCode());
            response.setResponseMsg(e.getMessage());
            LOGGER.warn("参数【{}】，任务申领失败，业务异常：", request, e);
        } catch (Exception e) {
            response.setResponseCode(ResponseCode.SYSTEM_ERROR.getCode());
            response.setResponseMsg("系统异常");
            LOGGER.error("参数【{}】，任务申领失败，系统异常：", request, e);
        }
        return response;
    }

    @Override
    public BaseResponse handleTask(ActivitiDemoRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            ParamValideUtil.checkHandleTask(request);
            Task task = taskService.createTaskQuery().active().taskId(request.getTaskId()).singleResult();
            if (task == null) {
                throw new BizServiceException("任务已处理");
            }

            if (StringUtils.isBlank(task.getAssignee())) {
                taskService.setAssignee(request.getTaskId(), request.getRequestUserAccount());
            }
            taskService.complete(request.getTaskId(), request.getVariableMap());

            response.setResponseCode(ResponseCode.SUCCESS.getCode());
            response.setResponseMsg("任务处理成功");
        } catch (IllegalArgumentException e) {
            response.setResponseCode(ResponseCode.PARAM_ERROR.getCode());
            response.setResponseCode(e.getMessage());
            LOGGER.warn("参数【{}】，任务处理失败，参数错误：", request, e);
        } catch (BizServiceException e) {
            response.setResponseCode(ResponseCode.BIZ_ERROR.getCode());
            response.setResponseMsg(e.getMessage());
            LOGGER.warn("参数【{}】，任务处理失败，业务异常：", request, e);
        } catch (Exception e) {
            response.setResponseCode(ResponseCode.SYSTEM_ERROR.getCode());
            response.setResponseMsg("系统异常");
            LOGGER.error("参数【{}】，任务处理失败，系统异常：", request, e);
        }
        return response;
    }

    @Override
    public long countWaitHandleTask(ActivitiDemoQueryRequest request) {
        TaskQuery taskQuery = createTaskQuery(request);
        long totalCount = taskQuery.count();
        return totalCount;
    }

    @Override
    public QueryObjResponse<WaitHandleTaskVO> waitHandleTask(ActivitiDemoQueryRequest request) {
        QueryObjResponse<WaitHandleTaskVO> response = new QueryObjResponse<WaitHandleTaskVO>();

        try {
            TaskQuery taskQuery = createTaskQuery(request);
            int firstResult = (request.getCurrentPage() - 1) * request.getPageSize();
            long totalCount = taskQuery.count();
            List<Task> taskList = taskQuery.listPage(firstResult, request.getPageSize());

            List<WaitHandleTaskVO> resultList = new ArrayList<WaitHandleTaskVO>();
            if (!CollectionUtils.isEmpty(taskList)) {
                for (Task task : taskList) {
                    WaitHandleTaskVO vo = convertToWaitHandleTaskVO(task);
                    resultList.add(vo);
                }
            }

            response.setTotalCount((int) totalCount);
            response.setResultList(resultList);
            response.setResponseCode(ResponseCode.SUCCESS.getCode());
            response.setResponseMsg(ResponseCode.SUCCESS.getMsg());
        } catch (Exception e) {

        }

        return response;
    }

    private WaitHandleTaskVO convertToWaitHandleTaskVO(Task task) {
        WaitHandleTaskVO vo = new WaitHandleTaskVO();
        vo.setTaskId(task.getId());
        vo.setTaskName(task.getName());
        vo.setTaskAssignee(task.getAssignee());
        vo.setTaskDefinitionKey(task.getTaskDefinitionKey());
        vo.setTaskCreateTime(task.getCreateTime());
        vo.setProcessDefinitionId(task.getProcessDefinitionId());
        vo.setProcessInstanceId(task.getProcessInstanceId());

        String processInstanceId = vo.getProcessInstanceId();

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance != null) {
            vo.setBusinessKey(processInstance.getBusinessKey());
            vo.setProcessDefinitionKey(processInstance.getProcessDefinitionKey());
            vo.setProcessDefinitionName(processInstance.getProcessDefinitionName());
        }

        return vo;
    }

    /**
     * 获取Task查询对象
     *
     * @param request
     * @return
     */
    private TaskQuery createTaskQuery(ActivitiDemoQueryRequest request) {
        TaskQuery taskQuery = taskService.createTaskQuery().active().orderByTaskCreateTime().desc();

        if (StringUtils.isNotBlank(request.getRequestUserAccount())) {
            taskQuery = taskQuery.taskCandidateOrAssigned(request.getRequestUserAccount());
        }

        if (StringUtils.isNotBlank(request.getProcessDefinitionKey())) {
            taskQuery = taskQuery.processDefinitionKey(request.getProcessDefinitionKey());
        }

        if (StringUtils.isNotBlank(request.getTaskId())) {
            taskQuery = taskQuery.taskId(request.getTaskId());
        }

        if (StringUtils.isNotBlank(request.getBusinessKey())) {
            taskQuery = taskQuery.processInstanceBusinessKey(request.getBusinessKey());
        }

        return taskQuery;
    }
}
