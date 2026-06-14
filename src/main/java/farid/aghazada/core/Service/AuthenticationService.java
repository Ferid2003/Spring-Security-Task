package farid.aghazada.core.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import farid.aghazada.core.DTO.AuthenticationResponseDto;
import farid.aghazada.core.Exception.UserLockedException;
import farid.aghazada.core.Security.JwtService;

@Service
public class AuthenticationService {

    private final JwtService jwtService;
    private final BruteForceProtectionService bruteForceProtectionService;
    private AuthenticationManager authenticationManager;

    public AuthenticationService(JwtService jwtService, BruteForceProtectionService bruteForceProtectionService) {
        this.jwtService = jwtService;
        this.bruteForceProtectionService = bruteForceProtectionService;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponseDto authenticateUser(String username, String password) {
        if (bruteForceProtectionService.isBlocked(username)) {
            throw new UserLockedException("User " + username + " is temporarily locked due to multiple failed login attempts. Please try again later.");
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException e) {
            bruteForceProtectionService.loginFailed(username);
            throw e;
        }

        bruteForceProtectionService.loginSucceeded(username);
        String token = jwtService.generateToken(username);
        return new AuthenticationResponseDto(token);
    }

}
