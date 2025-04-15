package com.chanwoopark.service.unifiedbiztool.common.service;

import com.chanwoopark.service.unifiedbiztool.common.encrypt.TokenEncryptor;
import com.chanwoopark.service.unifiedbiztool.common.exception.TokenNotFoundException;
import com.chanwoopark.service.unifiedbiztool.common.model.entity.PlatformToken;
import com.chanwoopark.service.unifiedbiztool.common.model.enums.Platform;
import com.chanwoopark.service.unifiedbiztool.common.repository.PlatformTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PlatformTokenService {

    private final PlatformTokenRepository platformTokenRepository;

    private final TokenEncryptor tokenEncryptor;

    private final MessageSource messageSource;

    @Transactional
    public void saveOrUpdateToken(Platform platform, String accessToken) {
        PlatformToken token = platformTokenRepository.findByPlatform(platform)
                .map(existing -> {
                    existing.setAccessToken(tokenEncryptor.encrypt(accessToken));
                    return existing;
                })
                .orElseGet(() -> PlatformToken.builder()
                        .platform(platform)
                        .accessToken(tokenEncryptor.encrypt(accessToken))
                        .build());
        platformTokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public String getToken(Platform platform) {
        return platformTokenRepository.findByPlatform(platform)
                .map(token -> tokenEncryptor.decrypt(token.getAccessToken()))
                .orElseThrow(() -> new TokenNotFoundException(
                        messageSource.getMessage(
                                "platform.token.not-found",
                                new Object[]{platform},
                                LocaleContextHolder.getLocale()
                        )
                ));
    }
}
