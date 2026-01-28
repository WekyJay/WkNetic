package cn.wekyjay.wknetic.auth.service;

import cn.wekyjay.wknetic.common.model.dto.LoginBody;

public interface AuthService {
    /**
     * 登录并返回 Token
     */
    String login(LoginBody loginBody);
}