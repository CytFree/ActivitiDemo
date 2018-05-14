package com.cyt.activiti.web.controller.login;

import com.alibaba.fastjson.JSONObject;
import com.cyt.activiti.common.enums.WebResponse;
import com.cyt.activiti.common.utils.UserUtil;
import com.cyt.activiti.common.utils.WebResponseUtil;
import com.cyt.activiti.web.controller.BaseController;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author CaoYangTao
 * @date 2018/4/19  16:18
 */
@Controller
public class LoginController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private IdentityService identityService;

    @RequestMapping("/login.htm")
    public String login() {
        return "login";
    }

    @RequestMapping("/index.htm")
    public String index(HttpSession session, ModelMap modelMap) {
        User user = UserUtil.getUserFromSession(session);
        if (user != null) {
            modelMap.put("user", user);
            return "index";
        } else {
            modelMap.put(ERROR_MSG, "请先登录");
            return "login";
        }
    }

    /**
     * 登录系统
     *
     * @param userName
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "/logon.htm", method = RequestMethod.POST)
    public void logon(@RequestParam("username") String userName, @RequestParam("password") String password,
                      HttpSession session, HttpServletResponse response) {
        LOGGER.info("logon request: {username={}, password={}}", userName, password);

        JSONObject jsonObject;
        try {
            boolean checkPassword = identityService.checkPassword(userName, password);
            if (checkPassword) {
                // read user from database
                User user = identityService.createUserQuery().userId(userName).singleResult();
                UserUtil.saveUserToSession(session, user);

                List<Group> groupList = identityService.createGroupQuery().groupMember(userName).list();
                session.setAttribute("groups", groupList);

                String[] groupNames = new String[groupList.size()];
                for (int i = 0; i < groupNames.length; i++) {
                    groupNames[i] = groupList.get(i).getName();
                }

                session.setAttribute("groupNames", ArrayUtils.toString(groupNames));

                jsonObject = WebResponseUtil.success();
                jsonObject.put("user", user);
            } else {
                jsonObject = WebResponseUtil.response(WebResponse.SYSTEM_ERROR.getResponseCode());
                jsonObject.put(ERROR_MSG, "密码或者用户名错误");
                jsonObject.put("userName", userName);
            }
        } catch (Exception e) {
            jsonObject = WebResponseUtil.response(WebResponse.SYSTEM_ERROR.getResponseCode());
            jsonObject.put("userName", userName);
            jsonObject.put(ERROR_MSG, "系统异常");
            LOGGER.error("用户【{}】，登录异常：", userName, e);
        }
        responseJson(response, jsonObject);
    }

    @RequestMapping(value = "/logout.htm")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "/login";
    }
}
