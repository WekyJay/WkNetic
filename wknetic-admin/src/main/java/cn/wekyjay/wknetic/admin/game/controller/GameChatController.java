package cn.wekyjay.wknetic.admin.game.controller;

import cn.wekyjay.wknetic.admin.game.service.GameChatControllerService;
import cn.wekyjay.wknetic.admin.game.service.GameChatService;
import cn.wekyjay.wknetic.auth.utils.SecurityUtils;
import cn.wekyjay.wknetic.common.model.Result;
import cn.wekyjay.wknetic.common.model.dto.ChatHistoryDTO;
import cn.wekyjay.wknetic.common.model.dto.SendChatMessageDTO;
import cn.wekyjay.wknetic.common.model.vo.ChatMessageVO;
import cn.wekyjay.wknetic.common.model.vo.ServerStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 游戏聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/game/chat")
@RequiredArgsConstructor
@Tag(name = "游戏聊天", description = "游戏聊天相关接口")
public class GameChatController {
    
    private final GameChatService gameChatService;
    private final GameChatControllerService gameChatControllerService;
    
    @GetMapping("/history")
    @Operation(summary = "获取聊天历史")
    public Result<List<ChatMessageVO>> getChatHistory(@Validated ChatHistoryDTO dto) {
        List<ChatMessageVO> messages = gameChatService.getChatHistory(dto);
        return Result.success(messages);
    }
    
    @PostMapping("/send")
    @Operation(summary = "发送聊天消息")
    public Result<Void> sendMessage(@Validated @RequestBody SendChatMessageDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean success = gameChatService.sendChatMessage(dto, userId);
        
        if (success) {
            return Result.success();
        } else {
            return Result.error("发送消息失败，请确保已绑定Minecraft账号");
        }
    }
    
    @GetMapping("/servers")
    @Operation(summary = "获取可用服务器列表")
    public Result<List<ServerInfo>> getServers() {
        List<ServerStatusVO> serverStatuses = gameChatControllerService.getServers();
        List<ServerInfo> servers = serverStatuses.stream()
            .map(status -> new ServerInfo(
                status.getSessionId(),
                status.getServerName(),
                status.getOnlinePlayers() != null ? status.getOnlinePlayers() : 0
            ))
            .toList();
        return Result.success(servers);
    }
    
    @GetMapping("/channels")
    @Operation(summary = "获取可用频道列表")
    public Result<List<ChannelInfo>> getChannels() {
        List<Map<String, String>> channelMaps = gameChatControllerService.getChannels();
        List<ChannelInfo> channels = channelMaps.stream()
            .map(map -> new ChannelInfo(map.get("id"), map.get("name")))
            .toList();
        return Result.success(channels);
    }
    
    @GetMapping("/worlds")
    @Operation(summary = "获取世界列表")
    public Result<List<WorldInfo>> getWorlds() {
        List<Map<String, String>> worldMaps = gameChatControllerService.getWorlds();
        List<WorldInfo> worlds = worldMaps.stream()
            .map(map -> new WorldInfo(map.get("id"), map.get("name")))
            .toList();
        return Result.success(worlds);
    }
    
    @Data
    public static class ServerInfo {
        private String id;
        private String name;
        private int players;
        
        public ServerInfo(String id, String name, int players) {
            this.id = id;
            this.name = name;
            this.players = players;
        }
    }
    
    @Data
    public static class ChannelInfo {
        private String id;
        private String name;
        
        public ChannelInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
    
    @Data
    public static class WorldInfo {
        private String id;
        private String name;
        
        public WorldInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
