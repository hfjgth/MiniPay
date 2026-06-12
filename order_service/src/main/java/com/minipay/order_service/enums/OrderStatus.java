package com.minipay.order_service.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    PENDING("PENDING", "待支付"),
    PAID("PAID", "已支付"),
    FAILED("FAILED", "支付失败"),
    CLOSED("CLOSED", "已关闭");

    @EnumValue   // 存到数据库的值
    private final String code;
    @JsonValue   // 序列化返回的值
    private final String desc;

    OrderStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}