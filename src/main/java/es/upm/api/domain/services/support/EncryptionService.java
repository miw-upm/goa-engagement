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

    private static final int PREVIEW_CHARS = 6;
    private static final String PREVIEW_TRUNCATION_MARK = "****";

    private final BytesEncryptor bytesEncryptor;

    public byte[] encrypt(byte[] value) {
        if (value == null || value.length == 0) {
            throw new ConflictException("Expected a non-empty value to encrypt");
        }
        if (this.startsWith(value, PREFIX_BYTES)) {
            throw new ConflictException("Value is already encrypted");
        }
        byte[] encrypted = this.bytesEncryptor.encrypt(value);
        byte[] prefixedEncrypted  = new byte[PREFIX_BYTES.length + encrypted.length];
        System.arraycopy(PREFIX_BYTES, 0, prefixedEncrypted , 0, PREFIX_BYTES.length);
        System.arraycopy(encrypted, 0, prefixedEncrypted , PREFIX_BYTES.length, encrypted.length);
        return prefixedEncrypted ;
    }

    public byte[] decrypt(byte[] value) {
        if (!this.startsWith(value, PREFIX_BYTES)) {
            throw new ConflictException("Expected an encrypted value with a valid prefix");
        }
        byte[] valueWithoutPrefix = Arrays.copyOfRange(value, PREFIX_BYTES.length, value.length);
        return this.bytesEncryptor.decrypt(valueWithoutPrefix);
    }

    public String extractPreview(byte[] value) {
        if (value == null || value.length == 0) {
            throw new ConflictException("Expected an encrypted value to build preview");
        }
        String prefix = this.extractAllPrefix(value);
        int prefixLength = prefix.length();
        byte[] encryptedPayload = Arrays.copyOfRange(value, prefixLength, value.length);
        String previewChars = Base64.getEncoder()
                .encodeToString(Arrays.copyOf(encryptedPayload, PREVIEW_CHARS)).substring(0, PREVIEW_CHARS);
        return prefix + previewChars + PREVIEW_TRUNCATION_MARK;
    }

    public String extractAllPrefix(byte[] value) {
        if (value == null || value.length == 0) {
            throw new ConflictException("Expected an encrypted value with a prefix");
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
            throw new ConflictException("Unsupported encryption prefix format");
        }
        for (int i = PREFIX_BASE_BYTES.length; i < value.length; i++) {
            if (value[i] == SEPARATOR) {
                return i + 1;
            }
        }
        throw new ConflictException("Malformed encryption prefix - missing separator");
    }
}
