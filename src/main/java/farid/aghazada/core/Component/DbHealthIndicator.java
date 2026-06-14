package farid.aghazada.core.Component;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import farid.aghazada.core.Repository.UserRepository;

@Component
public class DbHealthIndicator implements HealthIndicator {

    private final UserRepository userRepository;

    public DbHealthIndicator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public @Nullable Health health() {
        try {
            long userCount = userRepository.count();
            return Health.up()
                .withDetail("totalUsers", userCount)
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }

}

