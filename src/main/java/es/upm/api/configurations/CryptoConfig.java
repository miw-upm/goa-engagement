package es.upm.api.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;

@Configuration
public class CryptoConfig {

    @Bean
    public BytesEncryptor bytesEncryptor(
            @Value("${app.crypto.password}") String password,
            @Value("${app.crypto.salt}") String salt
    ) {
        return Encryptors.standard(password, salt);
    }
}
