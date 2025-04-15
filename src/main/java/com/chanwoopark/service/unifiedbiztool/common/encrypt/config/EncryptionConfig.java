package com.chanwoopark.service.unifiedbiztool.common.encrypt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Configuration
public class EncryptionConfig {
    @Bean
    public TextEncryptor textEncryptor(
            @Value("${app.token.encrypt.password}") String password,
            @Value("${app.token.encrypt.salt}") String salt) {
        return Encryptors.text(password, salt);
    }
}
