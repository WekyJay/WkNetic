package cn.wekyjay.wknetic.socket.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Component
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    @Resource
    private GamePacketHandler gamePacketHandler;

    @Override
    protected void initChannel(SocketChannel ch) {
        // 1. 解决粘包/拆包 (必须与插件端的参数完全一致！)
        // maxFrameLength: Int.MAX, lengthFieldOffset: 0, lengthFieldLength: 4
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        
        // 2. 发送时自动添加长度头
        ch.pipeline().addLast(new LengthFieldPrepender(4));

        // 3. 字符串编解码
        ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
        ch.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));

        // 4. 业务逻辑
        ch.pipeline().addLast(gamePacketHandler);
    }
}