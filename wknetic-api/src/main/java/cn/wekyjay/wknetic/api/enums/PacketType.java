package cn.wekyjay.wknetic.api.enums;

import lombok.ToString;

@ToString
public enum PacketType {
    // 基础连接/鉴权 (0-9)
    HEARTBEAT              (0),   // 心跳保活
    AUTH_REQUEST           (1),   // 鉴权请求
    HANDSHAKE              (2),   // 首次握手/版本协商
    DISCONNECT             (3),   // 主动断开/注销
    SESSION_INVALID        (4),   // 会话失效通知
    RECONNECT_REQUEST      (5),   // 断线重连请求
    RECONNECT_SUCCESS      (6),   // 重连成功回执
    CLIENT_PING            (7),   // 客户端 ping
    SERVER_PONG            (8),   // 服务端 pong

    // 聊天/社交 (10-29)
    CHAT_MSG               (10),  // 公共频道聊天
    PRIVATE_MSG            (11),  // 私聊
    POST_ARTICLE           (12),  // 广场/帖子发布
    GROUP_CHAT             (13),  // 群组/频道聊天
    SYSTEM_BROADCAST       (14),  // 系统公告广播
    CHAT_HISTORY_PULL      (15),  // 拉取聊天记录
    CHAT_HISTORY_PUSH      (16),  // 推送聊天记录
    CHAT_MUTE_NOTICE       (17),  // 禁言/解禁通知
    EMOTE                  (18),  // 表情/动作

    // 好友/关系 (30-49)
    FRIEND_APPLY           (30),  // 好友申请
    FRIEND_APPLY_RESULT    (31),  // 好友申请结果
    FRIEND_DELETE          (32),  // 删除好友
    FRIEND_STATUS_UPDATE   (33),  // 上下线状态
    BLOCK_USER             (34),  // 拉黑
    UNBLOCK_USER           (35),  // 取消拉黑

    // 公会/团队 (50-69)
    GUILD_CREATE           (50),  // 创建公会
    GUILD_INVITE           (51),  // 邀请加入
    GUILD_APPLY            (52),  // 申请加入
    GUILD_KICK             (53),  // 踢出成员
    GUILD_QUIT             (54),  // 退出公会
    GUILD_INFO_UPDATE      (55),  // 公会信息更新
    GUILD_CHAT             (56),  // 公会聊天

    // 匹配/战斗 (70-89)
    MATCH_QUEUE_ENTER      (70),  // 进入匹配
    MATCH_QUEUE_CANCEL     (71),  // 取消匹配
    MATCH_FOUND            (72),  // 匹配成功
    MATCH_TIMEOUT          (73),  // 匹配超时
    BATTLE_START           (74),  // 对局开始
    BATTLE_ACTION          (75),  // 对局内操作帧
    BATTLE_SYNC_STATE      (76),  // 状态同步
    BATTLE_END             (77),  // 对局结算
    BATTLE_RECONNECT       (78),  // 战局重连
    BATTLE_PENALTY         (79),  // 逃跑/惩罚

    // 邮件/通知 (90-109)
    MAIL_LIST_REQUEST      (90),  // 请求邮件列表
    MAIL_READ              (91),  // 标记已读
    MAIL_REWARD_CLAIM      (92),  // 领取附件
    MAIL_DELETE            (93),  // 删除邮件
    NOTIFY_POPUP           (94),  // 即时通知弹窗

    // 任务/成就 (110-129)
    QUEST_UPDATE           (110), // 任务进度更新
    QUEST_REWARD           (111), // 任务奖励发放
    ACHIEVEMENT_UNLOCK     (112), // 成就解锁
    SEASON_PASS_PROGRESS   (113), // 通行证进度

    // 经济/交易 (130-149)
    INVENTORY_SYNC         (130), // 背包同步
    ITEM_USE               (131), // 道具使用
    ITEM_REWARD            (132), // 道具奖励发放
    TRADE_REQUEST          (133), // 玩家交易请求
    TRADE_RESULT           (134), // 交易结果
    AUCTION_BID            (135), // 拍卖行出价

    // 世界/活动 (150-169)
    WORLD_EVENT_TRIGGER    (150), // 世界事件触发
    WORLD_EVENT_REWARD     (151), // 世界事件奖励
    DUNGEON_ENTER          (152), // 进入副本/关卡
    DUNGEON_RESULT         (153), // 副本结算
    RESOURCE_POINT_UPDATE  (154), // 资源点刷新

    // 组队/房间 (170-189)
    PARTY_CREATE           (170), // 创建队伍/房间
    PARTY_INVITE           (171), // 邀请入队
    PARTY_READY            (172), // 准备状态
    PARTY_START            (173), // 队伍开局
    PARTY_LEAVE            (174), // 离队/解散

    // 观战/回放 (190-199)
    SPECTATE_REQUEST       (190), // 请求观战
    SPECTATE_END           (191), // 结束观战
    REPLAY_REQUEST         (192), // 请求回放数据
    REPLAY_DATA            (193), // 回放数据流

    // 安全/风控 (200-209)
    ANTI_CHEAT_ALERT       (200), // 反作弊告警
    BAN_NOTICE             (201), // 封禁/处罚通知
    CLIENT_LOG_UPLOAD      (202), // 客户端日志上报

    // 游戏服务器管理 (210-229)
    SERVER_LOGIN           (210), // 游戏服务器登录（携带Token）
    SERVER_LOGIN_RESP      (211), // 登录响应
    SERVER_HEARTBEAT       (212), // 游戏服务器心跳
    SERVER_INFO            (213), // 服务器信息（MOTD、玩家、插件等）
    ADMIN_COMMAND          (214), // 管理员下发命令
    ADMIN_COMMAND_RESP     (215); // 命令执行结果

    private final int id;
    PacketType(int id) { this.id = id; }
    public int getId() { return id; }

    // 根据 ID 获取枚举，用于解码
    public static PacketType getById(int id) {
        for (PacketType type : values()) {
            if (type.id == id) return type;
        }
        return null;
    }
}
