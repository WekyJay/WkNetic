package cn.wekyjay.wknetic.admin.handler;

import cn.wekyjay.wknetic.common.exception.BusinessException;
import cn.wekyjay.wknetic.common.enums.ResultCode;
import cn.wekyjay.wknetic.common.model.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Objects;

/**
 * 全局异常拦截器
 * 作用：将系统中的各种异常（参数校验失败、业务中断、空指针等）统一转换为标准的 JSON Result 返回
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. 拦截 @Valid / @Validated 参数校验异常 (JSON Body 传参)
     * 场景：前端传来的 JSON 缺少必填字段，或者格式不对
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 从异常中提取出 BindingResult
        BindingResult bindingResult = e.getBindingResult();
        // 获取第一个校验失败的错误提示 (例如: "密码不能为空")
        String msg = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();
        
        log.warn("参数校验失败 (JSON): {}", msg);
        // 返回 40001 状态码
        return Result.error(ResultCode.PARAM_VALID_ERROR, msg);
    }

    /**
     * 2. 拦截 GET 请求参数绑定异常 (Query Param 传参)
     * 场景：/api/user?age=abc (类型不匹配)
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String msg = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        log.warn("参数校验失败 (Query): {}", msg);
        return Result.error(ResultCode.PARAM_VALID_ERROR, msg);
    }

    /**
     * 3. 拦截自定义业务异常
     * 场景：Service 层手动 throw new BusinessException("余额不足");
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务中断 [{}]: {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getResultCode(), e.getMessage());
    }

    /**
     * 4. 拦截 404 异常 (Spring Boot 3 特性)
     * 场景：前端访问了一个不存在的 API 路径
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        log.warn("路径不存在: {} {}", request.getMethod(), request.getRequestURI());
        return Result.error(ResultCode.RESOURCE_NOT_FOUND);
    }

    /**
     * 5. 拦截请求体读取异常
     * 场景：POST 请求没传 Body，或者 Body 格式错误的 JSON
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体读取失败: {}", e.getMessage());
        return Result.error(ResultCode.PARAM_VALID_ERROR, "请求体不能为空或格式错误");
    }

    /**
     * 99. 兜底策略：拦截所有未知的系统异常
     * 场景：空指针 (NPE)、数据库连接失败、索引越界等
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统未知异常 [{}]: ", request.getRequestURI(), e);
        // 生产环境通常只返回 "系统繁忙"，不返回具体堆栈信息
        return Result.error(ResultCode.SYSTEM_ERROR, "系统出了点小差错，请稍后再试");
    }
}