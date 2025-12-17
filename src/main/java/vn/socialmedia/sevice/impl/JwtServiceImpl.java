package vn.socialmedia.sevice.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.socialmedia.enums.TokenType;
import vn.socialmedia.sevice.JwtService;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "JWT-SERVICE")
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.key_access}")
    private String jwtSecretAccessKey;

    @Value("${jwt.key_refresh}")
    private String jwtSecretRefreshKey;

    @Value("${jwt.access_expiration}")
    private Long jwtAccessExpirationMs;

    @Value("${jwt.refresh_expiration}")
    private Long jwtRefreshExpirationMs;

    @Override
    public String extractUsername(String token, TokenType tokenType) {
        log.debug("extractUsername token : {}", tokenType);
        return extractClaim(token, Claims::getSubject, tokenType);
    }

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        log.debug("generateAccessToken userDetails : {}", userDetails);
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());
        return generateToken(userDetails, claims, TokenType.ACCESS_TOKEN);
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        log.debug("generateRefreshToken userDetails : {}", userDetails);
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());
        return generateToken(userDetails, claims, TokenType.REFRESH_TOKEN);
    }

    @Override
    public boolean isTokenValid(String token, TokenType tokenType) {
        return extractExpiration(token, tokenType).before(new Date());
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
        return Jwts.parser()
                .verifyWith(getSignInKey(tokenType))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public SecretKey getSignInKey(TokenType tokenType) {
        return switch (tokenType) {
            case REFRESH_TOKEN -> Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretRefreshKey));
            case ACCESS_TOKEN -> Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretAccessKey));
        };
    }

    private String generateToken(UserDetails userDetails, Map<String, Object> claims, TokenType tokenType) {
        return switch (tokenType) {
            case REFRESH_TOKEN -> Jwts.builder()
                    .claims(claims)
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + jwtRefreshExpirationMs))
                    .signWith(getSignInKey(TokenType.REFRESH_TOKEN), Jwts.SIG.HS256)
                    .compact();
            case ACCESS_TOKEN -> Jwts.builder()
                    .claims(claims)
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + jwtAccessExpirationMs))
                    .signWith(getSignInKey(TokenType.ACCESS_TOKEN), Jwts.SIG.HS256)
                    .compact();
        };
    }
}
