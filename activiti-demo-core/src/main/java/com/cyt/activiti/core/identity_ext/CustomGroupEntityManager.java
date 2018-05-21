package com.cyt.activiti.core.identity_ext;

import com.cyt.activiti.core.mapper.user.GroupMapper;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author CaoYangTao
 * @date 2018/5/17  17:55
 */
@Component
public class CustomGroupEntityManager extends GroupEntityManager {
    @Autowired
    private GroupMapper groupMapper;

    @Override
    public void insertGroup(Group group) {
        GroupEntity groupEntity = new GroupEntity();
        BeanUtils.copyProperties(group, groupEntity);
        groupMapper.insertGroup(groupEntity);
//        super.insertGroup(group);
    }
}
