package com.minipay.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {

    private final Random random = new Random();
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final RestClient restClient;
    private final Map<String, OrderStatus> orderStore = new ConcurrentHashMap<>();

    public PaymentService() {
        this.restClient = RestClient.create();
    }

    /**
     * 模拟支付：随机成功/失败 → 内部调订单服务更新状态
     */
    public PayResult processPay(String orderId, String payMethod, double amount) {
        // 校验金额是否与订单一致
        try {
            var response = restClient.get()
                    .uri("http://localhost:8081/api/v1/orders/{orderId}", orderId)
                    .retrieve()
                    .body(Map.class);
            if (response != null && response.get("data") instanceof Map data) {
                Object orderAmount = data.get("amount");
                if (orderAmount != null && Math.abs(((Number) orderAmount).doubleValue() - amount) > 0.001) {
                    PayResult fail = new PayResult();
                    fail.setSuccess(false);
                    fail.setStatus("AMOUNT_MISMATCH");
                    fail.setPayId("");
                    return fail;
                }
            }
        } catch (Exception ignored) {
            // 订单服务不可用，跳过金额校验
        }

        boolean success = random.nextBoolean();
        String payId = "PAY" + LocalDateTime.now().format(DF) + String.format("%04d", random.nextInt(10000));

        PayResult result = new PayResult();
        result.setPayId(payId);
        result.setSuccess(success);
        result.setStatus(success ? "SUCCESS" : "FAIL");

        // 调订单服务更新状态（服务间 HTTP 调用）
        String newStatus = success ? "PAID" : "FAILED";
        try {
            restClient.put()
                    .uri("http://localhost:8081/api/v1/orders/{orderId}/status", orderId)
                    .body(Map.of("status", newStatus, "payId", payId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            // 订单服务未就绪，先存内存
            OrderStatus os = orderStore.getOrDefault(orderId, new OrderStatus());
            os.setPayId(payId);
            os.setStatus(newStatus);
            orderStore.put(orderId, os);
        }

        return result;
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
