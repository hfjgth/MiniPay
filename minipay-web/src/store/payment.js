import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const usePaymentStore = defineStore('payment', {
  state: () => ({
    // 面板显隐
    showCreateOrder: false,
    showPayment: false,

    // 订单表单
    orderForm: {
      orderNo: '',
      amount: '',
      description: ''
    },

    // 支付表单
    paymentForm: {
      payMethod: '',
      amount: ''
    },

    // 当前订单信息
    currentOrderId: '',
    paymentStatus: '',

    // 查询区域
    queryOrderId: '',
    queryResult: null
  }),

  actions: {
    // 重置订单相关所有状态
    resetCreateOrder() {
      this.orderForm = { orderNo: '', amount: '', description: '' }
      this.paymentForm = { payMethod: '', amount: '' }
      this.showCreateOrder = false
      this.showPayment = false
      this.currentOrderId = ''
      this.paymentStatus = ''
    },
    // 清空查询结果
    clearQuery() {
      this.queryOrderId = ''
      this.queryResult = null
    }
  }
})
