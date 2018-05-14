package com.cyt.activiti.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author CaoYangTao
 * @date 2018/4/25  11:04
 */
public enum AuditStatusEnum {
    RISK_AUDIT_WAIT("0", "待风控审核"),
    RISK_AUDIT_PASS("1", "风控审核通过"),
    RISK_AUDIT_REJECTED("2", "风控审核驳回"),

    LEGAL_AUDIT_WAIT("3", "待法务审核"),
    LEGAL_AUDIT_PASS("4", "法务审核通过"),
    LEGAL_AUDIT_REJECTED("5", "法务审核驳回"),

    OPERATION_AUDIT_WAIT("6", "待运营配置"),
    OPERATION_AUDIT_PASS("7", "运营完成配置"),
    OPERATION_AUDIT_REJECTED("8", "运营审核驳回"),

    ADMINISTRATION_AUDIT_WAIT("9", "待行政盖章"),
    ADMINISTRATION_AUDIT_PASS("10", "行政已盖章"),

    COMPLIANCE_AUDIT_WAIT("11", "待合规归档"),
    COMPLIANCE_AUDIT_PASS("12", "合规已归档");

    // 成员变量
    private final String code;

    private final String message;

    // 构造方法
    AuditStatusEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据代码获取ENUM
     *
     * @param code
     * @return
     */
    public static AuditStatusEnum getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (AuditStatusEnum info : AuditStatusEnum.values()) {
            if (info.getCode().equals(code)) {
                return info;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
