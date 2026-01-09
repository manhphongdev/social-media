package vn.socialmedia.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "aws")
@Validated
@Getter
@Setter
public class AWSProperties {

    private String secretKey;
    private String accessKey;
    private S3 s3;
    private CloudFont cloudfront;

    @Getter
    @Setter
    public static class S3 {
        private String bucket;
        private String cdn;
        private String region;
    }

    @Getter
    @Setter
    public static class CloudFont {
        private String url;
    }

}
