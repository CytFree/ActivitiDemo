package com.cyt.activiti.facade.enums;

/**
 * @author CaoYangTao
 * @date 2018/5/4  10:03
 */
public enum ResponseCode {
    SUCCESS("S0001", "成功"),
    PARAM_ERROR("F0001", "参数错误"),
    BIZ_ERROR("F0002", "业务异常"),
    SYSTEM_ERROR("F9999", "系统异常"),
    ;

    ResponseCode(String code, String msg){
        this.code = code;
        this.msg = msg;
    }

    final String code;
    final String msg;

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
