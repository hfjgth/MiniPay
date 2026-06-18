import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePaymentStore } from './payment'

describe('payment store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('应初始化默认状态', () => {
    const store = usePaymentStore()
    expect(store.showCreateOrder).toBe(false)
    expect(store.showPayment).toBe(false)
    expect(store.currentOrderId).toBe('')
    expect(store.paymentStatus).toBe('')
    expect(store.queryResult).toBeNull()
  })

  it('resetCreateOrder 应清空订单与支付相关状态', () => {
    const store = usePaymentStore()
    store.showCreateOrder = true
    store.showPayment = true
    store.currentOrderId = 'ORDER_123'
    store.paymentStatus = 'SUCCESS'
    store.orderForm.orderNo = 'TEST'
    store.paymentForm.payMethod = 'BALANCE'

    store.resetCreateOrder()

    expect(store.showCreateOrder).toBe(false)
    expect(store.showPayment).toBe(false)
    expect(store.currentOrderId).toBe('')
    expect(store.paymentStatus).toBe('')
    expect(store.orderForm.orderNo).toBe('')
    expect(store.paymentForm.payMethod).toBe('')
  })

  it('clearQuery 应清空查询条件与结果', () => {
    const store = usePaymentStore()
    store.queryOrderId = 'ORDER_123'
    store.queryResult = { orderId: 'ORDER_123' }

    store.clearQuery()

    expect(store.queryOrderId).toBe('')
    expect(store.queryResult).toBeNull()
  })
})
