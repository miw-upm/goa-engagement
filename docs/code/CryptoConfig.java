@Configuration
public class CryptoConfig {

    @Bean("bytesEncryptor")
    public BytesEncryptor bytesEncryptor(
            @Value("${app.crypto.password}") String password,
            @Value("${app.crypto.salt}") String salt
    ) {
        return Encryptors.standard(password, salt);
    }

    @Bean("legacyBytesEncryptor")
    @ConditionalOnProperty(prefix = "app.crypto", name = "previous-password")
    public BytesEncryptor legacyBytesEncryptor(
            @Value("${app.crypto.previous-password}") String password,
            @Value("${app.crypto.previous-salt}") String salt
    ) {
        return Encryptors.standard(password, salt);
    }
}
