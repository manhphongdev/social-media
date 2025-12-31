package vn.socialmedia.auth.sevice.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.socialmedia.auth.sevice.JwtService;
import vn.socialmedia.enums.TokenType;
import vn.socialmedia.exception.InvalidTokenException;
import vn.socialmedia.exception.TokenExpiredException;
import vn.socialmedia.security.config.JwtProperties;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "JWT-SERVICE")
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey accessTokenKey;
    private SecretKey refreshTokenKey;

    @PostConstruct
    public void init() {
        this.accessTokenKey = Keys.hmacShaKeyFor(Base64
                .getDecoder()
                .decode(jwtProperties.getAccessTokenSecret()));
        this.refreshTokenKey = Keys.hmacShaKeyFor(Base64
                .getDecoder()
                .decode(jwtProperties.getRefreshTokenSecret()));
    }

    @Override
    public String extractUsername(String token, TokenType tokenType) {
        return extractClaim(token, Claims::getSubject, tokenType);
    }

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        log.debug("generateAccessToken email : {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", TokenType.ACCESS_TOKEN);
        return generateToken(userDetails, claims, TokenType.ACCESS_TOKEN);
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        log.debug("generateRefreshToken email : {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", TokenType.REFRESH_TOKEN);
        return generateToken(userDetails, claims, TokenType.REFRESH_TOKEN);
    }

    @Override
    public boolean isTokenValid(String token, TokenType tokenType, UserDetails userDetails) {

        return extractExpiration(token, tokenType).after(Date.from(Instant.now()))
                && userDetails.getUsername().equals(extractUsername(token, tokenType))
                && jwtProperties.getIssuer().equals(extractClaim(token, Claims::getIssuer, tokenType));
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, TokenType tokenType) {
        final Claims claims = getClaimsFromToken(token, tokenType);
        return claimsResolver.apply(claims);
    }

    private Date extractExpiration(String token, TokenType tokenType) {
        return extractClaim(token, Claims::getExpiration, tokenType);
    }

    private Claims getClaimsFromToken(String token, TokenType tokenType) {

        try {
            return switch (tokenType) {
                case ACCESS_TOKEN -> Jwts.parser()
                        .verifyWith(accessTokenKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                case REFRESH_TOKEN -> Jwts.parser()
                        .verifyWith(refreshTokenKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            };
        } catch (ExpiredJwtException e) {
            log.warn("Token expired for user: {}", e.getClaims().getSubject());
            throw new TokenExpiredException("Token has expired");

        } catch (SignatureException e) {
            log.error("Invalid JWT token signature: {}", e.getMessage());
            throw new InvalidTokenException("Invalid JWT token signature");

        } catch (MalformedJwtException e) {
            log.error("Invalid format JWT token");
            throw new InvalidTokenException("Invalid format JWT token");
        }
    }

    private String generateToken(UserDetails userDetails, Map<String, Object> claims, TokenType tokenType) {
        return switch (tokenType) {
            case REFRESH_TOKEN -> Jwts.builder()
                    .header().type("JWT")
                    .and()
                    .claims(claims)
                    .subject(userDetails.getUsername())
                    .issuer(jwtProperties.getIssuer())
                    .issuedAt(Date.from(Instant.now()))
                    .expiration(Date.from(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration())))
                    .signWith(refreshTokenKey, Jwts.SIG.HS256)
                    .compact();
            case ACCESS_TOKEN -> Jwts.builder()
                    .header().type("JWT")
                    .and()
                    .claims(claims)
                    .subject(userDetails.getUsername())
                    .issuer(jwtProperties.getIssuer())
                    .issuedAt(Date.from(Instant.now()))
                    .expiration(Date.from(Instant.now().plusMillis(jwtProperties.getAccessTokenExpiration())))
                    .signWith(accessTokenKey, Jwts.SIG.HS256)
                    .compact();
        };
    }
}
