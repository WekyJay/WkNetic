package cn.wekyjay.wknetic.admin.storage;

import cn.wekyjay.wknetic.common.model.Result;
import cn.wekyjay.wknetic.common.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传Controller
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Slf4j
@Tag(name = "文件上传", description = "文件上传相关接口")
@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {
    
    private final StorageService storageService;
    
    // 允许的图片格式
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    // 允许的文件格式
    private static final List<String> ALLOWED_FILE_TYPES = List.of(
            "application/pdf", "application/zip", "application/x-rar-compressed"
    );
    
    // 最大文件大小（10MB）
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    /**
     * 上传图片（用于帖子内容）
     */
    @Operation(summary = "上传图片")
    @PostMapping("/image")
    @PreAuthorize("hasRole('USER')")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            validateFile(file, ALLOWED_IMAGE_TYPES, MAX_FILE_SIZE);
            
            // 上传文件到 images 目录
            String url = storageService.upload(file, "images");
            
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("fileName", file.getOriginalFilename());
            
            log.info("图片上传成功: {}", url);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.error(500, "图片上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量上传图片
     */
    @Operation(summary = "批量上传图片")
    @PostMapping("/images")
    @PreAuthorize("hasRole('USER')")
    public Result<List<Map<String, String>>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        List<Map<String, String>> results = new ArrayList<>();
        
        try {
            for (MultipartFile file : files) {
                // 验证文件
                validateFile(file, ALLOWED_IMAGE_TYPES, MAX_FILE_SIZE);
                
                // 上传文件
                String url = storageService.upload(file, "images");
                
                Map<String, String> result = new HashMap<>();
                result.put("url", url);
                result.put("fileName", file.getOriginalFilename());
                
                results.add(result);
            }
            
            log.info("批量上传图片成功，共{}张", results.size());
            return Result.success(results);
            
        } catch (Exception e) {
            log.error("批量上传图片失败", e);
            return Result.error(500, "批量上传图片失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传附件（用于帖子附件）
     */
    @Operation(summary = "上传附件")
    @PostMapping("/file")
    @PreAuthorize("hasRole('USER')")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            List<String> allowedTypes = new ArrayList<>(ALLOWED_IMAGE_TYPES);
            allowedTypes.addAll(ALLOWED_FILE_TYPES);
            validateFile(file, allowedTypes, MAX_FILE_SIZE);
            
            // 上传文件到 files 目录
            String url = storageService.upload(file, "files");
            
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("fileName", file.getOriginalFilename());
            result.put("size", String.valueOf(file.getSize()));
            
            log.info("附件上传成功: {}", url);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("附件上传失败", e);
            return Result.error(500, "附件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传头像
     */
    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    @PreAuthorize("hasRole('USER')")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            validateFile(file, ALLOWED_IMAGE_TYPES, 2 * 1024 * 1024); // 头像限制2MB
            
            // 上传文件到 avatars 目录
            String url = storageService.upload(file, "avatars");
            
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            
            log.info("头像上传成功: {}", url);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.error(500, "头像上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除文件
     */
    @Operation(summary = "删除文件")
    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    public Result<Void> deleteFile(@RequestParam("url") String fileUrl) {
        try {
            boolean deleted = storageService.delete(fileUrl);
            if (deleted) {
                log.info("文件删除成功: {}", fileUrl);
                return Result.success();
            } else {
                return Result.error(500, "文件删除失败");
            }
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.error(500, "文件删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file, List<String> allowedTypes, long maxSize) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型: " + contentType);
        }
        
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制: " + (maxSize / 1024 / 1024) + "MB");
        }
    }
}
