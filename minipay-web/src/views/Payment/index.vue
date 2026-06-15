<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
// 引入Pinia状态
import { usePaymentStore } from '@/store/payment'
// 引入接口
import { createOrder, payOrder, getOrderDetail } from '@/api/order/order-api'
// 引入常量
import { ORDER_STATUS_TEXT, ORDER_STATUS_TAG_TYPE } from '@/constants/order'
import { PAY_METHOD, PAY_METHOD_TEXT } from '@/constants/pay'
import { ERROR_MSG } from '@/constants/error'

import { storeToRefs } from 'pinia'

// 实例化Store
const paymentStore = usePaymentStore()
const {
    showCreateOrder,
    showPayment,
    orderForm,
    paymentForm,
    currentOrderId,
    paymentStatus,
    queryOrderId,
    queryResult
} = storeToRefs(paymentStore)

// 表单引用和校验规则
const orderFormRef = ref(null)
const orderRules = {
    orderNo: [{ required: true, message: '请输入订单号', trigger: 'blur' }],
    amount: [
        { required: true, message: '请输入订单金额', trigger: 'blur' },
        { type: 'number', message: '金额必须是数字', trigger: 'blur', transform: (value) => Number(value) }
    ]
}

// 支付方式选项（取自常量）
const paymentMethods = [
    { value: PAY_METHOD.BALANCE, label: PAY_METHOD_TEXT[PAY_METHOD.BALANCE] },
    { value: PAY_METHOD.CARD, label: PAY_METHOD_TEXT[PAY_METHOD.CARD] }
]

// 格式化金额
const formatAmount = (amount) => {
    return Number(amount).toFixed(2)
}

// 显示订单输入面板
const handleCreateOrder = () => {
    paymentStore.showCreateOrder = true
    paymentStore.orderForm = { orderNo: '', amount: '', description: '' }
    // 重置支付状态，隐藏状态显示区域
    paymentStore.paymentStatus = ''
    paymentStore.currentOrderId = ''
    paymentStore.showPayment = false
}

// 打开支付面板 + 校验表单
const handlePay = async () => {
    if (!orderFormRef.value) return
    try {
        await orderFormRef.value.validate()
    } catch (err) {
        return
    }

    paymentForm.value.amount = orderForm.value.amount
    paymentForm.value.payMethod = ''
    paymentStore.showPayment = true
}

// 关闭支付面板
const handleClosePayment = () => {
    ElMessageBox.confirm('确定要退出支付吗？', '提示', {
        confirmButtonText: '是',
        cancelButtonText: '否',
        type: 'warning'
    }).then(() => {
        paymentStore.showPayment = false
        ElMessage.info('已退出支付')
    }).catch(() => { })
}

// 确认支付（核心接口调用）
const handleConfirmPayment = async () => {
    if (!paymentForm.value.payMethod) {
        ElMessage.warning('请选择支付方式')
        return
    }

    try {
        // 1. 创建订单
        const orderRes = await createOrder({
            orderNo: orderForm.value.orderNo,
            amount: Number(orderForm.value.amount),
            description: orderForm.value.description
        })
        const orderId = orderRes.orderId
        paymentStore.currentOrderId = orderId

        // 2. 发起支付
        const payRes = await payOrder(orderId, {
            payMethod: paymentForm.value.payMethod,
            amount: Number(paymentForm.value.amount)
        })
        // 状态转换：支付服务返回 SUCCESS/FAIL，转换为订单服务的 PAID/FAILED
        const statusMap = { 'SUCCESS': 'PAID', 'FAIL': 'FAILED' }
        paymentStore.paymentStatus = statusMap[payRes.status] || payRes.status

        ElMessage.success('支付请求已提交')
        paymentStore.showPayment = false
        paymentStore.showCreateOrder = false
    } catch (err) {
        const msg = err.message || ERROR_MSG[err.code] || '支付失败，请重试'
        ElMessage.error(msg)
        console.error('支付异常：', err)
        // 支付异常时设置订单状态为FAILED
        paymentStore.paymentStatus = 'FAILED'
        paymentStore.showPayment = false
        paymentStore.showCreateOrder = false
    }
}

// 查询订单
const handleQueryPayment = async () => {
    if (!queryOrderId.value) {
        ElMessage.warning('请输入订单ID')
        return
    }
    try {
        const res = await getOrderDetail(queryOrderId.value)
        const data = res
        paymentStore.queryResult = {
            orderId: data.orderId,
            orderNo: data.orderNo,
            amount: data.amount,
            description: data.description,
            payMethod: data.payId,
            status: data.status,
            statusText: ORDER_STATUS_TEXT[data.status],
            tagType: ORDER_STATUS_TAG_TYPE[data.status]
        }
        ElMessage.success('查询成功')
    } catch (err) {
        const msg = err.message || ERROR_MSG[err.code] || '查询失败，请确认订单ID'
        ElMessage.error(msg)
        paymentStore.queryResult = null
        console.error('查询异常：', err)
    }
}

// 重置订单模块
const resetCreateOrder = () => {
    paymentStore.resetCreateOrder()
}
</script>

