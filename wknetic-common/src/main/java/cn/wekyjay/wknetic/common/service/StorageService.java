package cn.wekyjay.wknetic.common.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件存储服务接口
 * 支持多种存储后端：本地存储、MinIO、图床等
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
public interface StorageService {
    
    /**
     * 上传文件
     *
     * @param file 文件
     * @param path 存储路径（相对路径）
     * @return 文件访问URL
     * @throws Exception 上传失败时抛出异常
     */
    String upload(MultipartFile file, String path) throws Exception;
    
    /**
     * 上传文件流
     *
     * @param inputStream 文件流
     * @param fileName 文件名
     * @param path 存储路径
     * @return 文件访问URL
     * @throws Exception 上传失败时抛出异常
     */
    String upload(InputStream inputStream, String fileName, String path) throws Exception;
    
    /**
     * 删除文件
     *
     * @param fileUrl 文件URL或路径
     * @return true=删除成功
     */
    boolean delete(String fileUrl);
    
    /**
     * 获取文件访问URL
     *
     * @param path 文件路径
     * @return 完整的访问URL
     */
    String getFileUrl(String path);
    
    /**
     * 检查文件是否存在
     *
     * @param path 文件路径
     * @return true=文件存在
     */
    boolean exists(String path);
}
