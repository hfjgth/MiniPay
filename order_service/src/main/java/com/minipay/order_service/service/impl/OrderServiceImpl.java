package com.minipay.order_service.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minipay.order_service.dto.CreateOrderRequest;
import com.minipay.order_service.dto.UpdateStatusRequest;
import com.minipay.order_service.enums.OrderStatus;
import com.minipay.order_service.mapper.OrderMapper;
import com.minipay.order_service.model.Order;
import com.minipay.order_service.service.OrderService;
import com.minipay.order_service.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderVO createOrder(CreateOrderRequest request) {
        String orderId = IdUtil.fastSimpleUUID();
        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderNo(request.getOrderNo());
        order.setAmount(request.getAmount());
        order.setStatus(OrderStatus.PENDING);
        order.setUserId(1L);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        int rows = orderMapper.insert(order);
        if (rows != 1) {
            log.info("订单创建失败");
            throw new RuntimeException("订单创建失败");
        }
        log.info("订单创建成功，orderId={}", orderId);
        return OrderVO.builder()
                .orderId(orderId)
                .orderNo(order.getOrderNo())
                .amount(order.getAmount())
                .status(order.getStatus().getCode())
                .createdAt(order.getCreateTime())
                .build();
    }

    @Override
    public OrderVO getOrderById(String orderId) {
        Order order = getOrderByOrderId(orderId);
        log.info("订单查询成功，orderId={}", orderId);
        return convertToVO(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(String orderId, UpdateStatusRequest request) {
        Order order = getOrderByOrderId(orderId);
        OrderStatus targetStatus;
        try {
            targetStatus = OrderStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的状态值");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("订单状态不允许修改");
        }
        order.setStatus(targetStatus);
        if (request.getPayId() != null) {
            order.setPayId(request.getPayId());
        }
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("订单状态更新成功，orderId={}, newStatus={}", orderId, targetStatus);
    }

    private Order getOrderByOrderId(String orderId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderId, orderId);
        Order order = orderMapper.selectOne(wrapper);
        if (order == null) {
            log.warn("订单查询失败，orderId={} 不存在", orderId);
            throw new RuntimeException("订单不存在");
        }
        return order;
    }

    private OrderVO convertToVO(Order order) {
        return OrderVO.builder()
                .orderId(order.getOrderId())
                .orderNo(order.getOrderNo())
                .amount(order.getAmount())
                .status(order.getStatus().getCode())
                .payId(order.getPayId())
                .createdAt(order.getCreateTime())
                .build();
    }
}