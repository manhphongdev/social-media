package vn.socialmedia.auth.sevice.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.socialmedia.auth.repository.UserRepository;
import vn.socialmedia.auth.sevice.AuthenticationService;
import vn.socialmedia.auth.sevice.JwtService;
import vn.socialmedia.dto.request.LoginRequest;
import vn.socialmedia.dto.response.TokenResponse;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j(topic = "AUTHENTICATION-SERVICE")
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    AuthenticationManager authenticationManager;

    @Override
    public TokenResponse getAccessToken(LoginRequest req) {
        log.debug("getAccessToken");
        List<String> authorities = new ArrayList<>();

        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );
            log.info("isAuthenticated={}", authenticate.isAuthenticated());
            log.info("authorities={}", authenticate.getAuthorities());

            authorities.add(authenticate.getAuthorities().toString());

            SecurityContextHolder.getContext().setAuthentication(authenticate);
        } catch (AuthenticationException e) {
            log.error("Login failed: {}", e.getMessage());
            throw new AuthenticationServiceException(e.getMessage());
        }

        return null;
    }
}
