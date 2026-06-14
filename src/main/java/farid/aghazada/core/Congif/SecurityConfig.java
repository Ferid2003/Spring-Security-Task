package farid.aghazada.core.Congif;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import farid.aghazada.core.Security.JwtAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter JwtAuthFilter;

    public SecurityConfig(JwtAuthFilter JwtAuthFilter) {
        this.JwtAuthFilter = JwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth ->auth

                    .requestMatchers(HttpMethod.POST, "/api/trainees").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/trainees/authenticate").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/trainers").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/trainers/authenticate").permitAll()

                    .anyRequest().authenticated()
                    )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    )
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> 
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized ")))
            .addFilterBefore(JwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
