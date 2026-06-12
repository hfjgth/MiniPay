package com.minipay.order_service.controller;

import com.minipay.order_service.common.Result;
import com.minipay.order_service.dto.CreateOrderRequest;
import com.minipay.order_service.dto.PayRequest;
import com.minipay.order_service.dto.UpdateStatusRequest;
import com.minipay.order_service.service.OrderService;
import com.minipay.order_service.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 1. 创建订单
    @PostMapping
    public Result<Map<String, String>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderVO vo = orderService.createOrder(request);
        Map<String, String> data = new HashMap<>();
        data.put("orderId", vo.getOrderId());
        data.put("status", vo.getStatus());
        return Result.success("创建成功", data);
    }

    // 2. 发起支付
    @PostMapping("/{orderId}/pay")
    public Result<Map<String, String>> pay(@PathVariable String orderId,
                                           @Valid @RequestBody PayRequest request) {
        String payId = orderService.payOrder(orderId, request);
        Map<String, String> data = new HashMap<>();
        data.put("payId", payId);
        data.put("status", "SUCCESS");   // 目前模拟总是成功
        return Result.success("支付成功", data);
    }

    // 3. 支付状态更新（内部/外部调用）
    @PutMapping("/{orderId}/status")
    public Result<Void> updateStatus(@PathVariable String orderId,
                                     @Valid @RequestBody UpdateStatusRequest request) {
        orderService.updateOrderStatus(orderId, request);
        return Result.success("状态更新成功", null);
    }

    // 4. 查询订单详情（支付结果）
    @GetMapping("/{orderId}")
    public Result<OrderVO> getOrder(@PathVariable String orderId) {
        OrderVO vo = orderService.getOrderById(orderId);
        return Result.success("查询成功", vo);
    }
}