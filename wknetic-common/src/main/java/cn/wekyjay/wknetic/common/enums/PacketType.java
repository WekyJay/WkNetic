package cn.wekyjay.wknetic.common.enums;

public enum PacketType {
    // 基础连接类
    HEARTBEAT(0),
    AUTH_REQUEST(1),
    
    // 业务类
    CHAT_MSG(10),
    PRIVATE_MSG(11),
    POST_ARTICLE(12);

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