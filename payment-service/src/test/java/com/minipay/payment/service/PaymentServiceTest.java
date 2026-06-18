package com.minipay.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PaymentServiceTest {

    private RestClient.Builder builder;
    private MockRestServiceServer mockServer;
    private PaymentService paymentService;
    private String orderServiceUrl;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        paymentService = new PaymentService(builder);

        // 手动设置订单服务地址（测试环境用 localhost）
        orderServiceUrl = "http://localhost:8081";
        paymentService.setOrderServiceUrl(orderServiceUrl);
    }

    // 1. 订单服务可用且金额匹配 -> 返回支付结果
    @Test
    void testProcessPay_AmountMatch() {
        mockServer.expect(requestTo(orderServiceUrl + "/api/v1/orders/ORDER_001"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"code\":0,\"data\":{\"amount\":99.99}}", MediaType.APPLICATION_JSON));

        // 状态更新请求，允许成功或失败（模拟随机）
        mockServer.expect(requestTo(orderServiceUrl + "/api/v1/orders/ORDER_001/status"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess());

        PaymentService.PayResult result = paymentService.processPay("ORDER_001", "BALANCE", 99.99);

        assertNotNull(result);
        assertNotNull(result.getPayId());
        assertFalse(result.getPayId().isEmpty());
        mockServer.verify();
    }

    // 2. 订单服务可用但金额不匹配 -> AMOUNT_MISMATCH
    @Test
    void testProcessPay_AmountMismatch() {
        mockServer.expect(requestTo(orderServiceUrl + "/api/v1/orders/ORDER_002"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"code\":0,\"data\":{\"amount\":100.00}}", MediaType.APPLICATION_JSON));

        PaymentService.PayResult result = paymentService.processPay("ORDER_002", "BALANCE", 50.00);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("AMOUNT_MISMATCH", result.getStatus());
        assertTrue(result.getPayId().isEmpty());
    }

    // 3. 订单服务不可用 -> 仍能返回支付结果并进入内存暂存
    @Test
    void testProcessPay_OrderServiceDown() {
        mockServer.expect(requestTo(orderServiceUrl + "/api/v1/orders/ORDER_003"))
                .andRespond(withServerError());
        // GET 失败后仍会尝试 PUT 同步状态，PUT 也失败则进内存
        mockServer.expect(requestTo(orderServiceUrl + "/api/v1/orders/ORDER_003/status"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withServerError());

        PaymentService.PayResult result = paymentService.processPay("ORDER_003", "BALANCE", 20.00);

        assertNotNull(result);
        assertNotNull(result.getPayId());
        assertTrue("SUCCESS".equals(result.getStatus()) || "FAIL".equals(result.getStatus()));
        mockServer.verify();
    }

    // 4. 状态更新接口调用失败 -> 仍能返回结果
    @Test
    void testProcessPay_StatusUpdateFail() {
        mockServer.expect(requestTo(orderServiceUrl + "/api/v1/orders/ORDER_004"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"code\":0,\"data\":{\"amount\":10.00}}", MediaType.APPLICATION_JSON));

        mockServer.expect(requestTo(orderServiceUrl + "/api/v1/orders/ORDER_004/status"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withServerError());

        PaymentService.PayResult result = paymentService.processPay("ORDER_004", "BALANCE", 10.00);

        assertNotNull(result);
        assertNotNull(result.getPayId());
        mockServer.verify();
    }
}