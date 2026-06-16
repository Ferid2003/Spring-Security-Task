package farid.aghazada.core.Service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import farid.aghazada.core.Security.JwtBlacklistService;

class JwtBlacklistServiceTest {

    private JwtBlacklistService jwtBlacklistService;

    @BeforeEach
    void setUp() {
        jwtBlacklistService = new JwtBlacklistService();
    }

    @Test
    void isTokenBlacklistedReturnsFalseForUnknownJti() {
        assertThat(jwtBlacklistService.isTokenBlacklisted("unknown-jti")).isFalse();
    }

    @Test
    void isTokenBlacklistedReturnsTrueForBlacklistedJti() {
        Date futureExpiration = new Date(System.currentTimeMillis() + 60000); // 1 min from now
        jwtBlacklistService.blacklistToken("jti-123", futureExpiration);

        assertThat(jwtBlacklistService.isTokenBlacklisted("jti-123")).isTrue();
    }

    @Test
    void isTokenBlacklistedReturnsFalseForExpiredJti() {
        Date pastExpiration = new Date(System.currentTimeMillis() - 60000); // 1 min ago
        jwtBlacklistService.blacklistToken("jti-123", pastExpiration);

        assertThat(jwtBlacklistService.isTokenBlacklisted("jti-123")).isFalse();
    }

    @Test
    void blacklistingDifferentJtisDoesNotAffectEachOther() {
        Date futureExpiration = new Date(System.currentTimeMillis() + 60000);
        jwtBlacklistService.blacklistToken("jti-1", futureExpiration);

        assertThat(jwtBlacklistService.isTokenBlacklisted("jti-2")).isFalse();
    }

    @Test
    void expiredTokensAreRemovedAfterCheck() {
        Date pastExpiration = new Date(System.currentTimeMillis() - 60000);
        jwtBlacklistService.blacklistToken("jti-expired", pastExpiration);

        jwtBlacklistService.isTokenBlacklisted("jti-expired"); // triggers cleanup

        assertThat(jwtBlacklistService.isTokenBlacklisted("jti-expired")).isFalse();
    }
}
