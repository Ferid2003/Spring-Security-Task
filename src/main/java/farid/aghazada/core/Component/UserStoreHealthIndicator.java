package farid.aghazada.core.Component;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import farid.aghazada.core.Repository.TraineeRepository;
import farid.aghazada.core.Repository.TrainerRepository;
import farid.aghazada.core.Repository.TrainingRepository;

@Component
public class UserStoreHealthIndicator implements HealthIndicator {

    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingRepository trainingRepository;

    public UserStoreHealthIndicator(TrainerRepository trainerRepository,
                                    TraineeRepository traineeRepository,
                                    TrainingRepository trainingRepository) {
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.trainingRepository = trainingRepository;
    }

    @Override
    public @Nullable Health health() {
        return Health.up()
                .withDetail("totalTrainers", trainerRepository.count())
                .withDetail("totalTrainees", traineeRepository.count())
                .withDetail("totalTrainingSessions", trainingRepository.count())
                .build();
    }
}
