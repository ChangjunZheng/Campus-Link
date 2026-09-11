package com.campuslink.module.account.infrastructure.codec;

import com.campuslink.common.crypto.CryptoService;
import com.campuslink.module.account.domain.gateway.SensitiveCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 适配器：将通用加密组件（AES-GCM + HMAC，common/crypto）适配为领域端口 */
@Component
@RequiredArgsConstructor
public class AesGcmHmacCodec implements SensitiveCodec {

    private final CryptoService cryptoService;

    @Override
    public String hash(String plain) {
        return cryptoService.hash(plain);
    }

    @Override
    public String encrypt(String plain) {
        return cryptoService.encrypt(plain);
    }

    @Override
    public String decrypt(String encoded) {
        return cryptoService.decrypt(encoded);
    }
}
