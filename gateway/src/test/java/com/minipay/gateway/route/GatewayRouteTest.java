package com.minipay.gateway.route;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GatewayRouteTest {

    @Autowired
    private RouteLocator routeLocator;

    // 1. 验证网关启动后存在预期的路由
    @Test
    void testRoutesExist() {
        Flux<Route> routes = routeLocator.getRoutes();

        StepVerifier.create(routes.collectList())
                .assertNext(routeList -> {
                    List<String> routeIds = routeList.stream().map(Route::getId).toList();
                    assertTrue(routeIds.contains("payment-pay"), "应存在 payment-pay 路由");
                    assertTrue(routeIds.contains("payment-status"), "应存在 payment-status 路由");
                    assertTrue(routeIds.contains("order-service"), "应存在 order-service 路由");
                })
                .verifyComplete();
    }

}
