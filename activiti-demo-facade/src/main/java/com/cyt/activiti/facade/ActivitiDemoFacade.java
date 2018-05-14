package com.cyt.activiti.facade;

import com.cyt.activiti.facade.pojo.WaitHandleTaskVO;
import com.cyt.activiti.facade.request.ActivitiDemoQueryRequest;
import com.cyt.activiti.facade.request.ActivitiDemoRequest;
import com.cyt.activiti.facade.response.BaseResponse;
import com.cyt.activiti.facade.response.QueryObjResponse;

/**
 * @author CaoYangTao
 * @date 2018/4/27  10:23
 */
public interface ActivitiDemoFacade {
    /**
     * 开启流程
     * 流程定义key、业务主键、发起人必传
     *
     * @param request
     * @return
     */
    BaseResponse startProcess(ActivitiDemoRequest request);

    /**
     * 申领任务
     * 申领人、任务ID必传
     *
     * @param request
     * @return
     */
    BaseResponse claimTask(ActivitiDemoRequest request);

    /**
     * 处理任务
     * 任务ID必传
     *
     * @param request
     * @return
     */
    BaseResponse handleTask(ActivitiDemoRequest request);

    /**
     * 所有待处理任务数量
     * 操作人必传
     *
     * @param request
     * @return
     */
    long countWaitHandleTask(ActivitiDemoQueryRequest request);

    /**
     * 所有待处理任务
     * 操作人必传
     *
     * @param request
     * @return
     */
    QueryObjResponse<WaitHandleTaskVO> waitHandleTask(ActivitiDemoQueryRequest request);
}
