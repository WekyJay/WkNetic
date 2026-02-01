package cn.wekyjay.wknetic.admin.storage;

import cn.wekyjay.wknetic.common.service.StorageService;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * MinIO对象存储实现
 * 将文件存储到MinIO服务器
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "minio")
public class MinioStorageServiceImpl implements StorageService {
    
    @Value("${storage.minio.endpoint:http://localhost:9000}")
    private String endpoint;
    
    @Value("${storage.minio.access-key}")
    private String accessKey;
    
    @Value("${storage.minio.secret-key}")
    private String secretKey;
    
    @Value("${storage.minio.bucket-name:wknetic}")
    private String bucketName;
    
    private MinioClient minioClient;
    
    /**
     * 初始化MinIO客户端
     */
    private MinioClient getMinioClient() {
        if (minioClient == null) {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            
            // 检查并创建bucket
            try {
                boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build());
                
                if (!exists) {
                    minioClient.makeBucket(MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build());
                    log.info("创建MinIO Bucket: {}", bucketName);
                }
            } catch (Exception e) {
                log.error("MinIO初始化失败", e);
            }
        }
        return minioClient;
    }
    
    @Override
    public String upload(MultipartFile file, String path) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        return upload(file.getInputStream(), originalFilename, path);
    }
    
    @Override
    public String upload(InputStream inputStream, String fileName, String path) throws Exception {
        if (inputStream == null) {
            throw new IllegalArgumentException("文件流不能为空");
        }
        
        // 生成对象名：path/yyyy-MM-dd/uuid.ext
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String extension = getFileExtension(fileName);
        String objectName = path + "/" + datePath + "/" + UUID.randomUUID() + extension;
        
        // 上传文件
        getMinioClient().putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, -1, 10485760) // -1表示未知大小，最大10MB
                        .contentType(getContentType(fileName))
                        .build()
        );
        
        log.info("文件上传到MinIO成功: {}/{}", bucketName, objectName);
        
        return getFileUrl(objectName);
    }
    
    @Override
    public boolean delete(String fileUrl) {
        try {
            // 从URL中提取对象名
            String objectName = extractObjectName(fileUrl);
            
            getMinioClient().removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            
            log.info("文件从MinIO删除成功: {}", objectName);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
            return false;
        }
    }
    
    @Override
    public String getFileUrl(String path) {
        // 返回MinIO的访问URL
        return endpoint + "/" + bucketName + "/" + path;
    }
    
    @Override
    public boolean exists(String path) {
        try {
            getMinioClient().statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
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
    
    /**
     * 根据文件名获取Content-Type
     */
    private String getContentType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return switch (extension) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            case ".pdf" -> "application/pdf";
            case ".zip" -> "application/zip";
            default -> "application/octet-stream";
        };
    }
    
    /**
     * 从URL中提取对象名
     */
    private String extractObjectName(String fileUrl) {
        // 假设URL格式：http://localhost:9000/bucket/path/file.ext
        String prefix = endpoint + "/" + bucketName + "/";
        if (fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        }
        return fileUrl;
    }
}
