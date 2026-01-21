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
    SUCCESS(20000, "code.success"),
    SYSTEM_ERROR(50000, "code.system.error"),

    /* ================= 客户端错误 (40xxx) ================= */
    CLIENT_ERROR(40000, "code.client.error"),
    PARAM_VALID_ERROR(40001, "code.param.error"),
    
    /* ================= 认证与安全 (401xx - 403xx) ================= */
    unauthorized(40100, "code.unauthorized"),
    TOKEN_INVALID(40101, "code.token.invalid"),
    
    // 【关键】WkNetic 特有：API 接口签名错误 (针对 Mc 插件/第三方接入)
    SIGN_MISSING(40102, "code.sign.missing"),
    SIGN_INVALID(40103, "code.sign.invalid"),
    SIGN_EXPIRED(40104, "code.sign.expired"), // 防重放攻击

    FORBIDDEN(40300, "code.forbidden"),
    
    /* ================= 业务资源 (404xx) ================= */
    RESOURCE_NOT_FOUND(40400, "code.noresource"),
    USER_NOT_FOUND(40401, "code.user.not_found"),
    SERVER_NOT_FOUND(40402, "code.server.not_found"),

    /* ================= 核心业务异常 (5xxxx) ================= */
    // 【关键】Netty 与 游戏同步相关
    GAME_SYNC_TIMEOUT(50100, "code.game.sync_timeout"),
    GAME_SERVER_OFFLINE(50101, "code.game.offline"),
    GAME_CMD_FAILED(50102, "code.game.cmd_failed"),

    // Flowable 工作流相关
    FLOW_PROCESS_ERROR(50200, "code.flow.error"),
    FLOW_TASK_NOT_FOUND(50201, "code.flow.task_not_found");

    private final Integer code;
    private final String message;
}