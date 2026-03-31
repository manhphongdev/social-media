package vn.socialmedia.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisTestRunner implements ApplicationRunner {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            redisTemplate.opsForValue().set("test:ping", "pong", Duration.ofSeconds(60));
            String value = (String) redisTemplate.opsForValue().get("test:ping");
            log.info("🟢 Redis test write OK: {}", value);
        } catch (Exception e) {
            log.error("🔴 Redis test write FAILED: {}", e.getMessage(), e);
        }
    }
}
