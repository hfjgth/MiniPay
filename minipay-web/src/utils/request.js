import axios from 'axios'
import { ElMessage } from 'element-plus'
// 导入错误码与错误文案常量
import { ERROR_CODE, ERROR_MSG } from '@/constants/error'

// 创建 axios 实例
const service = axios.create({
    // 后端网关基础地址，根据实际服务地址修改
    baseURL: 'http://localhost:8080',
    // 超时时间 5 秒
    timeout: 5000,
    // 统一请求头
    headers: {
        'Content-Type': 'application/json;charset=UTF-8'
    }
})

/**
 * 请求拦截器
 */
service.interceptors.request.use((config) => {
    // 记录请求日志，便于调试与问题排查
    console.log(`[Request] ${config.method?.toUpperCase()} ${config.url}`, config.params || config.data || '')
    return config
}, (error) => {
    console.error('[Request Error]', error)
    return Promise.reject(error)
})

/**
 * 响应拦截器
 * 对接文档返回格式：{ code, message, data }
 */
service.interceptors.response.use((response) => {
    // 统一处理响应数据格式、错误码等
    const { data: resp } = response
    const { code, message, data } = resp
    // 成功状态码，直接返回数据
    if (code === ERROR_CODE.SUCCESS) {
        return data
    }
    // 非成功状态码，统一拦截提示
    switch (code) {
        case ERROR_CODE.PARAM_ERROR:
            ElMessage.error(ERROR_MSG[ERROR_CODE.PARAM_ERROR])
            break
        case ERROR_CODE.ORDER_NOT_EXIST:
            ElMessage.error(ERROR_MSG[ERROR_CODE.ORDER_NOT_EXIST])
            break
        case ERROR_CODE.STATUS_FORBID:
            ElMessage.error(ERROR_MSG[ERROR_CODE.STATUS_FORBID])
            break
        case ERROR_CODE.AMOUNT_MISMATCH:
            ElMessage.error(ERROR_MSG[ERROR_CODE.AMOUNT_MISMATCH])
            break
        case ERROR_CODE.PAY_SERVICE_ERR:
            ElMessage.error(ERROR_MSG[ERROR_CODE.PAY_SERVICE_ERR])
            break
        case ERROR_CODE.SYSTEM_ERROR:
            ElMessage.error(ERROR_MSG[ERROR_CODE.SYSTEM_ERROR])
            break
        default:
            ElMessage.error(message || 'unknown api error')
            break
    }
    // 所有异常统一 reject，让业务页面捕获
    return Promise.reject(resp)
}, (error) => {
    // 网络错误或服务器异常统一记录
    console.error('[Response Error]', error.message || error)
    return Promise.reject(error)
}
)

export default service