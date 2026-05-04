package es.upm.api.configurations;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.form.ContentType;
import feign.form.FormData;
import feign.form.MultipartFormContentProcessor;
import feign.form.spring.SpringFormEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.JsonFormWriter;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Type;

@Configuration
@RequiredArgsConstructor
public class FeignConfig {

    private final TokenManager tokenManager;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                String tokenValue = jwtAuth.getToken().getTokenValue();
                template.header("Authorization", "Bearer " + tokenValue);
            } else {
                template.header("Authorization", "Bearer " + tokenManager.getToken());
            }
        };
    }

    @Bean
    public JsonFormWriter jsonFormWriter() {
        return new JsonFormWriter() {
            @Override
            public boolean isApplicable(Object value) {
                return value != null
                        && super.isApplicable(value)
                        && !(value instanceof FormData)
                        && !(value instanceof byte[])
                        && !(value instanceof MultipartFile)
                        && value.getClass().getPackageName().startsWith("es.upm");
            }
        };
    }

}
