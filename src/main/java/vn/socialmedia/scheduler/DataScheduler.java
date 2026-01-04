package vn.socialmedia.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.socialmedia.repository.RefreshTokenRepository;

@RequiredArgsConstructor
@Component
@Slf4j
public class DataScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredRefreshTokens() {
        log.info("Cleaning expired refresh tokens, start...");
        refreshTokenRepository.deleteExpiredRefreshTokens();
        log.info("Cleaning expired refresh tokens, end...");
    }
}
