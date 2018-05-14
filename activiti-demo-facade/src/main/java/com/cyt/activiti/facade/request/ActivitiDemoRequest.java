package com.cyt.activiti.facade.request;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;

/**
 * @author CaoYangTao
 * @date 2018/4/27  10:49
 */
public class ActivitiDemoRequest extends BaseRequest {
    private static final long serialVersionUID = 8713761968164366053L;

    /**
     * 流程定义Key（即画流程图所定义的ID）
     */
    private String processDefinitionKey;

    /**
     * 业务方唯一主键
     */
    private String businessKey;

    /**
     * 流程任务ID
     */
    private String taskId;

    /**
     * 往下一个任务所传的参数Map
     */
    private Map<String, Object> variableMap;

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Map<String, Object> getVariableMap() {
        return variableMap;
    }

    public void setVariableMap(Map<String, Object> variableMap) {
        this.variableMap = variableMap;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE) .toString();
    }
}
