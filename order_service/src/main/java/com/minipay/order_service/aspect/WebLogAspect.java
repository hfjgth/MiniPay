package com.minipay.order_service.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class WebLogAspect {

    // 切点：拦截 order_service 包下所有 Controller 的所有方法
    @Pointcut("execution(public * com.minipay.order_service.controller..*.*(..))")
    public void webLogPointcut() {}

    @Around("webLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        // 获取当前请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 1. 请求进入时打日志
        log.info("接口请求开始：{} {}", method, uri);

        try {
            // 执行原接口方法
            Object result = joinPoint.proceed();
            // 2. 请求正常结束，打印耗时
            log.info("接口请求结束：{} {}, 耗时={}ms", method, uri, System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            // 3. 接口抛异常，打印异常+耗时，保留堆栈
            log.error("接口请求异常：{} {}, 耗时={}ms", method, uri, System.currentTimeMillis() - startTime, e);
            throw e; // 继续抛出异常，交给全局异常处理器
        }
    }
}