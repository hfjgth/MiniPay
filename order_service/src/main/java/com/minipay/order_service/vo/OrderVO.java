package com.minipay.order_service.vo;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderVO {
    private String orderId;
    private String orderNo;
    private BigDecimal amount;
    private String status;        // 状态码 PENDING / PAID / FAILED / CLOSED
    private String payId;
    private LocalDateTime createdAt;
}