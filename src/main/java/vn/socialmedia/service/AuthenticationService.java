package vn.socialmedia.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.socialmedia.dto.request.AccountCreationRequest;
import vn.socialmedia.dto.request.LoginRequest;
import vn.socialmedia.dto.response.TokenResponse;

public interface AuthenticationService {

    TokenResponse login(LoginRequest req, HttpServletResponse httpResponse);

    void registerAccount(AccountCreationRequest req);

    TokenResponse refreshToken(HttpServletRequest req);

    void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse);
}
