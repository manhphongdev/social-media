package vn.socialmedia.sevice;

import org.springframework.security.core.userdetails.UserDetails;
import vn.socialmedia.enums.TokenType;

import java.util.Date;

public interface JwtService {

    String extractUsername(String token, TokenType tokenType);

    String generateAccessToken(UserDetails userDetails);

    String generateRefreshToken(UserDetails userDetails);

    boolean isTokenValid(String token, TokenType tokenType);

    String extractId(String token, TokenType tokenType);

    Date extractExpiration(String token, TokenType tokenType);

}
