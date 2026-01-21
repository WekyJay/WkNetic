package cn.wekyjay.wknetic.common.model;

import cn.wekyjay.wknetic.common.enums.ResultCode;
import cn.wekyjay.wknetic.common.util.MessageUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 业务状态码 (非 HTTP 状态码)
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 业务数据
     */
    private T data;

    /**
     * 响应时间戳
     */
    private long timestamp;

    /**
     * 私有构造，强制使用静态方法
     */
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /* ================= 成功响应 ================= */

    public static <T> Result<T> success() {
        return success(null);
    }






    public static <T> Result<T> success(T data) {
        // 自动去翻译工具类拿文字
        String msg = MessageUtils.get(ResultCode.SUCCESS.getMessage());
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data);
    }
    
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /* ================= 失败响应 ================= */

    /**
     * 直接使用枚举返回错误
     */
    public static <T> Result<T> error(IResultCode resultCode) {
        // 自动翻译错误信息
        String msg = MessageUtils.get(resultCode.getMessage());
        return new Result<>(resultCode.getCode(), msg, null);
    }

    /**
     * 使用枚举 + 自定义消息 (例如：参数校验失败，具体是哪个参数)
     */
    public static <T> Result<T> error(IResultCode resultCode, String customMessage) {
        return new Result<>(resultCode.getCode(), customMessage, null);
    }

    /**
     * 任意错误
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 快速判断是否成功 (供内部 Feign/RPC 调用判断)
     */
    public boolean isSuccess() {
        return this.code != null && this.code.equals(ResultCode.SUCCESS.getCode());
    }
}