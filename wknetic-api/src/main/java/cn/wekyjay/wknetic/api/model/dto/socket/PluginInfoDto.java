package cn.wekyjay.wknetic.api.model.dto.socket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 插件信息DTO
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginInfoDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 插件名称
     */
    private String name;

    /**
     * 插件版本
     */
    private String version;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 插件作者
     */
    private String author;

    /**
     * 插件描述
     */
    private String description;
}
