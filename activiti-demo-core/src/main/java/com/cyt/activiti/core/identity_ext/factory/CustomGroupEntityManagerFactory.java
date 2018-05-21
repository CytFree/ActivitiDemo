package com.cyt.activiti.core.identity_ext.factory;

import com.cyt.activiti.core.identity_ext.CustomGroupEntityManager;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author CaoYangTao
 * @date 2018/5/17  17:58
 */
@Service
public class CustomGroupEntityManagerFactory implements SessionFactory {

    @Resource
    private CustomGroupEntityManager customGroupEntityManager;

    @Override
    public Class<?> getSessionType() {
        // 返回原始的GroupManager类型
        return GroupIdentityManager.class;
    }

    @Override
    public Session openSession() {
        // 返回自定义的GroupManager实例
        return customGroupEntityManager;
    }
}
