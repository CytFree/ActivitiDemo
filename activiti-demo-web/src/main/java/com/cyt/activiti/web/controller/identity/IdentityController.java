package com.cyt.activiti.web.controller.identity;

import com.cyt.activiti.common.enums.WebResponse;
import com.cyt.activiti.common.utils.WebResponseUtil;
import com.cyt.activiti.web.controller.BaseController;
import com.meidusa.fastjson.JSONObject;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.Page;
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
        Map<String, List<Group>> groupOfUserMap = new HashMap<>(10);
        for (User user : userList) {
            List<Group> groupList = identityService.createGroupQuery().groupMember(user.getId()).list();
            groupOfUserMap.put(user.getId(), groupList);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(TOTAL_SIZE, total);
        jsonObject.put(ROW_LIST, userList);
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
}
