package com.minipay.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class PaymentService {

    private final Random random = new Random();
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final RestClient restClient;
    private final Map<String, OrderStatus> orderStore = new ConcurrentHashMap<>();

    @Value("${app.order-service.url:http://localhost:8081}")
    private String orderServiceUrl;

    public PaymentService() {
        this(RestClient.builder());
    }

    public PaymentService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    // ========== 供测试使用的 getter 和 setter ==========
    public String getOrderServiceUrl() {
        return orderServiceUrl;
    }

    public void setOrderServiceUrl(String orderServiceUrl) {
        this.orderServiceUrl = orderServiceUrl;
    }

    /**
     * 模拟支付：随机成功/失败 → 内部调订单服务更新状态
     */
    public PayResult processPay(String orderId, String payMethod, double amount) {
        log.info("开始处理支付，orderId={}，payMethod={}，amount={}", orderId, payMethod, amount);

        // 校验金额是否与订单一致
        try {
            var response = restClient.get()
                    .uri(orderServiceUrl + "/api/v1/orders/{orderId}", orderId)
                    .retrieve()
                    .body(Map.class);
            if (response != null && response.get("data") instanceof Map data) {
                Object orderAmount = data.get("amount");
                if (orderAmount != null && Math.abs(((Number) orderAmount).doubleValue() - amount) > 0.001) {
                    log.warn("支付金额与订单金额不匹配，orderId={}，请求金额={}，订单金额={}",
                            orderId, amount, orderAmount);
                    PayResult fail = new PayResult();
                    fail.setSuccess(false);
                    fail.setStatus("AMOUNT_MISMATCH");
                    fail.setPayId("");
                    return fail;
                }
            }
        } catch (Exception e) {
            log.warn("订单服务查询失败，跳过金额校验，orderId={}，原因：{}", orderId, e.getMessage());
        }

        boolean success = random.nextBoolean();
        String payId = "PAY" + LocalDateTime.now().format(DF) + String.format("%04d", random.nextInt(10000));

        PayResult result = new PayResult();
        result.setPayId(payId);
        result.setSuccess(success);
        result.setStatus(success ? "SUCCESS" : "FAIL");
        log.info("支付结果生成，orderId={}，payId={}，success={}", orderId, payId, success);

        // 调订单服务更新状态（服务间 HTTP 调用）
        String newStatus = success ? "PAID" : "FAILED";
        try {
            restClient.put()
                    .uri(orderServiceUrl + "/api/v1/orders/{orderId}/status", orderId)
                    .body(Map.of("status", newStatus, "payId", payId))
                    .retrieve()
                    .toBodilessEntity();
            log.info("订单状态同步成功，orderId={}，newStatus={}，payId={}", orderId, newStatus, payId);
        } catch (Exception e) {
            log.warn("订单状态同步失败，暂存内存，orderId={}，newStatus={}，payId={}，原因：{}",
                    orderId, newStatus, payId, e.getMessage());
            OrderStatus os = orderStore.getOrDefault(orderId, new OrderStatus());
            os.setPayId(payId);
            os.setStatus(newStatus);
            orderStore.put(orderId, os);
        }

        return result;
    }

    // ========== 内部类 ==========

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