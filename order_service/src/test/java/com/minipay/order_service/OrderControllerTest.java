package com.minipay.order_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minipay.order_service.dto.CreateOrderRequest;
import com.minipay.order_service.dto.UpdateStatusRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 1. 正常创建订单接口
    @Test
    void testCreateOrderApi_Success() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrderNo("API_001");
        request.setAmount(new BigDecimal("99.99"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderId").isNotEmpty());
    }

    // 2. 创建订单 - 非法负数金额
    @Test
    void testCreateOrderApi_InvalidAmount() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrderNo("API_002");
        request.setAmount(new BigDecimal("-10.00"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(101));
    }

    // 3. 查询订单 - 正常
    @Test
    void testGetOrderApi_Success() throws Exception {
        CreateOrderRequest createReq = new CreateOrderRequest();
        createReq.setOrderNo("QUERY_API_001");
        createReq.setAmount(new BigDecimal("50.00"));

        String resp = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(resp).get("data").get("orderId").asText();

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderId").value(orderId));
    }

    // 4. 更新订单状态 - 正常
    @Test
    void testUpdateStatusApi_Success() throws Exception {
        CreateOrderRequest createReq = new CreateOrderRequest();
        createReq.setOrderNo("UPDATE_API_001");
        createReq.setAmount(new BigDecimal("66.00"));

        String resp = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String orderId = objectMapper.readTree(resp).get("data").get("orderId").asText();

        UpdateStatusRequest updateReq = new UpdateStatusRequest();
        updateReq.setStatus("PAID");
        updateReq.setPayId("PAY_API_001");

        mockMvc.perform(put("/api/v1/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // 5. 查询不存在的订单
    @Test
    void testGetOrderApi_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/orders/NO_SUCH_ID_123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(102));
    }

    // 6. 创建订单 - 空订单号
    @Test
    void testCreateOrderApi_BlankOrderNo() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrderNo("");
        request.setAmount(new BigDecimal("10.00"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(101));
    }

    // 7. 创建订单 - 金额为零
    @Test
    void testCreateOrderApi_ZeroAmount() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrderNo("API_003");
        request.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(101));
    }

    // 8. 更新不存在的订单状态
    @Test
    void testUpdateStatusApi_NotFound() throws Exception {
        UpdateStatusRequest updateReq = new UpdateStatusRequest();
        updateReq.setStatus("PAID");
        updateReq.setPayId("PAY_API_002");

        mockMvc.perform(put("/api/v1/orders/{id}/status", "NO_SUCH_ID_123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(102));
    }

    // 9. 非法状态流转（PENDING -> CLOSED）
    @Test
    void testUpdateStatusApi_IllegalStatus() throws Exception {
        CreateOrderRequest createReq = new CreateOrderRequest();
        createReq.setOrderNo("UPDATE_API_002");
        createReq.setAmount(new BigDecimal("55.00"));

        String resp = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String orderId = objectMapper.readTree(resp).get("data").get("orderId").asText();

        // 先更新为 PAID
        UpdateStatusRequest updateReq = new UpdateStatusRequest();
        updateReq.setStatus("PAID");
        updateReq.setPayId("PAY_API_003");
        mockMvc.perform(put("/api/v1/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 再次尝试更新为 CLOSED，应被禁止
        updateReq.setStatus("CLOSED");
        mockMvc.perform(put("/api/v1/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(103));
    }
}