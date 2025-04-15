package com.chanwoopark.service.unifiedbiztool.common.encrypt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenEncryptor {

    private final TextEncryptor delegate;

    public String encrypt(String plain) {
        return delegate.encrypt(plain);
    }

    public String decrypt(String encrypted) {
        return delegate.decrypt(encrypted);
    }
}
