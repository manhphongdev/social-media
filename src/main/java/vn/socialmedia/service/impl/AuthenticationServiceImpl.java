package vn.socialmedia.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.socialmedia.dto.request.AccountCreationRequest;
import vn.socialmedia.dto.request.LoginRequest;
import vn.socialmedia.dto.response.TokenResponse;
import vn.socialmedia.enums.ErrorCode;
import vn.socialmedia.enums.TokenType;
import vn.socialmedia.enums.UserStatus;
import vn.socialmedia.exception.BusinessException;
import vn.socialmedia.exception.InvalidCredentialsException;
import vn.socialmedia.exception.InvalidTokenException;
import vn.socialmedia.model.RefreshTokenBlackList;
import vn.socialmedia.model.User;
import vn.socialmedia.repository.RefreshTokenRepository;
import vn.socialmedia.repository.UserRepository;
import vn.socialmedia.service.AuthenticationService;
import vn.socialmedia.service.CookieService;
import vn.socialmedia.service.JwtService;

import java.util.Set;


@Service
@Slf4j(topic = "AUTHENTICATION-SERVICE")
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;

    @Override
    @Transactional
    public TokenResponse login(LoginRequest req, HttpServletResponse httpResponse) {
        try {
            //b1 verify email/password
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(),
                            req.getPassword()));

            //load userdetail after authenticate success
            UserDetails userDetails = userDetailsService.loadUserByUsername(req.getEmail());
            //b2 create access/refresh token
            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            cookieService.addRefreshToken(httpResponse, refreshToken);

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .isAuthenticate(true)
                    .build();
        } catch (AuthenticationException e) {
            log.error("Invalid email or password, email: {} ", req.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    @Override
    @Transactional
    public void registerAccount(AccountCreationRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new BusinessException(ErrorCode.Email_Already_Exist);
        }
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.Password_And_Re_Password_Not_Match);
        }
        log.info("Register account with email {}", req.getEmail());


        userRepository.save(User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .gender(req.getGender())
                .dateOfBirth(req.getDateOfBirth())
                .roles(Set.of())
                .status(UserStatus.ACTIVE)
                .build());
    }

    @Override
    public TokenResponse refreshToken(HttpServletRequest req) {

        String token = cookieService.getRefreshToken(req)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND_IN_COOKIE));

        if (!jwtService.isTokenValid(token, TokenType.REFRESH_TOKEN)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String jId = jwtService.extractId(token, TokenType.REFRESH_TOKEN);

        RefreshTokenBlackList refreshToken = refreshTokenRepository.findById(jId).orElse(null);

        if (refreshToken != null) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        }

        String email = jwtService.extractUsername(token, TokenType.REFRESH_TOKEN);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return TokenResponse
                .builder()
                .accessToken(newAccessToken)
                .isAuthenticate(true)
                .build();
    }

    @Override
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String token = cookieService.getRefreshToken(httpRequest).orElse(null);
        refreshTokenRepository.save(RefreshTokenBlackList.builder()
                .jid(jwtService.extractId(token, TokenType.REFRESH_TOKEN))
                .expiresAt(jwtService.extractExpiration(token, TokenType.REFRESH_TOKEN).toInstant())
                .build());
    }

}
