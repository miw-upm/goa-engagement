package es.upm.api.domain.services.support;

import es.upm.miw.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EncryptionService {
    private static final char SEPARATOR = ':';
    public static final String PREFIX_BASE = "enc" + SEPARATOR;
    public static final String PREFIX = PREFIX_BASE + SEPARATOR;
    private static final byte[] PREFIX_BASE_BYTES = PREFIX_BASE.getBytes(StandardCharsets.UTF_8);
    private static final byte[] PREFIX_BYTES = PREFIX.getBytes(StandardCharsets.UTF_8);

    private final BytesEncryptor bytesEncryptor;

    public byte[] encrypt(byte[] value) {
        if (value == null || value.length == 0) {
            throw new ConflictException("Se esperaba un valor para encriptar no vacio");
        }
        if (this.startsWith(value, PREFIX_BYTES)) {
            throw new ConflictException("El valor ya esta encriptado");
        }
        byte[] encrypted = this.bytesEncryptor.encrypt(value);
        byte[] prependPrefix = new byte[PREFIX_BYTES.length + encrypted.length];
        System.arraycopy(PREFIX_BYTES, 0, prependPrefix, 0, PREFIX_BYTES.length);
        System.arraycopy(encrypted, 0, prependPrefix, PREFIX_BYTES.length, encrypted.length);
        return prependPrefix;
    }

    public byte[] decrypt(byte[] value) {
        if (!this.startsWith(value, PREFIX_BYTES)) {
            throw new ConflictException("Se esperaba un valor encriptado con prefijo valido");
        }
        byte[] valueWithoutPrefix = Arrays.copyOfRange(value, PREFIX_BYTES.length, value.length);
        return this.bytesEncryptor.decrypt(valueWithoutPrefix);
    }

    public String buildPreview(byte[] value) {
        String prefix = this.buildPrefix(value);
        if (prefix.isEmpty()) {
            return "";
        }
        int prefixLength = prefix.length();
        byte[] encryptedPayload = Arrays.copyOfRange(value, prefixLength, value.length);
        byte[] decryptedPayload = this.bytesEncryptor.decrypt(encryptedPayload);
        int headLength = Math.min(5, decryptedPayload.length);
        String headBase64 = Base64.getEncoder().encodeToString(Arrays.copyOf(decryptedPayload, headLength));
        String first6 = headBase64.substring(0, Math.min(6, headBase64.length()));
        return prefix + first6 + "****";
    }

    public String buildPrefix(byte[] value) {
        if (value == null || value.length == 0) {
            throw new ConflictException("Encrypt without prefix");
        }
        int prefixLength = this.resolvePrefixLength(value);
        return new String(value, 0, prefixLength, StandardCharsets.UTF_8);
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        for (int i = 0; i < prefix.length; i++) {
            if (value[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private int resolvePrefixLength(byte[] value) {
        if (!this.startsWith(value, PREFIX_BASE_BYTES)) {
            throw new ConflictException("Formato de prefijo de encriptacion no soportado");
        }
        for (int i = PREFIX_BASE_BYTES.length; i < value.length; i++) {
            if (value[i] == ':') {
                return i + 1;
            }
        }
        throw new ConflictException("Formato de prefijo de encriptacion no soportado");
    }

}
