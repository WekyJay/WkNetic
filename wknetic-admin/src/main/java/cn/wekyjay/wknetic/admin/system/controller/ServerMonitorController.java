package cn.wekyjay.wknetic.admin.system.controller;

import cn.wekyjay.wknetic.api.enums.PacketType;
import cn.wekyjay.wknetic.api.model.packet.AdminCommandPacket;
import cn.wekyjay.wknetic.common.model.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.*;

/**
 * 服务器监控控制器
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/server-monitor")
@RequiredArgsConstructor
@Tag(name = "服务器监控", description = "游戏服务器实时监控和管理")
public class ServerMonitorController {

    private final StringRedisTemplate stringRedisTemplate;

    public static final String ADMIN_COMMAND_TOPIC = "wknetic:admin:command";

    @PostMapping("/command")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "发送管理命令", description = "向游戏服务器发送管理命令")
    public Result<Void> sendCommand(@Valid @RequestBody SendCommandRequest request) {
        try {
            AdminCommandPacket command = new AdminCommandPacket();

            command.setSessionId(request.getSessionId());
            command.setCommandType(request.getCommandType());
            command.setTargetPlayer(request.getTargetPlayer());
            command.setCommand(request.getCommand());
            command.setReason(request.getReason());
            command.setCommandId(UUID.randomUUID().toString());
              
            stringRedisTemplate.convertAndSend(ADMIN_COMMAND_TOPIC, command.toJsonString());

            log.info("发送管理命令: {} [sessionId: {}]", request.getCommandType(), request.getSessionId());
            return Result.success();
        } catch (Exception e) {
            log.error("发送命令失败", e);
            return Result.error("发送命令失败: " + e.getMessage());
        }
    }

    @Data
    public static class SendCommandRequest {
        @NotBlank(message = "服务器SessionId不能为空")
        private String sessionId;

        @NotBlank(message = "命令类型不能为空")
        private String commandType; // KICK, BAN, COMMAND, MESSAGE

        private String targetPlayer;
        private String command;
        private String reason;
    }
}
