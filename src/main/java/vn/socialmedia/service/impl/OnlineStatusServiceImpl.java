package vn.socialmedia.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import vn.socialmedia.service.OnlineStatusService;

@Service
@RequiredArgsConstructor
public class OnlineStatusServiceImpl implements OnlineStatusService {
    private final StringRedisTemplate template;

    @Override
    public void userConnected(Long userid) {

    }

    @Override
    public void userDisconnected(Long userId) {

    }

    @Override
    public boolean isOnline(Long userId) {
        return false;
    }
}
