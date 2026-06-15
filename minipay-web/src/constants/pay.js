/**
 * 支付相关常量
 * 对应文档：支付方式、支付结果状态
 */

// 支付方式枚举
export const PAY_METHOD = {
    BALANCE: 'BALANCE', // 余额支付
    CARD: 'CARD'        // 银行卡支付
}

// 支付方式 -> 中文文案
export const PAY_METHOD_TEXT = {
    [PAY_METHOD.BALANCE]: '余额支付',
    [PAY_METHOD.CARD]: '银行卡支付'
}

// 支付结果状态枚举（发起支付接口返回）
export const PAY_RESULT = {
    SUCCESS: 'SUCCESS', // 支付成功
    FAIL: 'FAIL'        // 支付失败
}

// 支付结果状态 -> 中文文案
export const PAY_RESULT_TEXT = {
    [PAY_RESULT.SUCCESS]: '支付成功',
    [PAY_RESULT.FAIL]: '支付失败'
}

export const PAY_RESULT_TAG_TYPE = {
    [PAY_RESULT.SUCCESS]: 'success',
    [PAY_RESULT.FAIL]: 'danger'
}