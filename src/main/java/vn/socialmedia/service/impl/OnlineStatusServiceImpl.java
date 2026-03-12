package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import vn.socialmedia.service.OnlineStatusService;

@Service
@RequiredArgsConstructor
public class OnlineStatusServiceImpl implements OnlineStatusService {
    private static final String ONLINE_STATUS = "online:users";
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void userConnected(String username) {
        stringRedisTemplate.opsForSet().add(ONLINE_STATUS, username);
    }

    @Override
    public void userDisconnected(String username) {
        stringRedisTemplate.opsForSet().remove(ONLINE_STATUS, username);
        Long size = stringRedisTemplate.opsForSet().size(ONLINE_STATUS);
        if (size != null && size == 0) {
            stringRedisTemplate.delete(ONLINE_STATUS);
        }
    }

    @Override
    public boolean isOnline(String username) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(ONLINE_STATUS, username));
    }
}
