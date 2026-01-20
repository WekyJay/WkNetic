package cn.wekyjay.wknetic.common.enums;

import cn.wekyjay.wknetic.common.model.IResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description: 包含了 HTTP 标准状态，为未来项目预埋错误码。
 * @Title: 响应码枚举
 * @author WekyJay
 * @Github: <a href="https://github.com/WekyJay">https://github.com/WekyJay</a>
 * @Date: 2026/1/20 10:32
 */
@Getter
@AllArgsConstructor
public enum ResultCode implements IResultCode {

    /* ================= 基础状态 ================= */
    SUCCESS(20000, "操作成功"),
    SYSTEM_ERROR(50000, "系统繁忙，请稍后重试"),

    /* ================= 客户端错误 (40xxx) ================= */
    CLIENT_ERROR(40000, "客户端请求参数错误"),
    PARAM_VALID_ERROR(40001, "参数校验失败"),
    
    /* ================= 认证与安全 (401xx - 403xx) ================= */
    unauthorized(40100, "尚未登录或Token已失效"),
    TOKEN_INVALID(40101, "Token无效或已过期"),
    
    // 【关键】WkNetic 特有：API 接口签名错误 (针对 Mc 插件/第三方接入)
    SIGN_MISSING(40102, "缺少签名参数"),
    SIGN_INVALID(40103, "API签名验证失败"),
    SIGN_EXPIRED(40104, "API请求时间戳已过期"), // 防重放攻击

    FORBIDDEN(40300, "您没有权限执行此操作"),
    
    /* ================= 业务资源 (404xx) ================= */
    RESOURCE_NOT_FOUND(40400, "请求的资源不存在"),
    USER_NOT_FOUND(40401, "用户不存在"),
    SERVER_NOT_FOUND(40402, "未找到指定的Minecraft服务器"),

    /* ================= 核心业务异常 (5xxxx) ================= */
    // 【关键】Netty 与 游戏同步相关
    GAME_SYNC_TIMEOUT(50100, "游戏服务器响应超时"),
    GAME_SERVER_OFFLINE(50101, "游戏服务器未连接到云端"),
    GAME_CMD_FAILED(50102, "指令发送失败"),

    // Flowable 工作流相关
    FLOW_PROCESS_ERROR(50200, "流程执行异常"),
    FLOW_TASK_NOT_FOUND(50201, "当前任务不存在或已完结");

    private final Integer code;
    private final String message;
}