package farid.aghazada.core.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import farid.aghazada.core.Security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "dGVzdHNlY3JldGtleWZvcmp3dHRlc3Rpbmd0ZXN0c2VjcmV0a2V5Zm9y";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "SECRET", SECRET);
    }

    @Test
    void generateTokenReturnsNonNullToken() {
        String token = jwtService.generateToken("Jane.Doe");
        assertThat(token).isNotNull();
    }

    @Test
    void generateTokenReturnsNonEmptyToken() {
        String token = jwtService.generateToken("Jane.Doe");
        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsernameReturnsCorrectUsername() {
        String token = jwtService.generateToken("Jane.Doe");
        assertThat(jwtService.extractUsername(token)).isEqualTo("Jane.Doe");
    }

    @Test
    void validateTokenReturnsTrueForValidToken() {
        String token = jwtService.generateToken("Jane.Doe");
        assertThat(jwtService.validateToken(token, "Jane.Doe")).isTrue();
    }

    @Test
    void validateTokenReturnsFalseForWrongUsername() {
        String token = jwtService.generateToken("Jane.Doe");
        assertThat(jwtService.validateToken(token, "John.Smith")).isFalse();
    }

    @Test
    void generateTokenForDifferentUsersReturnsDifferentTokens() {
        String token1 = jwtService.generateToken("Jane.Doe");
        String token2 = jwtService.generateToken("John.Smith");
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void extractUsernameWithTamperedTokenThrowsException() {
        String token = jwtService.generateToken("Jane.Doe");
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";
        assertThatThrownBy(() -> jwtService.extractUsername(tamperedToken))
            .isInstanceOf(Exception.class);
    }

    @Test
    void validateTokenReturnsFalseForTamperedToken() {
        String token = jwtService.generateToken("Jane.Doe");
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";
        assertThatThrownBy(() -> jwtService.validateToken(tamperedToken, "Jane.Doe"))
            .isInstanceOf(Exception.class);
    }

    @Test
    void extractJtiReturnsCorrectJtiFromToken() {
        String token = jwtService.generateToken("Jane.Doe");
        String jti = jwtService.extractJti(token);

        assertThat(jti).isNotNull();
    }
}
