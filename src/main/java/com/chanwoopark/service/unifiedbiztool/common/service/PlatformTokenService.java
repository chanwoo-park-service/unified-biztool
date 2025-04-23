package com.chanwoopark.service.unifiedbiztool.common.service;

import com.chanwoopark.service.unifiedbiztool.common.encrypt.TokenEncryptor;
import com.chanwoopark.service.unifiedbiztool.common.exception.TokenNotFoundException;
import com.chanwoopark.service.unifiedbiztool.common.model.entity.PlatformToken;
import com.chanwoopark.service.unifiedbiztool.common.model.enums.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PlatformTokenService {

    private final TokenEncryptor tokenEncryptor;

    private final MessageSource messageSource;

    private final RedisTemplate<String, PlatformToken> platformTokenRedisTemplate;

    @Transactional
    public void saveOrUpdateToken(Platform platform, String accessToken) {
        platformTokenRedisTemplate.opsForValue().set(
                String.valueOf(platform),
                PlatformToken
                        .builder()
                        .accessToken(tokenEncryptor.encrypt(accessToken))
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public String getToken(Platform platform) {
        PlatformToken token = platformTokenRedisTemplate.opsForValue().get(platform.name());
        if (token == null) {
            throw new TokenNotFoundException(
                    messageSource.getMessage(
                            "platform.token.not-found",
                            new Object[]{platform},
                            LocaleContextHolder.getLocale()
                    )
            );
        }
        return tokenEncryptor.decrypt(token.getAccessToken());
    }
}
