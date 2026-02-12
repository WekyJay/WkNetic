package cn.wekyjay.wknetic.admin.system.service;

import cn.wekyjay.wknetic.common.model.vo.ChatMessageVO;
import cn.wekyjay.wknetic.common.model.dto.ChatHistoryDTO;
import cn.wekyjay.wknetic.common.model.dto.SendChatMessageDTO;

import java.util.List;

/**
 * 游戏聊天服务接口
 */
public interface GameChatService {
    
    /**
     * 获取聊天历史记录
     * @param dto 查询条件
     * @return 消息列表
     */
    List<ChatMessageVO> getChatHistory(ChatHistoryDTO dto);
    
    /**
     * 发送聊天消息
     * @param dto 消息内容
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean sendChatMessage(SendChatMessageDTO dto, Long userId);
    
    /**
     * 保存聊天消息到Redis
     * @param message 消息对象
     */
    void saveChatMessage(ChatMessageVO message);
    
    /**
     * 发布聊天消息到Redis
     * @param message 消息对象
     */
    void publishChatMessage(ChatMessageVO message);
}
