package com.cyt.activiti.facade.request;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @author CaoYangTao
 * @date 2018/4/27  10:24
 */
public class BaseRequest implements Serializable {
    private static final long serialVersionUID = 4469404191669351213L;

    private String requestUserAccount;
    private String traceId;

    public String getRequestUserAccount() {
        return requestUserAccount;
    }

    public void setRequestUserAccount(String requestUserAccount) {
        this.requestUserAccount = requestUserAccount;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .toString();
    }
}
