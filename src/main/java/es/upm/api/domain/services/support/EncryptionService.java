package es.upm.api.domain.services.support;

import es.upm.miw.exception.BadRequestException;
import es.upm.miw.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class EncryptionService {
    public static final String PREFIX = "enc::";
    private static final byte[] PREFIX_BYTES = PREFIX.getBytes(StandardCharsets.UTF_8);
    private static final String PREFIX_BASE = PREFIX.substring(0, PREFIX.indexOf(':') + 1);
    private static final byte[] PREFIX_BASE_BYTES = PREFIX_BASE.getBytes(StandardCharsets.UTF_8);

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
        return this.bytesEncryptor.decrypt(this.removePrefix(value));
    }

    public String getPrefixAndFirst6DecryptedBase64(byte[] value) {
        if (value == null || value.length == 0) {
            return "";
        }
        int prefixLength = this.resolvePrefixLength(value);
        String prefix = new String(value, 0, prefixLength, StandardCharsets.UTF_8);
        byte[] encryptedPayload = Arrays.copyOfRange(value, prefixLength, value.length);
        byte[] decryptedPayload = this.bytesEncryptor.decrypt(encryptedPayload);
        String base64 = Base64.getEncoder().encodeToString(decryptedPayload);
        String first6 = base64.substring(0, Math.min(6, base64.length()));
        return prefix + first6 + "****";
    }

    private boolean isPrefixed(byte[] value) {
        return this.startsWith(value, PREFIX_BYTES);
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        if (value == null || prefix == null || value.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (value[i] != prefix[i]) {
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

    private int resolvePrefixLength(byte[] value) {
        if (!this.startsWith(value, PREFIX_BASE_BYTES)) {
            throw new ConflictException("Formato de prefijo de encriptación no soportado");
        }
        for (int i = PREFIX_BASE_BYTES.length; i < value.length; i++) {
            if (value[i] == ':') {
                return i + 1;
            }
        }
        throw new ConflictException("Formato de prefijo de encriptación no soportado");
    }
}
