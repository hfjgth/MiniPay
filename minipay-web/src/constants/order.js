/**
 * 订单相关常量
 * 对应文档：订单状态定义、状态映射
 */

// 订单状态枚举
export const ORDER_STATUS = {
    PENDING: 'PENDING',  // 待支付
    PAID: 'PAID',        // 已支付
    FAILED: 'FAILED',    // 支付失败
    CLOSED: 'CLOSED'     // 已关闭
}

// 订单状态 -> 中文展示文案
export const ORDER_STATUS_TEXT = {
    [ORDER_STATUS.PENDING]: '待支付',
    [ORDER_STATUS.PAID]: '已支付',
    [ORDER_STATUS.FAILED]: '支付失败',
    [ORDER_STATUS.CLOSED]: '已关闭',
    'SUCCESS': '已支付',
    'FAIL': '支付失败'
}

// 订单状态 -> Element Plus Tag 样式类型
export const ORDER_STATUS_TAG_TYPE = {
    [ORDER_STATUS.PENDING]: 'warning',
    [ORDER_STATUS.PAID]: 'success',
    [ORDER_STATUS.FAILED]: 'danger',
    [ORDER_STATUS.CLOSED]: 'info',
    'SUCCESS': 'success',
    'FAIL': 'danger'
}