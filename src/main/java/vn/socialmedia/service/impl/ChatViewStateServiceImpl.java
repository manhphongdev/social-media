package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import vn.socialmedia.service.ChatViewStateService;

import java.time.Duration;

@Service
@Slf4j(topic = "CHAT-VIEW-STATE-SERVICE")
@RequiredArgsConstructor
public class ChatViewStateServiceImpl implements ChatViewStateService {
    private static final Duration VIEW_TTL = Duration.ofSeconds(60);
    private static final String CHAT_VIEW_SESSION_KEY = "chat:view:user:%s:session:%s";
    private static final String CHAT_VIEW_CONV_SESSIONS_KEY = "chat:view:user:%s:conv:%d:sessions";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void startViewingConversation(String username, Long conversationId, String sessionId) {
        if (username == null || sessionId == null || conversationId == null) {
            return;
        }

        String sessionKey = String.format(CHAT_VIEW_SESSION_KEY, username, sessionId);
        //TODO check previous session to view what conversation
        String previousConversationId = stringRedisTemplate.opsForValue().get(sessionKey);
        if (previousConversationId != null && !previousConversationId.equals(conversationId.toString())) {
            String previousConvSessionsKey = CHAT_VIEW_CONV_SESSIONS_KEY.formatted(username, Long.parseLong(previousConversationId));
            stringRedisTemplate.opsForSet().remove(previousConvSessionsKey, sessionId);
            Long remain = stringRedisTemplate.opsForSet().size(previousConvSessionsKey);
            if (remain != null && remain == 0L) {
                stringRedisTemplate.delete(previousConvSessionsKey);
            }
        }

        stringRedisTemplate.opsForValue().set(sessionKey, conversationId.toString(), VIEW_TTL);

        String convSessionKey = CHAT_VIEW_CONV_SESSIONS_KEY.formatted(username, conversationId);
        stringRedisTemplate.opsForSet().add(convSessionKey, sessionId);
        stringRedisTemplate.expire(convSessionKey, VIEW_TTL);

    }

    @Override
    public void stopViewingConversation(String username, Long conversationId, String sessionId) {
        if (username == null || username.isBlank() || sessionId == null || sessionId.isBlank()) {
            return;
        }

        String sessionKey = CHAT_VIEW_SESSION_KEY.formatted(username, sessionId);
        String currentConversation = stringRedisTemplate.opsForValue().get(sessionKey);

        if (currentConversation != null) {
            Long currentConvId = Long.parseLong(currentConversation);
            if (conversationId == null || currentConvId.equals(conversationId)) {
                String convSessionsKey = CHAT_VIEW_CONV_SESSIONS_KEY.formatted(username, currentConvId);
                stringRedisTemplate.opsForSet().remove(convSessionsKey, sessionId);

                Long remain = stringRedisTemplate.opsForSet().size(convSessionsKey);
                if (remain != null && remain == 0L) {
                    stringRedisTemplate.delete(convSessionsKey);
                }
            }
        }

        stringRedisTemplate.delete(sessionKey);

    }

    @Override
    public boolean isViewingConversation(String username, Long conversationId) {

        String convSessionsKey = CHAT_VIEW_CONV_SESSIONS_KEY.formatted(username, conversationId);
        Long count = stringRedisTemplate.opsForSet().size(convSessionsKey);
        return count != null && count > 0;
    }

    @Override
    public void clearViewingStateBySession(String username, String sessionId) {
        if (username == null || username.isBlank() || sessionId == null || sessionId.isBlank()) {
            return;
        }

        String sessionKey = CHAT_VIEW_SESSION_KEY.formatted(username, sessionId);
        String conversationId = stringRedisTemplate.opsForValue().get(sessionKey);

        if (conversationId != null) {
            String convSessionsKey = CHAT_VIEW_CONV_SESSIONS_KEY.formatted(username, Long.parseLong(conversationId));
            stringRedisTemplate.opsForSet().remove(convSessionsKey, sessionId);

            Long remain = stringRedisTemplate.opsForSet().size(convSessionsKey);
            if (remain != null && remain == 0L) {
                stringRedisTemplate.delete(convSessionsKey);
            }
        }

        stringRedisTemplate.delete(sessionKey);
    }

    private String sessionKey(String username, String sessionId) {
        return CHAT_VIEW_SESSION_KEY.formatted(username, sessionId);
    }
}
