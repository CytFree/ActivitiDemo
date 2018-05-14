package com.cyt.activiti.service.util;

import com.cyt.activiti.facade.request.ActivitiDemoRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * @author CaoYangTao
 * @date 2018/5/4  10:06
 */
public class ParamValideUtil {

    public static void checkStartProcess(ActivitiDemoRequest request) {
        Assert.isTrue(request != null, "参数不能为空");
        Assert.isTrue(StringUtils.isNotBlank(request.getRequestUserAccount()), "操作人不能为空");
        Assert.isTrue(StringUtils.isNotBlank(request.getBusinessKey()), "业务主键不能为空");
        Assert.isTrue(StringUtils.isNotBlank(request.getProcessDefinitionKey()), "流程定义Key不能为空");
    }

    public static void checkClaimTask(ActivitiDemoRequest request) {
        Assert.isTrue(request != null, "参数不能为空");
        Assert.isTrue(StringUtils.isNotBlank(request.getRequestUserAccount()), "操作人不能为空");
        Assert.isTrue(StringUtils.isNotBlank(request.getTaskId()), "任务ID不能为空");
    }

    public static void checkHandleTask(ActivitiDemoRequest request) {
        Assert.isTrue(request != null, "参数不能为空");
        Assert.isTrue(StringUtils.isNotBlank(request.getTaskId()), "任务ID不能为空");
    }
}
