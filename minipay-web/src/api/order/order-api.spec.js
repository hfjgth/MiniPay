import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createOrder, payOrder, updateOrderStatus, getOrderDetail } from './order-api'

// 模拟 axios 实例
const mockRequest = vi.fn()
vi.mock('@/utils/request', () => ({
  default: (config) => mockRequest(config)
}))

describe('order-api', () => {
  beforeEach(() => {
    mockRequest.mockReset()
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('createOrder 应发送 POST /api/v1/orders', async () => {
    const payload = { orderNo: 'TEST_001', amount: 99.99 }
    mockRequest.mockResolvedValue({ orderId: 'ORDER_001' })

    const res = await createOrder(payload)

    expect(mockRequest).toHaveBeenCalledTimes(1)
    expect(mockRequest).toHaveBeenCalledWith({
      url: '/api/v1/orders',
      method: 'POST',
      data: payload
    })
    expect(res.orderId).toBe('ORDER_001')
  })

  it('payOrder 应发送 POST /api/v1/orders/{orderId}/pay', async () => {
    const payload = { payMethod: 'BALANCE', amount: 99.99 }
    mockRequest.mockResolvedValue({ payId: 'PAY_001', status: 'SUCCESS' })

    const res = await payOrder('ORDER_001', payload)

    expect(mockRequest).toHaveBeenCalledWith({
      url: '/api/v1/orders/ORDER_001/pay',
      method: 'POST',
      data: payload
    })
    expect(res.status).toBe('SUCCESS')
  })

  it('updateOrderStatus 应发送 PUT /api/v1/orders/{orderId}/status', async () => {
    const payload = { status: 'PAID', payId: 'PAY_001' }
    mockRequest.mockResolvedValue({ code: 0 })

    await updateOrderStatus('ORDER_001', payload)

    expect(mockRequest).toHaveBeenCalledWith({
      url: '/api/v1/orders/ORDER_001/status',
      method: 'PUT',
      data: payload
    })
  })

  it('getOrderDetail 应发送 GET /api/v1/orders/{orderId}', async () => {
    mockRequest.mockResolvedValue({ orderId: 'ORDER_001', status: 'PAID' })

    const res = await getOrderDetail('ORDER_001')

    expect(mockRequest).toHaveBeenCalledWith({
      url: '/api/v1/orders/ORDER_001',
      method: 'GET'
    })
    expect(res.status).toBe('PAID')
  })
})
