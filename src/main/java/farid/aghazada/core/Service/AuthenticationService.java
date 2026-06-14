package farid.aghazada.core.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import farid.aghazada.core.DTO.AuthenticationResponseDto;
import farid.aghazada.core.Security.JwtService;

@Service
public class AuthenticationService {

    private final JwtService jwtService;
    private AuthenticationManager authenticationManager;

    public AuthenticationService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponseDto authenticateUser(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        String token = jwtService.generateToken(username);
        return new AuthenticationResponseDto(token);
    }

}
