package cn.wekyjay.wknetic.admin.storage;

import cn.wekyjay.wknetic.common.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地文件存储实现
 * 将文件存储到服务器本地磁盘
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageServiceImpl implements StorageService {
    
    @Value("${storage.local.base-path:./data/uploads}")
    private String basePath;
    
    @Value("${storage.local.base-url:http://localhost:8080/uploads}")
    private String baseUrl;
    
    @Override
    public String upload(MultipartFile file, String path) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        // 生成存储路径：basePath/path/yyyy-MM-dd/uuid.ext
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String extension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + extension;
        String relativePath = path + File.separator + datePath + File.separator + fileName;
        
        // 创建目录
        Path fullPath = Paths.get(basePath, relativePath);
        Files.createDirectories(fullPath.getParent());
        
        // 保存文件
        file.transferTo(fullPath.toFile());
        
        log.info("文件上传成功: {}", fullPath);
        
        // 返回访问URL
        return getFileUrl(relativePath);
    }
    
    @Override
    public String upload(InputStream inputStream, String fileName, String path) throws Exception {
        if (inputStream == null) {
            throw new IllegalArgumentException("文件流不能为空");
        }
        
        // 生成存储路径
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String extension = getFileExtension(fileName);
        String newFileName = UUID.randomUUID().toString() + extension;
        String relativePath = path + File.separator + datePath + File.separator + newFileName;
        
        // 创建目录
        Path fullPath = Paths.get(basePath, relativePath);
        Files.createDirectories(fullPath.getParent());
        
        // 保存文件
        Files.copy(inputStream, fullPath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("文件流上传成功: {}", fullPath);
        
        return getFileUrl(relativePath);
    }
    
    @Override
    public boolean delete(String fileUrl) {
        try {
            // 从URL中提取相对路径
            String relativePath = fileUrl.replace(baseUrl, "");
            Path fullPath = Paths.get(basePath, relativePath);
            
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                log.info("文件删除成功: {}", fullPath);
                return true;
            }
            
            log.warn("文件不存在: {}", fullPath);
            return false;
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
            return false;
        }
    }
    
    @Override
    public String getFileUrl(String path) {
        // 统一使用正斜杠
        String normalizedPath = path.replace(File.separator, "/");
        return baseUrl + "/" + normalizedPath;
    }
    
    @Override
    public boolean exists(String path) {
        Path fullPath = Paths.get(basePath, path);
        return Files.exists(fullPath);
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }
}
