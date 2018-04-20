package com.cyt.activiti.web.controller;


import com.meidusa.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @author CaoYangTao
 * @date 2018/4/19  15:59
 */
public class BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);

    public static final String SUCESS_CODE = "S0001";
    public static final String SUCESS_MSG = "successMsg";
    public static final String ERROR_CODE = "F0001";
    public static final String ERROR_MSG = "errorMsg";

    public static final String TOTAL_SIZE = "total";
    public static final String ROW_LIST = "rows";

    /**
     * response写入JSON数据
     *
     * @param response
     * @param object
     */
    public void responseJson(HttpServletResponse response, JSONObject object) {
        response.setContentType("text/json; charset=UTF-8");
        PrintWriter printWriter = null;
        try {
            printWriter = response.getWriter();
            printWriter.write(object.toString());
            printWriter.flush();
        } catch (Exception e) {
            LOGGER.error("response 写入异常：", e);
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }
}
