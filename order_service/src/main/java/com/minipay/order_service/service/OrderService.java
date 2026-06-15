package com.minipay.order_service.service;

import com.minipay.order_service.dto.CreateOrderRequest;
import com.minipay.order_service.dto.UpdateStatusRequest;
import com.minipay.order_service.vo.OrderVO;

public interface OrderService {
    OrderVO createOrder(CreateOrderRequest request);
    OrderVO getOrderById(String orderId);
    void updateOrderStatus(String orderId, UpdateStatusRequest request);

}