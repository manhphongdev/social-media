package vn.socialmedia.config.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class WsListener {

    @EventListener
    public void connect(SessionConnectEvent session) {
        Principal user = session.getUser();
        String sessionId = SimpMessageHeaderAccessor
                .wrap(session.getMessage())
                .getSessionId();

        log.info("WS connected user={} session={}",
                user != null ? user.getName() : "anonymous",
                sessionId);
    }
}
