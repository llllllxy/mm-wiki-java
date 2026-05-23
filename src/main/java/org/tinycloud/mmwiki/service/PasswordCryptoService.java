package org.tinycloud.mmwiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.mmwiki.config.MmwikiProperties;
import org.tinycloud.mmwiki.exception.SystemException;
import org.tinycloud.mmwiki.util.RSAUtils;

/**
 * 前端密码密文解密服务。
 */
@Service
public class PasswordCryptoService {

    @Autowired
    private MmwikiProperties properties;

    /**
     * 使用配置中的 RSA 私钥解密前端提交的密码密文。
     */
    public String decryptPassword(String encryptedPassword) {
        if (!StringUtils.hasText(encryptedPassword)) {
            throw new SystemException("密码不能为空。");
        }
        String privateKey = properties.getSecurity().getRsa().getPrivateKey();
        if (!StringUtils.hasText(privateKey)) {
            throw new SystemException("密码解密配置缺失。");
        }
        try {
            return RSAUtils.decryptByPrivateKey(privateKey, encryptedPassword.trim());
        } catch (Exception ex) {
            throw new SystemException("密码解密失败。");
        }
    }
}
