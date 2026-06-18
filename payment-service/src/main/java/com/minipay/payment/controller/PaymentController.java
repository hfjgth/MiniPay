package com.minipay.payment.controller;

import com.minipay.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付服务控制器 — 按 MiniPay 接口定义文档实现
 */
@Slf4j
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

        log.info("收到支付请求，orderId={}，payMethod={}，amount={}", orderId, payMethod, amount);

        // 参数校验
        if (orderId == null || orderId.isBlank()) {
            log.warn("支付参数校验失败：orderId 为空");
            return Map.of("code", 101, "message", "参数校验失败：orderId 不能为空");
        }
        if (amount <= 0) {
            log.warn("支付参数校验失败：金额不合法，amount={}", amount);
            return Map.of("code", 101, "message", "参数校验失败：金额必须大于0");
        }

        PaymentService.PayResult result = paymentService.processPay(orderId, payMethod, amount);

        int code;
        String message;
        if (result.isSuccess()) {
            code = 0;
            message = "支付成功";
            log.info("支付处理成功，orderId={}，payId={}", orderId, result.getPayId());
        } else if ("AMOUNT_MISMATCH".equals(result.getStatus())) {
            code = 201;
            message = "支付金额与订单金额不匹配";
            log.warn("支付金额不匹配，orderId={}，amount={}", orderId, amount);
        } else {
            code = 202;
            message = "支付失败";
            log.warn("支付处理失败，orderId={}，status={}", orderId, result.getStatus());
        }

        return Map.of(
                "code", code,
                "message", message,
                "data", Map.of("payId", result.getPayId(), "status", result.getStatus())
        );
    }

}
