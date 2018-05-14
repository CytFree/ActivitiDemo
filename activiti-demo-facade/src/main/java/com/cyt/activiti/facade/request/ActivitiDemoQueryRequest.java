package com.cyt.activiti.facade.request;

/**
 * @author CaoYangTao
 * @date 2018/4/27  10:49
 */
public class ActivitiDemoQueryRequest extends QueryBaseRequest {
    private static final long serialVersionUID = 8713761968164366053L;

    /**
     * 流程定义Key（即画流程图所定义的ID）
     */
    private String processDefinitionKey;

    /**
     * 业务唯一主键（如果是复合主键，业务方可以自定义格式）
     */
    private String businessKey;

    /**
     * 流程任务ID
     */
    private String taskId;

    /**
     * 流程定义ID（即流程定义表的主键ID）
     */
    private String processDefinitionId;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

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

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
}
