package com.minipay.order_service;

import cn.hutool.core.util.IdUtil;
import com.minipay.order_service.enums.OrderStatus;
import com.minipay.order_service.mapper.OrderMapper;
import com.minipay.order_service.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // 测试结束自动回滚，不污染数据库
public class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    // 测试新增订单
    @Test
    void testInsertOrder() {
        Order order = new Order();
        String orderId = IdUtil.fastSimpleUUID();
        order.setOrderId(orderId);
        order.setOrderNo("MAPPER_TEST_001");
        order.setAmount(new BigDecimal("20.00"));
        order.setStatus(OrderStatus.PENDING);
        order.setUserId(1L);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        int rows = orderMapper.insert(order);
        assertEquals(1, rows);

        Long id = order.getId();
        Order dbOrder = orderMapper.selectById(id);
        assertNotNull(dbOrder);
        assertEquals("MAPPER_TEST_001", dbOrder.getOrderNo());
        assertEquals(OrderStatus.PENDING, dbOrder.getStatus());
        assertEquals(1L, dbOrder.getUserId());
    }

    // 测试根据ID查询
    @Test
    void testSelectById() {
        Order order = new Order();
        String orderId = IdUtil.fastSimpleUUID();
        order.setOrderId(orderId);
        order.setOrderNo("SELECT_TEST_001");
        order.setAmount(new BigDecimal("30.00"));
        order.setStatus(OrderStatus.PENDING);
        order.setUserId(1L);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);

        Long id = order.getId();
        Order res = orderMapper.selectById(id);
        assertNotNull(res);
        assertEquals(orderId, res.getOrderId());
        assertEquals(new BigDecimal("30.00"), res.getAmount());
    }

    // 测试更新订单
    @Test
    void testUpdateById() {
        Order order = new Order();
        String orderId = IdUtil.fastSimpleUUID();
        order.setOrderId(orderId);
        order.setOrderNo("UPDATE_TEST_001");
        order.setAmount(new BigDecimal("40.00"));
        order.setStatus(OrderStatus.PENDING);
        order.setUserId(1L);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);

        order.setStatus(OrderStatus.PAID);
        int rows = orderMapper.updateById(order);
        assertEquals(1, rows);

        long id = order.getId();
        Order after = orderMapper.selectById(id);
        assertEquals(OrderStatus.PAID, after.getStatus());
        assertEquals(order.getOrderNo(), after.getOrderNo());
    }
}