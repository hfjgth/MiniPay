package com.minipay.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    @NotBlank(message = "状态不能为空")
    private String status;    // PAID / FAILED / CLOSED

    private String payId;     // 支付流水号（支付成功时必填）
}