package cn.wekyjay.wknetic.common.exception;

import cn.wekyjay.wknetic.common.enums.ResultCode;
import cn.wekyjay.wknetic.common.model.IResultCode;
import lombok.Getter;

/**
 * 自定义业务异常
 * 用于在 Service 层中断逻辑，并返回指定的错误码
 */
@Getter
public class BusinessException extends RuntimeException {
    
    // 对应的错误码枚举
    private final IResultCode resultCode;

    // 允许覆盖默认的错误消息
    public BusinessException(IResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public BusinessException(IResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
    
    // 默认抛出系统错误
    public BusinessException(String message) {
        super(message);
        this.resultCode = ResultCode.SYSTEM_ERROR;
    }
}