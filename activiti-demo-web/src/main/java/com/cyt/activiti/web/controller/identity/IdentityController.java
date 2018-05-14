package com.cyt.activiti.web.controller.identity;

import com.alibaba.fastjson.JSONObject;
import com.cyt.activiti.common.enums.WebResponse;
import com.cyt.activiti.common.exception.BizServiceException;
import com.cyt.activiti.common.utils.WebResponseUtil;
import com.cyt.activiti.common.vo.UserVo;
import com.cyt.activiti.web.controller.BaseController;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * 工作流内置用户、组Controller
 *
 * @author CaoYangTao
 * @date 2018/4/19  15:59
 */
@Controller
@RequestMapping("/management/identity/")
public class IdentityController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityController.class);

    @Autowired
    private IdentityService identityService;

    @RequestMapping(value = "userListIndex.htm", method = RequestMethod.GET)
    public String userListIndex() {
        return "identity/userList";
    }

    /**
     * 用户列表
     *
     * @param pageStartIndex
     * @param pageSize
     * @param response
     */
    @RequestMapping(value = "userList.htm", method = RequestMethod.POST)
    public void userList(@RequestParam(value = "pageStartIndex", defaultValue = "0") Integer pageStartIndex,
                         @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                         HttpServletResponse response) {
        UserQuery userQuery = identityService.createUserQuery();
        List<User> userList = userQuery.listPage(pageStartIndex, pageSize);

        long total = userQuery.count();

        // 查询每个人的分组，这样的写法比较耗费性能、时间，仅供读者参考
        List<UserVo> userVoList = new ArrayList<UserVo>();
        for (User user : userList) {
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user, userVo);
            List<Group> groupList = identityService.createGroupQuery().groupMember(user.getId()).list();
            userVo.setGroupList(groupList);
            userVoList.add(userVo);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOTAL_SIZE, total);
        jsonObject.put(ROW_LIST, userVoList);
        responseJson(response, jsonObject);
    }

    /**
     * 保存User
     *
     * @param newUser
     * @param response
     * @return
     */
    @RequestMapping(value = "saveUser.htm", method = RequestMethod.POST)
    public void saveUser(@ModelAttribute(value = "user") UserEntity newUser,
                         HttpServletResponse response) {
        JSONObject jsonObject;
        try {
            String userId = newUser.getId();
            User user = identityService.createUserQuery().userId(userId).singleResult();
            if (user == null) {
                user = identityService.newUser(userId);
                BeanUtils.copyProperties(newUser, user, new String[]{"revision"});
                identityService.saveUser(user);

                LOGGER.info("成功添加用户[{}]", newUser.getFirstName() + newUser.getLastName());
                jsonObject = WebResponseUtil.success();
            } else {
                jsonObject = WebResponseUtil.response(WebResponse.BIZ_ERROR.getResponseCode());
                jsonObject.put(ERROR_MSG, "添加用户失败：该改登录名[" + newUser.getId() + "]已存在");
                LOGGER.warn("添加用户[{}]失败：该改登录名[{}]已存在", newUser.getId(),
                        newUser.getFirstName() + newUser.getLastName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("添加用户[{}]失败：", newUser.getFirstName() + newUser.getLastName(), e);
            jsonObject = WebResponseUtil.response(WebResponse.SYSTEM_ERROR.getResponseCode());
            jsonObject.put(ERROR_MSG, "添加用户失败：系统异常");
        }

        responseJson(response, jsonObject);
    }

    /**
     * 保存User
     *
     * @param newUser
     * @param response
     * @return
     */
    @RequestMapping(value = "updateUser.htm", method = RequestMethod.POST)
    public void updateUser(@ModelAttribute(value = "user") UserEntity newUser,
                         HttpServletResponse response) {
        JSONObject jsonObject;
        try {
            String userId = newUser.getId();
            User user = identityService.createUserQuery().userId(userId).singleResult();
            if (user != null) {
                BeanUtils.copyProperties(newUser, user, new String[]{"revision"});
                identityService.saveUser(user);

                LOGGER.info("修改用户成功[{}]", newUser.getId());
                jsonObject = WebResponseUtil.success();
            } else {
                jsonObject = WebResponseUtil.response(WebResponse.BIZ_ERROR.getResponseCode());
                jsonObject.put(ERROR_MSG, "修改用户失败：该改登录名[" + newUser.getId() + "]不存在");
                LOGGER.warn("修改用户[{}]失败：该改登录ID[{}]不存在", newUser.getId(), newUser.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("修改用户[{}]失败：", newUser.getId(), e);
            jsonObject = WebResponseUtil.response(WebResponse.SYSTEM_ERROR.getResponseCode());
            jsonObject.put(ERROR_MSG, "修改用户失败：系统异常");
        }

        responseJson(response, jsonObject);
    }

    @RequestMapping(value = "deleteUser.htm", method = POST)
    public void deleteUser(HttpServletResponse response, String id) {
        JSONObject jsonObject;
        try {
            User user = identityService.createUserQuery().userId(id).singleResult();
            if (user == null) {
                throw new BizServiceException("用户ID="+ id +"不存在");
            }

            identityService.deleteUser(id);

            LOGGER.info("成功删除用户[{}]", id);
            jsonObject = WebResponseUtil.success();
        }  catch (BizServiceException e) {
            LOGGER.error("删除用户[{}]失败：", id, e);
            jsonObject = WebResponseUtil.response(WebResponse.BIZ_ERROR.getResponseCode());
            jsonObject.put(ERROR_MSG, "删除用户失败：" + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("删除用户[{}]失败：", id, e);
            jsonObject = WebResponseUtil.response(WebResponse.SYSTEM_ERROR.getResponseCode());
            jsonObject.put(ERROR_MSG, "删除用户失败：系统异常");
        }

        responseJson(response, jsonObject);
    }

    /**
     * 保存用户的用户组关系
     *
     * @param userId
     * @param groupIds
     * @param response
     * @return
     */
    @RequestMapping(value = "saveUserGroup.htm", method = RequestMethod.POST)
    public void saveUserGroup(String userId, String[] groupIds, HttpServletResponse response) {
        JSONObject jsonObject;
        try {
            List<Group> groupList = identityService.createGroupQuery().groupMember(userId).list();
            if (groupList != null && !groupList.isEmpty()) {
               for (Group group : groupList) {
                   identityService.deleteMembership(userId, group.getId());
               }
            }

            if (groupIds != null && groupIds.length > 0) {
                for (String groupId : groupIds) {
                    identityService.createMembership(userId, groupId);
                }
            }
            jsonObject = WebResponseUtil.success();
        } catch (Exception e) {
            LOGGER.error("设置用户[{}]的用户组[{}]失败：", userId, groupIds, e);
            jsonObject = WebResponseUtil.response(WebResponse.SYSTEM_ERROR.getResponseCode());
            jsonObject.put(ERROR_MSG, "设置用户的用户组失败：系统异常");
        }

        responseJson(response, jsonObject);
    }


    @RequestMapping(value = "groupListIndex.htm", method = RequestMethod.GET)
    public String groupListIndex() {
        return "identity/groupList";
    }

    /**
     * 用户组列表
     *
     * @param pageStartIndex
     * @param pageSize
     * @param response
     */
    @RequestMapping(value = "groupList.htm", method = RequestMethod.POST)
    public void groupList(@RequestParam(value = "pageStartIndex", defaultValue = "0") Integer pageStartIndex,
                          @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                          HttpServletResponse response) {
        GroupQuery groupQuery = identityService.createGroupQuery();
        List<Group> groupList = groupQuery.listPage(pageStartIndex, pageSize);

        long total = groupQuery.count();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOTAL_SIZE, total);
        jsonObject.put(ROW_LIST, groupList);
        responseJson(response, jsonObject);
    }

    /**
     * 保存用户组
     *
     * @param newGroupEntity
     * @param response
     * @return
     */
    @RequestMapping(value = "saveGroup.htm", method = RequestMethod.POST)
    public void saveGroup(@ModelAttribute(value = "group") GroupEntity newGroupEntity,
                          HttpServletResponse response) {
        JSONObject jsonObject;
        try {
            String groupId = newGroupEntity.getId();
            Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
            if (group == null) {
                group = identityService.newGroup(groupId);
                BeanUtils.copyProperties(newGroupEntity, group, new String[]{"revision"});
                identityService.saveGroup(group);

                LOGGER.info("成功添加用户组[{}]", newGroupEntity.getName());
                jsonObject = WebResponseUtil.success();
            } else {
                jsonObject = WebResponseUtil.response(WebResponse.BIZ_ERROR.getResponseCode());
                jsonObject.put(ERROR_MSG, "添加用户组失败：该用户组ID[" + newGroupEntity.getId() + "]已存在");
                LOGGER.warn("添加用户组[{}]失败：该用户组ID[{}]已存在", newGroupEntity.getName(), newGroupEntity.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("添加用户组[{}]失败：", newGroupEntity.getName(), e);
            jsonObject = WebResponseUtil.response(WebResponse.SYSTEM_ERROR.getResponseCode());
            jsonObject.put(ERROR_MSG, "添加用户组失败：系统异常");
        }

        responseJson(response, jsonObject);
    }

    /**
     * 用户组列表
     *
     * @param response
     */
    @RequestMapping(value = "allGroup.htm", method = RequestMethod.POST)
    public void groupList(HttpServletResponse response) {
        GroupQuery groupQuery = identityService.createGroupQuery();
        List<Group> groupList = groupQuery.list();

        JSONObject jsonObject = WebResponseUtil.success();
        jsonObject.put("groupList", groupList);
        responseJson(response, jsonObject);
    }
}
