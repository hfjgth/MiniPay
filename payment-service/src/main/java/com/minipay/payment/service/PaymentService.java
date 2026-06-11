package com.minipay.payment.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付服务 — 模拟支付处理 + 订单状态管理（内存）
 */
@Service
public class PaymentService {

    private final Random random = new Random();
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** 订单状态存储（内存） */
    private final Map<String, OrderStatus> orderStore = new ConcurrentHashMap<>();

    /**
     * 模拟支付：随机成功/失败，生成流水号
     */
    public PayResult processPay(String orderId, String payMethod, double amount) {
        boolean success = random.nextBoolean();
        String payId = "PAY" + LocalDateTime.now().format(DF) + String.format("%04d", random.nextInt(10000));

        PayResult result = new PayResult();
        result.setPayId(payId);
        result.setSuccess(success);
        result.setStatus(success ? "SUCCESS" : "FAIL");

        // 更新内存中的订单状态
        OrderStatus os = orderStore.getOrDefault(orderId, new OrderStatus());
        os.setPayId(payId);
        os.setStatus(success ? "PAID" : "FAILED");
        orderStore.put(orderId, os);

        return result;
    }

    /**
     * 更新订单支付状态
     */
    public void updateStatus(String orderId, String status, String payId) {
        OrderStatus os = orderStore.getOrDefault(orderId, new OrderStatus());
        os.setStatus(status);
        if (payId != null && !payId.isBlank()) {
            os.setPayId(payId);
        }
        orderStore.put(orderId, os);
    }

    // ---- 内部类 ----

    public static class PayResult {
        private String payId;
        private boolean success;
        private String status;

        public String getPayId() { return payId; }
        public void setPayId(String payId) { this.payId = payId; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class OrderStatus {
        private String status;
        private String payId;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getPayId() { return payId; }
        public void setPayId(String payId) { this.payId = payId; }
    }
}
