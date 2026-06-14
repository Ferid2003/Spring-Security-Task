package farid.aghazada.core.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class BruteForceProtectionService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long BLOCK_DUTATION_MINUTES = 5;

    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> blockedUsers = new ConcurrentHashMap<>();

    public void loginFailed(String username) {
        int attempts = failedAttempts.getOrDefault(username, 0) + 1;
        failedAttempts.put(username, attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            blockedUsers.put(username, LocalDateTime.now());
        }
    }

    public void loginSucceeded(String username) {
        failedAttempts.remove(username);
        blockedUsers.remove(username);
    }

    public boolean isBlocked(String username) {
        if (!blockedUsers.containsKey(username)) {
            return false;
        }

        LocalDateTime blockedAt = blockedUsers.get(username);
        if (LocalDateTime.now().isAfter(blockedAt.plusMinutes(BLOCK_DUTATION_MINUTES))) {
            // block expired, clean up
            blockedUsers.remove(username);
            failedAttempts.remove(username);
            return false;
        }

        return true;
    }

}
