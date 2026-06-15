package com.minipay.order_service.exception;

import com.minipay.order_service.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.error(101, message);
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntime(RuntimeException e) {
        log.error("业务异常", e);
        String msg = e.getMessage();
        if (msg.contains("订单不存在")) {
            return Result.error(102, msg);
        } else if (msg.contains("订单状态不允许")) {
            return Result.error(103, msg);
        } else if (msg.contains("支付金额与订单金额不匹配")) {
            return Result.error(201, msg);
        } else if (msg.contains("支付服务异常")) {      // 新增
            return Result.error(202, msg);
        }
        return Result.error(500, msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("系统错误", e);
        return Result.error(500, "系统内部错误");
    }
}