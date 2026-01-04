package vn.socialmedia.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.socialmedia.dto.request.AccountCreationRequest;
import vn.socialmedia.dto.request.LoginRequest;
import vn.socialmedia.dto.response.ResponseData;
import vn.socialmedia.dto.response.TokenResponse;
import vn.socialmedia.sevice.AuthenticationService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller")
@Slf4j(topic = "AUTHENTICATION-CONTROLLER")
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseData<TokenResponse> login(@Valid @RequestBody LoginRequest req, HttpServletResponse httpResponse) {
        log.info("Login Request: {}", req.getEmail());

        return new ResponseData<>(HttpStatus.OK.value(), "Login successful", authenticationService.login(req, httpResponse));
    }

    @PostMapping("/logout")
    public ResponseData<?> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        log.info("Logout Request");
        authenticationService.logout(httpRequest, httpResponse);
        return new ResponseData<>(HttpStatus.OK.value(), "Logout successful");
    }

    @PostMapping("/register")
    public ResponseData<?> register(@Valid @RequestBody AccountCreationRequest req) {
        log.info("Account Registration Request, email: {}", req.getEmail());
        authenticationService.registerAccount(req);
        return new ResponseData<>(HttpStatus.CREATED.value(), "Account created successfully");
    }

    @PostMapping("/refresh-token")
    public ResponseData<?> refreshToken(HttpServletRequest request) {
        log.info("Refresh token request");
        return new ResponseData<>(HttpStatus.OK.value(), "Refresh successful", authenticationService.refreshToken(request));
    }
}
