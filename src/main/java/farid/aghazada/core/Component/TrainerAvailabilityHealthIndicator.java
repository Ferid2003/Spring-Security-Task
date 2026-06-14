package farid.aghazada.core.Component;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import farid.aghazada.core.Repository.TrainerRepository;

@Component
public class TrainerAvailabilityHealthIndicator implements HealthIndicator {

    private final TrainerRepository trainerRepository;

    public TrainerAvailabilityHealthIndicator(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Override
    public @Nullable Health health() {
        long activeTrainers = trainerRepository.countByUserIsActive(true);
        if (activeTrainers > 0) {
            return Health.up()
                    .withDetail("activeTrainers", activeTrainers)
                    .build();
        } else {
            return Health.down()
                    .withDetail("activeTrainers", 0)
                    .withDetail("message", "No active trainers available — new training sessions cannot be assigned")
                    .build();
        }
    }
}
