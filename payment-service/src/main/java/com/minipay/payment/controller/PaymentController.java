package com.minipay.payment.controller;

import com.minipay.payment.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付服务控制器 — 按 MiniPay 接口定义文档实现
 */
@RestController
@RequestMapping("/api/v1/orders")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 3.2 发起支付
     * POST /api/v1/orders/{orderId}/pay
     */
    @PostMapping("/{orderId}/pay")
    public Map<String, Object> pay(
            @PathVariable String orderId,
            @RequestBody Map<String, Object> body) {

        String payMethod = (String) body.getOrDefault("payMethod", "BALANCE");
        double amount = ((Number) body.getOrDefault("amount", 0)).doubleValue();

        // 参数校验
        if (orderId == null || orderId.isBlank()) {
            return Map.of("code", 101, "message", "参数校验失败：orderId 不能为空");
        }
        if (amount <= 0) {
            return Map.of("code", 101, "message", "参数校验失败：金额必须大于0");
        }

        PaymentService.PayResult result = paymentService.processPay(orderId, payMethod, amount);

        return Map.of(
                "code", result.isSuccess() ? 0 : 201,
                "message", result.isSuccess() ? "支付成功" : "支付失败",
                "data", Map.of("payId", result.getPayId(), "status", result.getStatus())
        );
    }

    /**
     * 3.3 支付状态更新
     * PUT /api/v1/orders/{orderId}/status
     */
    @PutMapping("/{orderId}/status")
    public Map<String, Object> updateStatus(
            @PathVariable String orderId,
            @RequestBody Map<String, Object> body) {

        String status = (String) body.get("status");
        String payId = (String) body.get("payId");

        if (status == null || status.isBlank()) {
            return Map.of("code", 101, "message", "参数校验失败：status 不能为空");
        }

        paymentService.updateStatus(orderId, status, payId);

        return Map.of("code", 0, "message", "状态更新成功");
    }
}
