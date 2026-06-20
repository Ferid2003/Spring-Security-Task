package farid.aghazada.core.Security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import farid.aghazada.core.Entity.User;
import farid.aghazada.core.Exception.UserNotFoundException;
import farid.aghazada.core.Repository.UserRepository;
import farid.aghazada.core.Service.CustomUserDetailsService;
import farid.aghazada.core.Service.TokenCacheService;

@Component
public class JwtAuthFilter extends OncePerRequestFilter{

    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final TokenCacheService tokenCacheService;
    private final UserRepository userRepository;

    public JwtAuthFilter(CustomUserDetailsService userDetailsService, JwtService jwtService, TokenCacheService tokenCacheService, UserRepository userRepository) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.tokenCacheService = tokenCacheService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username = jwtService.extractUsername(token);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails;

            if (tokenCacheService.isCacheAvailable()) {
                if (!tokenCacheService.isTokenActive(token)) {
                    filterChain.doFilter(request, response);
                    return;
                }
                userDetails = userDetailsService.loadUserByUsername(username);
            }else {
                int jwtVersion = jwtService.extractTokenVersion(token);
                User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found with username"));
                if (jwtVersion != user.getTokenVersion()) {
                    filterChain.doFilter(request, response);
                    return;
                }

                userDetails = new UserPrincipal(user);
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        filterChain.doFilter(request, response);
    }

}
