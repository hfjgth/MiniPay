README_FOR_HUWENFENG.md
markdown
# 订单服务对接说明

## 1. 订单服务基本信息

- **服务端口**：`8081`
- **基础 URL**（本地开发）：`http://localhost:8081/api/v1/orders`
- **运行要求**：JDK 17 + MySQL 8.0（数据库名 `minipay`，表 `orders`）,数据库建表代码我放在src/main/resources/db/schema.sql里了

## 2. 如何启动订单服务

1. 确保本地 MySQL 已启动，并创建了 `minipay` 数据库及 `orders` 表（建表脚本在 `order_service/src/main/resources/db/schema.sql`）
2. 修改 `order_service/src/main/resources/application.yaml` 中的数据库密码
3. 在 IDEA 中运行 `OrderServiceApplication` 的 main 方法
4. 看到日志 `Started OrderServiceApplication` 即为成功

## 3. 你需要调用的接口（支付服务 → 订单服务）

### 更新订单状态（支付完成后必须调用）

- **方法**：`PUT`
- **路径**：`/api/v1/orders/{orderId}/status`
- **请求体**（JSON）：

```json
{
  "status": "PAID",     // 可选值：PAID / FAILED / CLOSED
  "payId": "PAYxxxxx"   // 你自己生成的支付流水号
}
响应示例：

json
{
  "code": 0,
  "message": "状态更新成功"
}
错误码：

102：订单不存在

103：订单状态不允许此操作（例如已支付的订单不能再改）

（可选）查询订单状态
方法：GET

路径：/api/v1/orders/{orderId}

返回订单详情，包含当前 status 和 payId

4. 网关路由配置建议
你的网关需要将 /api/v1/orders/** 转发到 localhost:8081。示例（以 Spring Cloud Gateway 为例）：

yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/v1/orders/**
5. 联调测试步骤
启动订单服务（确保端口 8081）

启动你的支付服务 + 网关

调用你的支付接口（例如 POST /api/v1/orders/{orderId}/pay）后，你的支付服务内部必须调用订单服务的 PUT /api/v1/orders/{orderId}/status 更新订单状态

最终前端查询订单时，状态应为 PAID

6. 常见问题
订单服务启动失败：检查 MySQL 是否运行，application.yaml 中数据库密码是否正确，端口 8081 是否被占用。

调用状态更新接口返回 404：确认网关路由是否正确，或直接访问 http://localhost:8081/api/v1/orders/{orderId}/status 测试。

状态更新后查询状态未变：检查请求体中的 status 参数值是否为 PAID（大写）。