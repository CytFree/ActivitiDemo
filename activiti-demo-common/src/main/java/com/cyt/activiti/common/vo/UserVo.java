package com.cyt.activiti.common.vo;

import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.persistence.entity.UserEntity;

import java.util.List;

/**
 * @author CaoYangTao
 * @date 2018/4/23  17:45
 */
public class UserVo extends UserEntity{
    /**
     * 所在用户组
     */
    private List<Group> groupList;

    public List<Group> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList;
    }
}
