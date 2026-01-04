package vn.socialmedia.security.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Validated
@Getter
@Setter
public class JwtProperties {

    @NotBlank
    private String accessTokenSecret;

    @NotBlank
    private String refreshTokenSecret;

    @Positive
    private long accessTokenExpiration;

    @Positive
    private long refreshTokenExpiration;

    @NotBlank
    private String issuer = "social-media.com";

    private String refreshTokenCookieName = "refreshToken";
    private boolean cookieHttpOnly = true;
    private boolean cookieSecure = true;
    private String cookiePath = "/";
    private String cookieDomain = "";
}
