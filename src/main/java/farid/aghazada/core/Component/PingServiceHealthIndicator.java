package farid.aghazada.core.Component;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class PingServiceHealthIndicator implements HealthIndicator {

    private final long startTime = System.currentTimeMillis();

    @Override
    public @Nullable Health health() {
        long uptimeMs = System.currentTimeMillis() - startTime;
        return Health.up()
            .withDetail("status", "Application is running")
            .withDetail("uptimeMs", uptimeMs)
            .build();
    }

}

