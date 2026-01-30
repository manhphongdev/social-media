package vn.socialmedia.enums.converter;


import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import vn.socialmedia.enums.PostPrivacy;

@Component
public class PostPrivacyConverter implements Converter<String, PostPrivacy> {
    @Override
    public PostPrivacy convert(String source) {
        return PostPrivacy.valueOf(source.toUpperCase());
    }
}
