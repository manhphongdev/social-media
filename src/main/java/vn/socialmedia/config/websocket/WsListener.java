package vn.socialmedia.config.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import vn.socialmedia.service.OnlineStatusService;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class WsListener {

    private final OnlineStatusService onlineStatusService;

    @EventListener
    public void connect(SessionConnectEvent session) {
        Principal user = session.getUser();
        String sessionId = SimpMessageHeaderAccessor
                .wrap(session.getMessage())
                .getSessionId();

        assert user != null;
        onlineStatusService.userConnected(user.getName());

        log.info("WS connected user={} session={}",
                user.getName(),
                sessionId);
    }

    @EventListener
    public void disconnect(SessionDisconnectEvent session) {
        Principal user = session.getUser();
        assert user != null;
        onlineStatusService.userDisconnected(user.getName()); //TODO check disconnected
        log.info("Ws disconnect user ={}", user.getName());
    }
}
