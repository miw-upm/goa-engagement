package es.upm.api.domain.services.support;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class EncryptionService {
    private static final String PREFIX = "enc::";
    private static final byte[] PREFIX_BYTES = PREFIX.getBytes(StandardCharsets.UTF_8);

    private final BytesEncryptor bytesEncryptor;

    public byte[] encrypt(byte[] value) {
        if (value == null || value.length == 0 || this.isPrefixed(value)) {
            return value;
        }
        byte[] encrypted = this.bytesEncryptor.encrypt(value);
        return this.prependPrefix(encrypted);
    }

    public byte[] decrypt(byte[] value) {
        if (value == null || value.length == 0) {
            return value;
        }
        if (!this.isPrefixed(value)) {
            return value;
        }
        byte[] encryptedWithoutPrefix = this.removePrefix(value);
        return this.bytesEncryptor.decrypt(encryptedWithoutPrefix);
    }

    private boolean isPrefixed(byte[] value) {
        if (value == null || value.length < PREFIX_BYTES.length) {
            return false;
        }
        for (int i = 0; i < PREFIX_BYTES.length; i++) {
            if (value[i] != PREFIX_BYTES[i]) {
                return false;
            }
        }
        return true;
    }

    private byte[] prependPrefix(byte[] encrypted) {
        byte[] result = new byte[PREFIX_BYTES.length + encrypted.length];
        System.arraycopy(PREFIX_BYTES, 0, result, 0, PREFIX_BYTES.length);
        System.arraycopy(encrypted, 0, result, PREFIX_BYTES.length, encrypted.length);
        return result;
    }

    private byte[] removePrefix(byte[] value) {
        return Arrays.copyOfRange(value, PREFIX_BYTES.length, value.length);
    }
}
