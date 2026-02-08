package cn.wekyjay.wknetic.api.utils;

import cn.wekyjay.wknetic.api.model.packet.BasePacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Packet序列化工具类
 * 提供Packet与JSON之间的转换功能
 * 使用Gson作为JSON库，兼容Minecraft插件环境
 */
public class PacketUtils {
    
    private static final Gson gson = createGson();
    
    private PacketUtils() {
        // 工具类，禁止实例化
    }
    
    /**
     * 创建配置好的Gson实例
     */
    private static Gson createGson() {
        return new GsonBuilder()
            .setPrettyPrinting() // 美化输出（用于调试）
            .serializeNulls()    // 序列化null值
            .disableHtmlEscaping() // 禁用HTML转义
            .create();
    }
    
    /**
     * 将Packet对象序列化为JSON字符串
     * @param packet 要序列化的Packet
     * @return JSON字符串
     * @throws IOException 如果序列化失败
     */
    public static String toJson(BasePacket packet) throws IOException {
        if (packet == null) {
            throw new IllegalArgumentException("Packet不能为空");
        }
        
        try {
            return gson.toJson(packet);
        } catch (Exception e) {
            throw new IOException("Packet序列化失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将Packet对象序列化为JSON字符串（安全版本，不抛出异常）
     * @param packet 要序列化的Packet
     * @return JSON字符串，如果序列化失败返回null
     */
    public static String toJsonSafe(BasePacket packet) {
        try {
            return toJson(packet);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 将Packet对象序列化为字节数组
     * @param packet 要序列化的Packet
     * @return 字节数组
     * @throws IOException 如果序列化失败
     */
    public static byte[] toBytes(BasePacket packet) throws IOException {
        String json = toJson(packet);
        return json.getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * 将Packet对象序列化为字节数组（安全版本，不抛出异常）
     * @param packet 要序列化的Packet
     * @return 字节数组，如果序列化失败返回null
     */
    public static byte[] toBytesSafe(BasePacket packet) {
        try {
            return toBytes(packet);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 将JSON字符串反序列化为Packet对象
     * @param json JSON字符串
     * @param packetClass Packet类
     * @param <T> Packet类型
     * @return Packet对象
     * @throws IOException 如果反序列化失败
     */
    public static <T extends BasePacket> T fromJson(String json, Class<T> packetClass) throws IOException {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON字符串不能为空");
        }
        
        if (packetClass == null) {
            throw new IllegalArgumentException("Packet类不能为空");
        }
        
        try {
            return gson.fromJson(json, packetClass);
        } catch (JsonSyntaxException e) {
            throw new IOException("JSON反序列化失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将JSON字符串反序列化为Packet对象（安全版本，不抛出异常）
     * @param json JSON字符串
     * @param packetClass Packet类
     * @param <T> Packet类型
     * @return Packet对象，如果反序列化失败返回null
     */
    public static <T extends BasePacket> T fromJsonSafe(String json, Class<T> packetClass) {
        try {
            return fromJson(json, packetClass);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 将字节数组反序列化为Packet对象
     * @param bytes 字节数组
     * @param packetClass Packet类
     * @param <T> Packet类型
     * @return Packet对象
     * @throws IOException 如果反序列化失败
     */
    public static <T extends BasePacket> T fromBytes(byte[] bytes, Class<T> packetClass) throws IOException {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("字节数组不能为空");
        }
        
        String json = new String(bytes, StandardCharsets.UTF_8);
        return fromJson(json, packetClass);
    }
    
    /**
     * 将字节数组反序列化为Packet对象（安全版本，不抛出异常）
     * @param bytes 字节数组
     * @param packetClass Packet类
     * @param <T> Packet类型
     * @return Packet对象，如果反序列化失败返回null
     */
    public static <T extends BasePacket> T fromBytesSafe(byte[] bytes, Class<T> packetClass) {
        try {
            return fromBytes(bytes, packetClass);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 将Packet对象转换为美化格式的JSON字符串（用于日志输出）
     * @param packet 要转换的Packet
     * @return 美化格式的JSON字符串
     */
    public static String toPrettyJson(BasePacket packet) {
        if (packet == null) {
            return "null";
        }
        
        try {
            // 使用配置了美化输出的Gson
            Gson prettyGson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .disableHtmlEscaping()
                .create();
            return prettyGson.toJson(packet);
        } catch (Exception e) {
            return "序列化失败: " + e.getMessage();
        }
    }
    
    /**
     * 创建Packet的深拷贝
     * @param packet 要拷贝的Packet
     * @param packetClass Packet类
     * @param <T> Packet类型
     * @return 深拷贝的Packet对象
     * @throws IOException 如果拷贝失败
     */
    public static <T extends BasePacket> T deepCopy(T packet, Class<T> packetClass) throws IOException {
        if (packet == null) {
            return null;
        }
        
        String json = toJson(packet);
        return fromJson(json, packetClass);
    }
    
    /**
     * 创建Packet的深拷贝（安全版本，不抛出异常）
     * @param packet 要拷贝的Packet
     * @param packetClass Packet类
     * @param <T> Packet类型
     * @return 深拷贝的Packet对象，如果拷贝失败返回null
     */
    public static <T extends BasePacket> T deepCopySafe(T packet, Class<T> packetClass) {
        try {
            return deepCopy(packet, packetClass);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 验证JSON字符串是否为有效的Packet格式
     * @param json JSON字符串
     * @param packetClass Packet类
     * @param <T> Packet类型
     * @return 如果JSON有效返回true，否则返回false
     */
    public static <T extends BasePacket> boolean isValidJson(String json, Class<T> packetClass) {
        try {
            T packet = fromJson(json, packetClass);
            return PacketValidator.isValid(packet);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取Gson实例（用于高级操作）
     * @return Gson实例
     */
    public static Gson getGson() {
        return gson;
    }
    
    /**
     * 创建自定义配置的Gson实例
     * @param builder GsonBuilder配置
     * @return 自定义Gson实例
     */
    public static Gson createCustomGson(GsonBuilder builder) {
        if (builder == null) {
            throw new IllegalArgumentException("GsonBuilder不能为空");
        }
        return builder.create();
    }
}
