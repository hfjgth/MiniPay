package com.minipay.order_service.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minipay.order_service.dto.CreateOrderRequest;
import com.minipay.order_service.dto.PayRequest;
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
        // 生成业务订单号（UUID）
        String orderId = IdUtil.fastSimpleUUID();

        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderNo(request.getOrderNo());
        order.setAmount(request.getAmount());
        order.setStatus(OrderStatus.PENDING);
        order.setUserId(1L);           // 临时固定用户，实际可从认证上下文获取
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        orderMapper.insert(order);
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

        // 状态流转校验（简单版：只有 PENDING 可以转其他状态）
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

    @Override
    @Transactional
    public String payOrder(String orderId, PayRequest request) {
        Order order = getOrderByOrderId(orderId);
        // 金额校验
        if (order.getAmount().compareTo(request.getAmount()) != 0) {
            throw new RuntimeException("支付金额与订单金额不匹配");
        }
        // 状态必须是 PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("订单状态不允许支付");
        }

        // 模拟支付处理：这里简单认为总是成功
        String payId = IdUtil.fastSimpleUUID();  // 生成支付流水号
        order.setStatus(OrderStatus.PAID);
        order.setPayId(payId);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("支付成功，orderId={}, payId={}", orderId, payId);
        return payId;
    }

    private Order getOrderByOrderId(String orderId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderId, orderId);
        Order order = orderMapper.selectOne(wrapper);
        if (order == null) {
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