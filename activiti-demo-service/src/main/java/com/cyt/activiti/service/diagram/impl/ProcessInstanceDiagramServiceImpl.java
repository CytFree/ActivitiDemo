package com.cyt.activiti.service.diagram.impl;

import com.cyt.activiti.diagram.ProcessInstanceDiagramCmd;
import com.cyt.activiti.service.diagram.ProcessInstanceDiagramService;
import org.activiti.engine.*;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * @author CaoYangTao
 * @date 2018/5/14  11:33
 */
@Service("processInstanceDiagramService")
public class ProcessInstanceDiagramServiceImpl implements ProcessInstanceDiagramService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessEngineFactoryBean processEngine;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private IdentityService identityService;

    @Override
    public InputStream generateDiagram(String processInstanceId) {
        //方法中用到的参数是流程实例ID，其实TaskId也可以转为这个。调用taskService查询即可。
        Command<InputStream> cmd = new ProcessInstanceDiagramCmd(processInstanceId, runtimeService, repositoryService, processEngine, historyService, identityService);
        return managementService.executeCommand(cmd);
    }
}
