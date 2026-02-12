package cn.wekyjay.wknetic.admin.system.service;

import cn.wekyjay.wknetic.common.model.vo.ServerStatusVO;

import java.util.List;
import java.util.Map;

/**
 * GameChatController相关服务接口
 * 负责获取服务器状态、频道列表和世界列表等实时数据
 */
public interface GameChatControllerService {
    
    /**
     * 获取服务器列表
     * 从Redis获取实时服务器状态信息
     * @return 服务器状态列表
     */
    List<ServerStatusVO> getServers();
    
    /**
     * 获取频道列表
     * 从配置或数据库获取可用的聊天频道
     * @return 频道列表，每个频道包含id和name
     */
    List<Map<String, String>> getChannels();
    
    /**
     * 获取世界列表
     * 从配置或数据库获取可用的游戏世界
     * @return 世界列表，每个世界包含id和name
     */
    List<Map<String, String>> getWorlds();
    
}