USE `minipay`;

CREATE TABLE `orders` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                          `order_id` VARCHAR(32) NOT NULL COMMENT '业务订单号（UUID）',
                          `order_no` VARCHAR(64) NOT NULL COMMENT '调用方订单编号',
                          `user_id` BIGINT NOT NULL COMMENT '用户ID',
                          `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
                          `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态：PENDING/PAID/FAILED/CLOSED',
                          `pay_id` VARCHAR(32) DEFAULT NULL COMMENT '支付流水号',
                          `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_order_id` (`order_id`),
                          KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';