package cn.wekyjay.wknetic.socket.handler;

import cn.wekyjay.wknetic.socket.manager.ChannelManager;
// 移除 fastjson 的 import
// import com.alibaba.fastjson2.JSON;
// import com.alibaba.fastjson2.JSONObject;

// 引入 Jackson
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Slf4j
@Component
@ChannelHandler.Sharable
public class GamePacketHandler extends SimpleChannelInboundHandler<String> {

    @Resource
    private ChannelManager channelManager;
    
    // ✅ 直接注入 Spring Boot 自带的 ObjectMapper
    @Resource
    private ObjectMapper objectMapper;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        log.info("RECV: {}", msg);

        try {
            // ✅ Jackson 解析方式：readTree
            // 类似于 Fastjson 的 JSONObject，Jackson 用 JsonNode
            JsonNode json = objectMapper.readTree(msg);
            
            // 安全取值
            if (!json.has("type")) return;
            int type = json.get("type").asInt();

            switch (type) {
                case 1: 
                    handleLogin(ctx, json);
                    break;
                // ... 其他 case
            }
        } catch (Exception e) {
            log.error("Packet parse error", e);
        }
    }

    private void handleLogin(ChannelHandlerContext ctx, JsonNode json) {
        // Jackson 取字符串: node.get("key").asText()
        String token = json.has("token") ? json.get("token").asText() : "";

        // ... 验证逻辑不变 ...
        boolean isValid = true; 

        if (isValid) {
            channelManager.addChannel(token, ctx.channel());
            // 发送响应
            sendJson(ctx, 100, "Login Success");
        }
    }

    /**
     * 发送响应消息
     * @param ctx
     * @param code
     * @param msg
     */
    private void sendJson(ChannelHandlerContext ctx, int code, String msg) {
        // ✅ Jackson 构造 JSON：createObjectNode
        ObjectNode resp = objectMapper.createObjectNode();
        resp.put("code", code);
        resp.put("msg", msg);
        
        // 转为字符串发送
        ctx.writeAndFlush(resp.toString());
    }
}