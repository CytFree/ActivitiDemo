package com.cyt.activiti.common.utils;

import com.cyt.activiti.common.constants.CodeConstant;
import com.cyt.activiti.common.enums.WebResponse;
import com.meidusa.fastjson.JSONObject;

/**
 * Controller返回信息工具类
 *
 * @author CaoYangTao
 * @date 2018/4/20  10:12
 */
public class WebResponseUtil {
    /**
     * 返回成功
     *
     * @return
     */
    public static JSONObject success() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(CodeConstant.WEB_RESPONSE, WebResponse.SUCCESS);
        return jsonObject;
    }

    /**
     * 返回失败
     *
     * @return
     */
    public static JSONObject error() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(CodeConstant.WEB_RESPONSE, WebResponse.UNKNOW_ERROR);
        return jsonObject;
    }

    /**
     * 根据响应Code返回对应信息
     *
     * @param responseCode
     * @return
     */
    public static JSONObject response(String responseCode) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(CodeConstant.WEB_RESPONSE, WebResponse.getByCode(responseCode));
        return jsonObject;
    }
}
