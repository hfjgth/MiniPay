package com.minipay.order_service;

import cn.hutool.core.util.IdUtil;
import com.minipay.order_service.dto.CreateOrderRequest;
import com.minipay.order_service.dto.UpdateStatusRequest;
import com.minipay.order_service.enums.OrderStatus;
import com.minipay.order_service.mapper.OrderMapper;
import com.minipay.order_service.model.Order;
import com.minipay.order_service.service.impl.OrderServiceImpl;
import com.minipay.order_service.vo.OrderVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    // 1. 正常创建订单
    @Test
    void testCreateOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrderNo("TEST_001");
        request.setAmount(new BigDecimal("100.00"));

        when(orderMapper.insert(any(Order.class))).thenReturn(1);
        OrderVO result = orderService.createOrder(request);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("TEST_001", result.getOrderNo());
        Assertions.assertEquals(new BigDecimal("100.00"), result.getAmount());
        Assertions.assertEquals(OrderStatus.PENDING.getCode(), result.getStatus());
        verify(orderMapper, times(1)).insert(any(Order.class));
    }

    // 2. 创建订单 - 数据库插入失败（抛异常）
    @Test
    void testCreateOrder_Fail() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setOrderNo("TEST_002");
        request.setAmount(new BigDecimal("88.00"));

        when(orderMapper.insert(any(Order.class))).thenReturn(0);
        Assertions.assertThrows(RuntimeException.class, () -> orderService.createOrder(request));
    }

    // 3. 正常更新订单状态
    @Test
    void testUpdateOrderStatus_Success() {
        String orderId = IdUtil.fastSimpleUUID();
        Order mockOrder = new Order();
        mockOrder.setOrderId(orderId);
        mockOrder.setStatus(OrderStatus.PENDING);
        mockOrder.setUpdateTime(LocalDateTime.now());

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus("PAID");
        request.setPayId("PAY_001");

        when(orderMapper.selectOne(any())).thenReturn(mockOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        Assertions.assertDoesNotThrow(() -> orderService.updateOrderStatus(orderId, request));
        Assertions.assertEquals(OrderStatus.PAID, mockOrder.getStatus());
        Assertions.assertEquals("PAY_001", mockOrder.getPayId());
        verify(orderMapper, times(1)).updateById(any(Order.class));
    }

    // 4. 更新状态 - 订单不存在
    @Test
    void testUpdateOrderStatus_OrderNotFound() {
        String orderId = "NOT_EXIST_ID";
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus("PAID");

        when(orderMapper.selectOne(any())).thenReturn(null);
        RuntimeException ex = Assertions.assertThrows(RuntimeException.class,
                () -> orderService.updateOrderStatus(orderId, request));
        Assertions.assertTrue(ex.getMessage().contains("订单不存在"));
    }

    // 5. 更新状态 - 非法状态流转
    @Test
    void testUpdateOrderStatus_IllegalStatus() {
        String orderId = IdUtil.fastSimpleUUID();
        Order mockOrder = new Order();
        mockOrder.setOrderId(orderId);
        mockOrder.setStatus(OrderStatus.PAID);

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus("CLOSED");

        when(orderMapper.selectOne(any())).thenReturn(mockOrder);
        RuntimeException ex = Assertions.assertThrows(RuntimeException.class,
                () -> orderService.updateOrderStatus(orderId, request));
        Assertions.assertTrue(ex.getMessage().contains("订单状态不允许修改"));
    }

    // 6. 根据ID查询订单 - 正常
    @Test
    void testGetOrderById_Success() {
        String orderId = IdUtil.fastSimpleUUID();
        Order mockOrder = new Order();
        mockOrder.setOrderId(orderId);
        mockOrder.setOrderNo("QUERY_001");
        mockOrder.setStatus(OrderStatus.PENDING);
        mockOrder.setAmount(new BigDecimal("50.00"));
        mockOrder.setCreateTime(LocalDateTime.now());
        mockOrder.setUpdateTime(LocalDateTime.now());

        when(orderMapper.selectOne(any())).thenReturn(mockOrder);
        OrderVO vo = orderService.getOrderById(orderId);

        Assertions.assertNotNull(vo);
        Assertions.assertEquals("QUERY_001", vo.getOrderNo());
    }

    // 7. 根据ID查询订单 - 订单不存在
    @Test
    void testGetOrderById_NotFound() {
        String orderId = "EMPTY_ID";
        when(orderMapper.selectOne(any())).thenReturn(null);

        RuntimeException ex = Assertions.assertThrows(RuntimeException.class,
                () -> orderService.getOrderById(orderId));
        Assertions.assertTrue(ex.getMessage().contains("订单不存在"));
    }
}