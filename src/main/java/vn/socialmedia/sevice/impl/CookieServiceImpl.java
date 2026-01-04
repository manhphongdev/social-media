package vn.socialmedia.sevice.impl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import vn.socialmedia.security.config.JwtProperties;
import vn.socialmedia.sevice.CookieService;

import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CookieServiceImpl implements CookieService {
    private final JwtProperties jwtProperties;

    @Override
    public void addRefreshToken(HttpServletResponse response, String token) {
/*        Cookie cookie = new Cookie(jwtProperties.getRefreshTokenCookieName(), token);
        cookie.setPath(jwtProperties.getCookiePath());
        cookie.setHttpOnly(jwtProperties.isCookieHttpOnly());
        // cookie.setSecure(jwtProperties.isCookieSecure());
        //  cookie.setDomain(jwtProperties.getCookieDomain());
        cookie.setMaxAge((int) jwtProperties.getRefreshTokenExpiration());*/

        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getRefreshTokenCookieName(), token)
                .httpOnly(jwtProperties.isCookieHttpOnly())
                .path(jwtProperties.getCookiePath())
                .maxAge(jwtProperties.getRefreshTokenExpiration())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public Optional<String> getRefreshToken(HttpServletRequest req) {
        if (req.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(req.getCookies())
                .filter(cookie -> jwtProperties.getRefreshTokenCookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    @Override
    public void removeRefreshToken(HttpServletResponse resp) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getRefreshTokenCookieName(), "")
                .httpOnly(jwtProperties.isCookieHttpOnly())
                .path(jwtProperties.getCookiePath())
                .maxAge(0)
                .build();

        resp.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
