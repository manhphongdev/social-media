package vn.socialmedia.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.socialmedia.auth.sevice.AuthenticationService;
import vn.socialmedia.auth.sevice.JwtService;
import vn.socialmedia.dto.request.LoginRequest;
import vn.socialmedia.enums.TokenType;
import vn.socialmedia.model.User;
import vn.socialmedia.security.user.UserSecurity;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller")
@Slf4j(topic = "AUTHENTICATION-CONTROLLER")
public class AuthController {
    private final AuthenticationService authenticationService;

    private final JwtService jwtService;

    @PostMapping("/login")
    public String login(LoginRequest req) {
        log.info("Login Request: {}", req.getEmail());
        UserDetails user = UserSecurity.builder()
                .user(User.builder().email("admin@mail.com").password("123456").build())
                .build();

        return jwtService.generateAccessToken(user);
    }


    @PostMapping("/test")
    public boolean test(LoginRequest req) {
        log.info("Login Request: {}", req.getEmail());
        UserDetails user = UserSecurity.builder()
                .user(User.builder().email("admin@mail.com").password("123456").build())
                .build();

        return jwtService.isTokenValid(
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0b2tlbl90eXBlIjoiQUNDRVNTX1RPS0VOIiwic3ViIjoiYWRtaW5AbWFpbC5jb20iLCJpc3MiOiJzb2NpYWwtbWVkaWEiLCJpYXQiOjE3NjcxNDc1NjgsImV4cCI6MTc2NzE1MTE2OH0.Y48sfFnkUdLdgGl1eH3TsJu5OlvBP5RzWDjLOUfocBc",
                TokenType.ACCESS_TOKEN,
                user);
    }


}
