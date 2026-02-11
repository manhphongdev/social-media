package vn.socialmedia.common.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import vn.socialmedia.dto.request.CursorPageRequest;

import java.util.Base64;

public class CursorPageHelper {
    private static ObjectMapper objectMapper;

    public CursorPageHelper() {
        objectMapper = new ObjectMapper();
    }

    public static String encodeCursor(CursorPageRequest cursor) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(cursor);
            return Base64.getUrlEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Encode cursor failed", e);
        }
    }

    public static CursorPageRequest decodeCursor(String cursor) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cursor);
            return objectMapper.readValue(bytes, CursorPageRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Decode cursor failed", e);
        }
    }

}
