package com.cyt.activiti.service;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * @author CaoYangTao
 * @date 2018/4/20  15:53
 */
public class Test {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("activiti.cfg.xml");
        IdentityService identityService = context.getBean(IdentityService.class);
        List<User> list = identityService.createUserQuery().listPage(0, 10);
        System.out.println(list.size());
    }
}
