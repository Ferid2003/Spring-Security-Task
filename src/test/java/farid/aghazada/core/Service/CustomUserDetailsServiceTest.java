package farid.aghazada.core.Service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import farid.aghazada.core.Entity.User;
import farid.aghazada.core.Repository.UserRepository;
import farid.aghazada.core.Security.UserPrincipal;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsernameReturnsUserPrincipalWhenUserExists() {
        User user = new User();
        user.setUsername("Jane.Doe");
        when(userRepository.findByUsername("Jane.Doe")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("Jane.Doe");

        assertThat(result).isInstanceOf(UserPrincipal.class);
        assertThat(result.getUsername()).isEqualTo("Jane.Doe");
    }

    @Test
    void loadUserByUsernameThrowsWhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("unknown"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessage("User not found");
    }
}
