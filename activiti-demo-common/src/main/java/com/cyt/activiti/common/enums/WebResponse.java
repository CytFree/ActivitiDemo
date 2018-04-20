package com.cyt.activiti.common.enums;

/**
 * @author CaoYangTao
 * @date 2018/4/20  10:05
 */
public enum WebResponse {
    SUCCESS("S0001", "成功"),
    SYSTEM_ERROR("F0001", "系统异常"),
    BIZ_ERROR("F0002", "业务异常"),
    UNKNOW_ERROR("F9999", "未知异常"),;

    private final String responseCode;
    private final String desc;

    WebResponse(String responseCode, String desc) {
        this.responseCode = responseCode;
        this.desc = desc;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getDesc() {
        return desc;
    }

    public static WebResponse getByCode(String responseCode) {
        if (responseCode == null) {
            return null;
        }

        for (WebResponse webResponse : WebResponse.values()) {
            if (webResponse.getResponseCode().equals(responseCode)) {
                return webResponse;
            }
        }

        return null;
    }
}
