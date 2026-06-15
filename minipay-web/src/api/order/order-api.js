import request from '@/utils/request'

/**
 * 1. 创建订单
 * @param {Object} data { orderNo, amount, description }
 */
export const createOrder = (data) => {
    return request({
        url: '/api/v1/orders',
        method: 'POST',
        data
    })
}

/**
 * 2. 发起支付
 * @param {string} orderId 系统订单ID
 * @param {Object} data { payMethod, amount }
 */
export const payOrder = (orderId, data) => {
    return request({
        url: `/api/v1/orders/${orderId}/pay`,
        method: 'POST',
        data
    })
}

/**
 * 3. 更新订单支付状态
 * @param {string} orderId 系统订单ID
 * @param {Object} data { status, payId }
 */
export const updateOrderStatus = (orderId, data) => {
    return request({
        url: `/api/v1/orders/${orderId}/status`,
        method: 'PUT',
        data
    })
}

/**
 * 4. 查询订单详情
 * @param {string} orderId 系统订单ID
 */
export const getOrderDetail = (orderId) => {
    return request({
        url: `/api/v1/orders/${orderId}`,
        method: 'GET'
    })
}