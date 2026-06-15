/**
 * 接口错误码常量
 * 对应文档：全局错误码定义
 */

// 错误码枚举
export const ERROR_CODE = {
    SUCCESS: 0,          // 成功
    PARAM_ERROR: 101,    // 参数校验失败
    ORDER_NOT_EXIST: 102,// 订单不存在
    STATUS_FORBID: 103,  // 订单状态不允许此操作
    AMOUNT_MISMATCH: 201,// 支付金额与订单金额不匹配
    PAY_SERVICE_ERR: 202,// 支付服务异常
    SYSTEM_ERROR: 500    // 系统内部错误
}

// 错误码 -> 友好提示文案
export const ERROR_MSG = {
    [ERROR_CODE.SUCCESS]: '请求成功',
    [ERROR_CODE.PARAM_ERROR]: '参数校验失败',
    [ERROR_CODE.ORDER_NOT_EXIST]: '订单不存在',
    [ERROR_CODE.STATUS_FORBID]: '订单当前状态不支持该操作',
    [ERROR_CODE.AMOUNT_MISMATCH]: '支付金额与订单金额不匹配',
    [ERROR_CODE.PAY_SERVICE_ERR]: '支付服务异常',
    [ERROR_CODE.SYSTEM_ERROR]: '系统内部错误，请稍后重试'
}

export const ERROR_TAG_TYPE = {
    [ERROR_CODE.SUCCESS]: 'success',
    [ERROR_CODE.PARAM_ERROR]: 'danger',
    [ERROR_CODE.ORDER_NOT_EXIST]: 'danger',
    [ERROR_CODE.STATUS_FORBID]: 'danger',
    [ERROR_CODE.AMOUNT_MISMATCH]: 'danger',
    [ERROR_CODE.PAY_SERVICE_ERR]: 'danger',
    [ERROR_CODE.SYSTEM_ERROR]: 'danger'
}