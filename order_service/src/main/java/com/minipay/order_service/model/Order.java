package com.minipay.order_service.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.minipay.order_service.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;               // 数据库自增主键

    private String orderId;        // 业务订单号（UUID，对外暴露）

    private String orderNo;        // 调用方传入的订单编号

    private Long userId;           // 用户ID（可暂时固定或从请求中获取）

    private BigDecimal amount;     // 订单金额

    private OrderStatus status;    // 订单状态（枚举）

    private String payId;          // 支付流水号（支付成功后填充）

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}