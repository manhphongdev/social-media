package vn.socialmedia.config.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import vn.socialmedia.enums.TokenType;
import vn.socialmedia.exception.JwtException;
import vn.socialmedia.service.JwtService;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (!StringUtils.hasText(authHeader)) {
            throw new JwtException("Missing Authorization header");
        }

        String[] parts = authHeader.split(" ");
        if (parts.length != 2 || !"Bearer".equalsIgnoreCase(parts[0])) {
            throw new JwtException("Invalid Authorization header format"); //TODO update exception
        }

        String token = parts[1];

        if (!jwtService.isTokenValid(token, TokenType.ACCESS_TOKEN)) {
            throw new JwtException("Invalid or expired token");
        }

        String username = jwtService.extractUsername(token, TokenType.ACCESS_TOKEN);
        if (!StringUtils.hasText(username)) {
            throw new JwtException("Invalid token payload");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

        accessor.setUser(authentication);

        return message;
    }
}
