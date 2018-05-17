package com.cyt.activiti.service.diagram;

import java.io.InputStream;

/**
 * @author CaoYangTao
 * @date 2018/5/14  11:32
 */
public interface ProcessInstanceDiagramService {

    /**
     * 获取当前任务流程图
     *
     * @param processInstanceId
     * @return
     */
    InputStream generateDiagram(String processInstanceId);
}
