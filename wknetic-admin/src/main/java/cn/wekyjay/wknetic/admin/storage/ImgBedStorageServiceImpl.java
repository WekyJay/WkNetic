package cn.wekyjay.wknetic.admin.storage;

import cn.wekyjay.wknetic.common.service.StorageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Base64;

/**
 * 图床存储实现
 * 将图片上传到第三方图床服务（如SM.MS、ImgBB等）
 * 
 * @author WkNetic
 * @since 2026-02-01
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "imgbed")
public class ImgBedStorageServiceImpl implements StorageService {
    
    @Value("${storage.imgbed.api-url:https://sm.ms/api/v2/upload}")
    private String apiUrl;
    
    @Value("${storage.imgbed.api-key:}")
    private String apiKey;
    
    @Value("${storage.imgbed.type:smms}")
    private String imgBedType;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String upload(MultipartFile file, String path) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 检查是否为图片
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("图床只支持图片文件");
        }
        
        return uploadToImgBed(file.getBytes(), file.getOriginalFilename());
    }
    
    @Override
    public String upload(InputStream inputStream, String fileName, String path) throws Exception {
        if (inputStream == null) {
            throw new IllegalArgumentException("文件流不能为空");
        }
        
        byte[] bytes = inputStream.readAllBytes();
        return uploadToImgBed(bytes, fileName);
    }
    
    /**
     * 上传到图床
     */
    private String uploadToImgBed(byte[] fileBytes, String fileName) throws Exception {
        if ("smms".equals(imgBedType)) {
            return uploadToSmMs(fileBytes, fileName);
        } else if ("imgbb".equals(imgBedType)) {
            return uploadToImgBB(fileBytes, fileName);
        } else {
            throw new UnsupportedOperationException("不支持的图床类型: " + imgBedType);
        }
    }
    
    /**
     * 上传到SM.MS图床
     */
    private String uploadToSmMs(byte[] fileBytes, String fileName) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("Authorization", apiKey);
        }
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("smfile", new org.springframework.core.io.ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.get("success").asBoolean()) {
                String url = jsonNode.get("data").get("url").asText();
                log.info("文件上传到SM.MS成功: {}", url);
                return url;
            } else {
                String message = jsonNode.get("message").asText();
                throw new RuntimeException("SM.MS上传失败: " + message);
            }
        } else {
            throw new RuntimeException("SM.MS上传失败，HTTP状态: " + response.getStatusCode());
        }
    }
    
    /**
     * 上传到ImgBB图床
     */
    private String uploadToImgBB(byte[] fileBytes, String fileName) throws Exception {
        String base64Image = Base64.getEncoder().encodeToString(fileBytes);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        String requestBody = "key=" + apiKey + "&image=" + base64Image;
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.get("success").asBoolean()) {
                String url = jsonNode.get("data").get("url").asText();
                log.info("文件上传到ImgBB成功: {}", url);
                return url;
            } else {
                throw new RuntimeException("ImgBB上传失败");
            }
        } else {
            throw new RuntimeException("ImgBB上传失败，HTTP状态: " + response.getStatusCode());
        }
    }
    
    @Override
    public boolean delete(String fileUrl) {
        log.warn("图床服务不支持删除操作: {}", fileUrl);
        // 大多数免费图床不支持通过API删除文件
        return false;
    }
    
    @Override
    public String getFileUrl(String path) {
        // 图床直接返回完整URL
        return path;
    }
    
    @Override
    public boolean exists(String path) {
        // 图床无法检查文件是否存在
        return false;
    }
}
