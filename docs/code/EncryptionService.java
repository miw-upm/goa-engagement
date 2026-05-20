@Service
@RequiredArgsConstructor
public class EncryptionService {
    private static final byte[] LEGACY_PREFIX = "enc::".getBytes(StandardCharsets.UTF_8);

    @Qualifier("bytesEncryptor")
    private final BytesEncryptor bytesEncryptor;

    @Qualifier("legacyBytesEncryptor")
    private final Optional<BytesEncryptor> legacyBytesEncryptor;

    // sin prefijo => actual
    // con "enc::" => legacy
}
