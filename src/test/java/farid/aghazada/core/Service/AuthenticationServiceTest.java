package farid.aghazada.core.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import farid.aghazada.core.DTO.AuthenticationResponseDto;
import farid.aghazada.core.Exception.UserLockedException;
import farid.aghazada.core.Security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private BruteForceProtectionService bruteForceProtectionService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService.setAuthenticationManager(authenticationManager);
    }

    @Test
    void authenticateUserReturnsTokenWhenCredentialsAreValid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(mock(Authentication.class));
        when(jwtService.generateToken("Jane.Doe")).thenReturn("mocked.jwt.token");

        AuthenticationResponseDto result = authenticationService.authenticateUser("Jane.Doe", "password");

        assertThat(result.token()).isEqualTo("mocked.jwt.token");
    }

    @Test
    void authenticateUserThrowsWhenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authenticationService.authenticateUser("Jane.Doe", "wrongpassword"))
            .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void authenticateUserCallsAuthenticationManagerWithCorrectCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(mock(Authentication.class));
        when(jwtService.generateToken("Jane.Doe")).thenReturn("mocked.jwt.token");

        authenticationService.authenticateUser("Jane.Doe", "password");

        verify(authenticationManager).authenticate(
            new UsernamePasswordAuthenticationToken("Jane.Doe", "password")
        );
    }

    @Test
    void authenticateUserThrowsWhenUserIsBlocked() {
        when(bruteForceProtectionService.isBlocked("Jane.Doe")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.authenticateUser("Jane.Doe", "password"))
            .isInstanceOf(UserLockedException.class);

        verifyNoInteractions(authenticationManager);
    }

    @Test
    void authenticateUserCallsLoginFailedOnBadCredentials() {
        when(bruteForceProtectionService.isBlocked("Jane.Doe")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authenticationService.authenticateUser("Jane.Doe", "wrongpassword"))
            .isInstanceOf(BadCredentialsException.class);

        verify(bruteForceProtectionService).loginFailed("Jane.Doe");
    }

    @Test
    void authenticateUserCallsLoginSucceededOnSuccess() {
        when(bruteForceProtectionService.isBlocked("Jane.Doe")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(mock(Authentication.class));
        when(jwtService.generateToken("Jane.Doe")).thenReturn("mocked.jwt.token");

        authenticationService.authenticateUser("Jane.Doe", "password");

        verify(bruteForceProtectionService).loginSucceeded("Jane.Doe");
    }

}
