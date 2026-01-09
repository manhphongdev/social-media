package vn.socialmedia.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public interface CookieService {
    void addRefreshToken(HttpServletResponse response, String token);

    Optional<String> getRefreshToken(HttpServletRequest req);

    void removeRefreshToken(HttpServletResponse resp);
}
