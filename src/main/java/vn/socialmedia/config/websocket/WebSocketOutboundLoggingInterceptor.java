package vn.socialmedia.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketOutboundLoggingInterceptor implements ChannelInterceptor {

    private static final int MAX_PAYLOAD_PREVIEW = 200;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        SimpMessageType messageType = SimpMessageHeaderAccessor.getMessageType(message.getHeaders());
        if (messageType != SimpMessageType.MESSAGE) {
            return message;
        }

        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();
        String user = accessor.getUser() != null ? accessor.getUser().getName() : null;

        Object payload = message.getPayload();
        String payloadPreview = toPreview(payload);

        log.debug("WS outbound type={} dest={} session={} user={} payload={}",
                messageType, destination, sessionId, user, payloadPreview);
        return message;
    }

    private String toPreview(Object payload) {
        if (payload == null) {
            return "null";
        }
        if (payload instanceof byte[]) {
            return "byte[" + ((byte[]) payload).length + "]";
        }

        String text = payload.toString();
        if (text.length() <= MAX_PAYLOAD_PREVIEW) {
            return text;
        }
        return text.substring(0, MAX_PAYLOAD_PREVIEW) + "...(truncated)";
    }
}
