package vn.socialmedia.common.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import vn.socialmedia.dto.request.CursorPageRequest;

import java.util.Base64;

@Component
public class CursorPageHelper {

    private final ObjectMapper objectMapper;

    public CursorPageHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String encodeCursor(CursorPageRequest cursor) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(cursor);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Encode cursor failed", e);
        }
    }

    public CursorPageRequest decodeCursor(String cursor) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cursor);
            return objectMapper.readValue(bytes, CursorPageRequest.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Decode cursor failed", e);
        }
    }
}
