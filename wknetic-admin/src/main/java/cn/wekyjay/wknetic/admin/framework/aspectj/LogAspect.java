package cn.wekyjay.wknetic.admin.framework.aspectj;

import cn.wekyjay.wknetic.admin.common.annotation.Log;
import cn.wekyjay.wknetic.admin.system.domain.SysOperLog;
import cn.wekyjay.wknetic.admin.system.service.ISysOperLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Aspect
@Component
@Slf4j
public class LogAspect {

    // 日志服务
    @Resource
    private ISysOperLogService operLogService;

    // JSON 转换器
    @Resource
    private ObjectMapper objectMapper;


    // 成功返回通知
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    // 异常通知
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    // 处理日志
    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {
            // 1. 【主线程】提取 Request 数据
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = (attributes != null) ? attributes.getRequest() : null;

            SysOperLog operLog = new SysOperLog();
            operLog.setStatus(0);
            operLog.setOperTime(new Date());

            // 设定请求相关数据
            if (request != null) {
                operLog.setOperIp(request.getRemoteAddr());
                operLog.setOperUrl(request.getRequestURI());
                operLog.setRequestMethod(request.getMethod());
            }

            // 设定异常信息
            if (e != null) {
                operLog.setStatus(1);
                operLog.setErrorMsg(e.getMessage());
            }

            // 设定方法名称和标题
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");
            operLog.setTitle(controllerLog.title()); // 注解上的标题
            operLog.setBusinessType(controllerLog.businessType().ordinal());

            if (controllerLog.isSaveRequestData()) {
                operLog.setOperParam(argsArrayToString(joinPoint.getArgs()));
            }
            if (controllerLog.isSaveResponseData() && jsonResult != null) {
                try {
                    operLog.setJsonResult(objectMapper.writeValueAsString(jsonResult));
                } catch (Exception ex) { }
            }

            // 2. 【虚拟线程】异步入库
            Thread.ofVirtual().name("virtual-log-saver").start(() -> {
                try {
                    operLogService.insertOperLog(operLog);
                } catch (Exception ex) {
                    log.error("日志入库失败: {}", ex.getMessage());
                }
            });

        } catch (Exception exp) {
            log.error("日志切面异常: {}", exp.getMessage());
        }
    }

    private String argsArrayToString(Object[] paramsArray) {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                if (o != null && !isFilterObject(o)) {
                    try {
                        params.append(objectMapper.writeValueAsString(o)).append(" ");
                    } catch (Exception e) { }
                }
            }
        }
        return params.toString().trim();
    }

    public boolean isFilterObject(final Object o) {
        return o instanceof HttpServletRequest || o instanceof HttpServletResponse;
    }
}