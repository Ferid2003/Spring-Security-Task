package farid.aghazada.core.Service;

import static org.assertj.core.api.Assertions.assertThat;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BruteForceProtectionServiceTest {

    private BruteForceProtectionService bruteForceProtectionService;

    @BeforeEach
    void setUp() {
        bruteForceProtectionService = new BruteForceProtectionService();
    }

    @Test
    void isBlockedReturnsFalseForNewUser() {
        assertThat(bruteForceProtectionService.isBlocked("Jane.Doe")).isFalse();
    }

    @Test
    void isBlockedReturnsFalseBeforeMaxAttempts() {
        bruteForceProtectionService.loginFailed("Jane.Doe");
        bruteForceProtectionService.loginFailed("Jane.Doe");
        assertThat(bruteForceProtectionService.isBlocked("Jane.Doe")).isFalse();
    }

    @Test
    void isBlockedReturnsTrueAfterMaxAttempts() {
        bruteForceProtectionService.loginFailed("Jane.Doe");
        bruteForceProtectionService.loginFailed("Jane.Doe");
        bruteForceProtectionService.loginFailed("Jane.Doe");
        assertThat(bruteForceProtectionService.isBlocked("Jane.Doe")).isTrue();
    }

    @Test
    void loginSucceededResetsFailedAttempts() {
        bruteForceProtectionService.loginFailed("Jane.Doe");
        bruteForceProtectionService.loginFailed("Jane.Doe");
        bruteForceProtectionService.loginSucceeded("Jane.Doe");
        bruteForceProtectionService.loginFailed("Jane.Doe");
        bruteForceProtectionService.loginFailed("Jane.Doe");
        assertThat(bruteForceProtectionService.isBlocked("Jane.Doe")).isFalse();
    }

    @Test
    void loginSucceededUnblocksUser() {
        bruteForceProtectionService.loginFailed("Jane.Doe");
        bruteForceProtectionService.loginFailed("Jane.Doe");
        bruteForceProtectionService.loginFailed("Jane.Doe");
        bruteForceProtectionService.loginSucceeded("Jane.Doe");
        assertThat(bruteForceProtectionService.isBlocked("Jane.Doe")).isFalse();
    }

    @Test
    void isBlockedReturnsFalseAfterBlockExpires() throws Exception {
        // set block time to 6 minutes ago using reflection
        bruteForceProtectionService.loginFailed("Jane.Doe");
        bruteForceProtectionService.loginFailed("Jane.Doe");
        bruteForceProtectionService.loginFailed("Jane.Doe");

        Field blockedUsersField = BruteForceProtectionService.class.getDeclaredField("blockedUsers");
        blockedUsersField.setAccessible(true);
        Map<String, LocalDateTime> blockedUsers = (Map<String, LocalDateTime>) blockedUsersField.get(bruteForceProtectionService);
        blockedUsers.put("Jane.Doe", LocalDateTime.now().minusMinutes(6)); // simulate expired block

        assertThat(bruteForceProtectionService.isBlocked("Jane.Doe")).isFalse();
    }

    @Test
    void blockingOneUserDoesNotAffectAnotherUser() {
        bruteForceProtectionService.loginFailed("Jane.Doe");
        bruteForceProtectionService.loginFailed("Jane.Doe");
        bruteForceProtectionService.loginFailed("Jane.Doe");
        assertThat(bruteForceProtectionService.isBlocked("John.Smith")).isFalse();
    }
}
