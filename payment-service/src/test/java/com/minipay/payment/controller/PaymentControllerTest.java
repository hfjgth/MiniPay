package com.minipay.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 1. 支付请求 - 正常参数
    @Test
    void testPay_SuccessParams() throws Exception {
        mockMvc.perform(post("/api/v1/orders/ORDER_001/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("payMethod", "BALANCE", "amount", 99.99))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.data.payId").exists())
                .andExpect(jsonPath("$.data.status").exists());
    }

    // 2. 支付请求 - 金额小于等于0
    @Test
    void testPay_InvalidAmount() throws Exception {
        mockMvc.perform(post("/api/v1/orders/ORDER_002/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("payMethod", "BALANCE", "amount", -10))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(101))
                .andExpect(jsonPath("$.message").value("参数校验失败：金额必须大于0"));
    }

    // 3. 支付请求 - 缺少金额字段（默认0）
    @Test
    void testPay_MissingAmount() throws Exception {
        mockMvc.perform(post("/api/v1/orders/ORDER_003/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("payMethod", "BALANCE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(101));
    }

    // 4. 支付请求 - 默认支付方式
    @Test
    void testPay_DefaultPayMethod() throws Exception {
        mockMvc.perform(post("/api/v1/orders/ORDER_004/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("amount", 10))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.data.payId").exists());
    }

}
