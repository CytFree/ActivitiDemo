package com.cyt.activiti.service.sign;

import com.cyt.activiti.common.vo.SignProcessVO;
import com.cyt.activiti.core.pojo.SignProcessDO;
import com.cyt.activiti.facade.pojo.WaitHandleTaskVO;
import com.cyt.activiti.facade.response.QueryObjResponse;

import java.util.List;
import java.util.Map;

/**
 * 签约流程服务
 *
 * @author CaoYangTao
 * @date 2018/4/25  10:20
 */
public interface SignWorkFlowService {
    /**
     * 发起签约流程
     *
     * @param signProcessDO
     */
    void saveSignProcess(SignProcessDO signProcessDO, Map<String, Object> param);

    QueryObjResponse<WaitHandleTaskVO> queryAllRuningProcess(Integer pageStartIndex, Integer pageSize, String op);

    int countRuningProcess();

    List<SignProcessVO> queryWaitHandleSignProcessList(String userId, Integer pageStartIndex, Integer pageSize);

    int countWaitHandleSignProcess(String userId);

    void claimTask(String userId, String taskId);

    void handleTask(String taskId, Map<String, Object> map);

    List<SignProcessVO> queryAllCompleteProcess(Integer pageStartIndex, Integer pageSize);

    int countCompleteProcess();
}