<template>
    <div class="payment-container">
        <el-card class="main-card">
            <template #header>
                <div class="card-header">
                    <span>支付系统</span>
                </div>
            </template>

            <el-tabs type="card">
                <!-- 创建订单 Tab -->
                <el-tab-pane label="创建订单">
                    <div class="tab-content">
                        <el-button type="primary" @click="handleCreateOrder" size="large">
                            创建订单
                        </el-button>

                        <el-card v-if="showCreateOrder && !showPayment" class="order-card" shadow="hover">
                            <template #header>
                                <div class="card-header">
                                    <span>填写订单信息</span>
                                    <!-- <el-button type="danger" :icon="Close" circle size="small" @click="resetCreateOrder"
                                        class="close-btn" /> -->
                                    <el-button type="default" @click="resetCreateOrder">返回</el-button>
                                </div>
                            </template>
                            <el-form ref="orderFormRef" :model="orderForm" :rules="orderRules" label-width="100px">
                                <el-form-item label="订单号" prop="orderNo">
                                    <el-input v-model="orderForm.orderNo" placeholder="请输入订单编号" />
                                </el-form-item>
                                <el-form-item label="订单金额" prop="amount">
                                    <el-input v-model="orderForm.amount" type="number" step="0.01"
                                        placeholder="请输入金额" />
                                </el-form-item>
                                <el-form-item label="订单描述">
                                    <el-input v-model="orderForm.description" placeholder="请输入订单描述" />
                                </el-form-item>
                            </el-form>
                            <div class="card-footer">
                                <el-button type="success" @click="handlePay">确认并支付</el-button>
                            </div>
                        </el-card>

                        <el-card v-if="showPayment" class="payment-card" shadow="hover">
                            <template #header>
                                <div class="card-header">
                                    <span>选择支付方式</span>
                                    <!-- <el-button type="danger" :icon="Close" circle size="small"
                                        @click="handleClosePayment" class="close-btn" /> -->
                                    <el-button type="default" @click="handleClosePayment">返回</el-button>
                                </div>
                            </template>
                            <el-form :model="paymentForm" label-width="100px">
                                <el-form-item label="支付方式">
                                    <el-select v-model="paymentForm.payMethod" placeholder="请选择支付方式" style="width:100%">
                                        <el-option v-for="item in paymentMethods" :key="item.value" :label="item.label"
                                            :value="item.value" />
                                    </el-select>
                                </el-form-item>
                                <el-form-item label="支付金额">
                                    <el-input v-model="paymentForm.amount" disabled />
                                </el-form-item>
                            </el-form>
                            <div class="card-footer">
                                <el-button type="primary" @click="handleConfirmPayment">确认支付</el-button>
                            </div>
                        </el-card>

                        <div v-if="currentOrderId" class="status-display">
                            <el-alert :title="`订单ID：${currentOrderId} 支付状态：${paymentStatus}`"
                                :type="paymentStatus === 'PAID' || paymentStatus === 'SUCCESS' ? 'success' : (paymentStatus === 'FAILED' ? 'error' : 'info')"
                                show-icon :closable="false" />
                        </div>
                    </div>
                </el-tab-pane>

                <!-- 查询结果 Tab -->
                <el-tab-pane label="查询支付结果">
                    <div class="tab-content">
                        <div class="query-section">
                            <el-input v-model="queryOrderId" placeholder="请输入订单ID"
                                style="width: 300px; margin-right: 10px" />
                            <el-button type="primary" @click="handleQueryPayment">查询</el-button>
                        </div>

                        <el-card v-if="queryResult" class="result-card" shadow="hover">
                            <template #header>
                                <span>查询结果</span>
                            </template>
                            <el-descriptions :column="1" border>
                                <el-descriptions-item label="订单ID">{{ queryResult.orderId }}</el-descriptions-item>
                                <el-descriptions-item label="外部订单号">{{ queryResult.orderNo }}</el-descriptions-item>
                                <el-descriptions-item label="订单金额">¥{{ formatAmount(queryResult.amount)
                                    }}</el-descriptions-item>
                                <!-- <el-descriptions-item label="订单描述">{{ queryResult.description }}</el-descriptions-item> -->
                                <el-descriptions-item label="支付流水号">{{ queryResult.payMethod || '暂无'
                                    }}</el-descriptions-item>
                                <el-descriptions-item label="订单状态"><el-tag :type="queryResult.tagType">{{
                                        queryResult.statusText
                                        }}</el-tag></el-descriptions-item>
                            </el-descriptions>
                        </el-card>

                        <el-empty v-if="!queryResult" description="请输入订单ID查询支付结果" />
                    </div>
                </el-tab-pane>
            </el-tabs>
        </el-card>
    </div>
</template>

<style scoped>
.payment-container {
    min-height: 90vh;
    background: #e8ecf1;
    padding: 40px 20px;
    display: flex;
    justify-content: center;
    align-items: flex-start;
}

.main-card {
    width: 100%;
    max-width: 700px;
    border-radius: 12px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
}

.card-header span {
    font-size: 18px;
    font-weight: 600;
}

.tab-content {
    padding: 20px 0;
}

.order-card,
.payment-card {
    margin-top: 20px;
    border-radius: 8px;
}

.close-btn {
    position: absolute;
    top: 10px;
    right: 10px;
}

.card-footer {
    display: flex;
    justify-content: flex-end;
    padding-top: 10px;
}

.status-display {
    margin-top: 20px;
}

.query-section {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
}

.result-card {
    margin-top: 20px;
}
</style>