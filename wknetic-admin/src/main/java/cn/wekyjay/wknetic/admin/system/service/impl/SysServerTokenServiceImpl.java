package cn.wekyjay.wknetic.admin.system.service.impl;

import cn.wekyjay.wknetic.admin.system.service.ISysServerTokenService;
import cn.wekyjay.wknetic.common.domain.SysServerToken;
import cn.wekyjay.wknetic.common.mapper.SysServerTokenMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;

/**
 * 服务器Token Service实现
 * 
 * @author WkNetic
 * @since 2026-02-03
 */
@Slf4j
@Service
public class SysServerTokenServiceImpl extends ServiceImpl<SysServerTokenMapper, SysServerToken> 
        implements ISysServerTokenService {

    @Override
    public Page<SysServerToken> getTokenPage(Page<SysServerToken> page, String name, Integer status) {
        LambdaQueryWrapper<SysServerToken> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(name)) {
            wrapper.like(SysServerToken::getName, name);
        }
        
        if (status != null) {
            wrapper.eq(SysServerToken::getStatus, status);
        }
        
        wrapper.orderByDesc(SysServerToken::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    public SysServerToken createToken(String name, String remark, String createBy) {
        // 生成UUID作为Token值
        String tokenValue = UUID.randomUUID().toString().replace("-", "");
        
        SysServerToken token = SysServerToken.builder()
                .name(name)
                .tokenValue(tokenValue)
                .status(1) // 默认启用
                .remark(remark)
                .createBy(createBy)
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        
        this.save(token);
        log.info("创建服务器Token: {} ({})", name, tokenValue);
        
        return token;
    }

    @Override
    public String regenerateToken(Long id) {
        SysServerToken token = this.getById(id);
        if (token == null) {
            throw new RuntimeException("Token不存在");
        }
        
        // 生成新的Token值
        String newTokenValue = UUID.randomUUID().toString().replace("-", "");
        
        LambdaUpdateWrapper<SysServerToken> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysServerToken::getId, id)
               .set(SysServerToken::getTokenValue, newTokenValue)
               .set(SysServerToken::getUpdateTime, new Date());
        
        this.update(wrapper);
        log.info("重新生成Token: {} -> {}", token.getName(), newTokenValue);
        
        return newTokenValue;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        LambdaUpdateWrapper<SysServerToken> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysServerToken::getId, id)
               .set(SysServerToken::getStatus, status)
               .set(SysServerToken::getUpdateTime, new Date());
        
        return this.update(wrapper);
    }
}
