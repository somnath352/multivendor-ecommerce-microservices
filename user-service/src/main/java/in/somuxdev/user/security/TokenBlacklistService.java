package in.somuxdev.user.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public void blacklistToken(String token, long remainingExpiryMillis) {
        log.info(">>> Attempting to blacklist token");
        log.info(">>> Remaining expiry millis: {}", remainingExpiryMillis);
        if(remainingExpiryMillis > 0) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + token,
                    "blacklisted",
                    remainingExpiryMillis,
                    TimeUnit.MILLISECONDS
            );
            log.info("Token blacklisted successfully");
        }
        else {
            log.info(">>> Token already expired — skipping blacklist");
        }
    }

    public boolean isBlacklisted(String token) {
        boolean result = redisTemplate.hasKey(BLACKLIST_PREFIX + token);
        log.info("Is token blacklist: {}", result);
        return result;
    }

}
