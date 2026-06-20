package farid.aghazada.core.Service;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import farid.aghazada.core.DTO.AuthenticationResponseDto;
import farid.aghazada.core.Exception.UserLockedException;
import farid.aghazada.core.Repository.UserRepository;
import farid.aghazada.core.Security.JwtService;
import farid.aghazada.core.Security.UserPrincipal;

@Service
public class AuthenticationService {

    private final JwtService jwtService;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final TokenCacheService tokenCacheService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private AuthenticationManager authenticationManager;

    public AuthenticationService(JwtService jwtService, BruteForceProtectionService bruteForceProtectionService, TokenCacheService tokenCacheService, CustomUserDetailsService userDetailsService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.bruteForceProtectionService = bruteForceProtectionService;
        this.tokenCacheService = tokenCacheService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponseDto authenticateUser(String username, String password) {
        if (bruteForceProtectionService.isBlocked(username)) {
            throw new UserLockedException("User " + username + " is temporarily locked due to multiple failed login attempts. Please try again later.");
        }
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException e) {
            bruteForceProtectionService.loginFailed(username);
            throw e;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        int tokenVersion = userPrincipal.getUser().getTokenVersion();

        bruteForceProtectionService.loginSucceeded(username);
        String token = jwtService.generateToken(username, tokenVersion);
        tokenCacheService.addToken(token);
        return new AuthenticationResponseDto(token);
    }

    public void logoutUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header must be provided and start with Bearer");
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        tokenCacheService.removeToken(token);
        userDetailsService.evictUserDetailsCache(username);

        userRepository.incrementTokenVersion(username);
    }
}
