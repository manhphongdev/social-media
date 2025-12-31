package vn.socialmedia.auth.sevice;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;
import vn.socialmedia.enums.TokenType;

import java.util.function.Function;

public interface JwtService {

    String extractUsername(String token, TokenType tokenType);

    String generateAccessToken(UserDetails userDetails);

    String generateRefreshToken(UserDetails userDetails);

    boolean isTokenValid(String token, TokenType tokenType, UserDetails userDetails);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver, TokenType tokenType);
}
