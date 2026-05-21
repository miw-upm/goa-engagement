package es.upm.api.domain.services.support;

import es.upm.miw.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
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
        if (this.startsWith(value, PREFIX_BYTES)) {
            throw new ConflictException("Value is already encrypted");
        }
        byte[] encrypted = this.bytesEncryptor.encrypt(value);
        return ByteBuffer.allocate(PREFIX_BYTES.length + encrypted.length)
                .put(PREFIX_BYTES)
                .put(encrypted)
                .array();
    }

    public byte[] decrypt(byte[] value) {
        if (!this.startsWith(value, PREFIX_BYTES)) {
            throw new ConflictException("Expected an encrypted value with a valid prefix");
        }
        byte[] valueWithoutPrefix = Arrays.copyOfRange(value, PREFIX_BYTES.length, value.length);
        return this.bytesEncryptor.decrypt(valueWithoutPrefix);
    }

    public String extractPreview(byte[] value) {
        String prefix = this.extractAllPrefix(value);
        int prefixLength = prefix.length();
        byte[] previewBytes = Arrays.copyOfRange(value, prefixLength, prefixLength + PREVIEW_CHARS);
        String previewChars = Base64.getEncoder().encodeToString(previewBytes).substring(0, PREVIEW_CHARS);
        return prefix + previewChars + PREVIEW_TRUNCATION_MARK;
    }

    public String extractAllPrefix(byte[] value) {
        if (!this.startsWith(value, PREFIX_BASE_BYTES)) {
            throw new ConflictException("Unsupported encryption prefix format");
        }
        for (int i = PREFIX_BASE_BYTES.length; i < value.length; i++) {
            if (value[i] == SEPARATOR) {
                return new String(value, 0, i + 1, StandardCharsets.UTF_8);
            }
        }
        throw new ConflictException("Malformed encryption prefix - missing separator");
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (value[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
