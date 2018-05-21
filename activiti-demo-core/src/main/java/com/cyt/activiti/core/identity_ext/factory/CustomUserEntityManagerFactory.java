package com.cyt.activiti.core.identity_ext.factory;

import com.cyt.activiti.core.identity_ext.CustomUserEntityManager;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author CaoYangTao
 * @date 2018/5/17  17:56
 */
@Service
public class CustomUserEntityManagerFactory implements SessionFactory {
    @Resource
    private CustomUserEntityManager customUserEntityManager;

    @Override
    public Class<?> getSessionType() {
        //注意此处也必须为Activiti原生类
        return UserIdentityManager.class;
    }

    @Override
    public Session openSession() {
        return customUserEntityManager;
    }
}
