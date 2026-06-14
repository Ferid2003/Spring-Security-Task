package farid.aghazada.core.Service;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import farid.aghazada.core.Repository.TraineeRepository;
import farid.aghazada.core.Repository.TrainerRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class GymMetricsService {

    private final Counter trainerRegistrations;
    private final Counter traineeRegistrations;
    private final Counter trainingsCreated;
    private final Counter authFailures;

    private final AtomicLong activeTrainersCount = new AtomicLong(0);
    private final AtomicLong activeTraineesCount = new AtomicLong(0);

    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;

    public GymMetricsService(MeterRegistry registry,
                              TrainerRepository trainerRepository,
                              TraineeRepository traineeRepository) {
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;

        this.trainerRegistrations = Counter.builder("gym.trainers.registrations.total")
                .description("Total number of trainer registrations")
                .register(registry);

        this.traineeRegistrations = Counter.builder("gym.trainees.registrations.total")
                .description("Total number of trainee registrations")
                .register(registry);

        this.trainingsCreated = Counter.builder("gym.trainings.created.total")
                .description("Total number of training sessions created")
                .register(registry);

        this.authFailures = Counter.builder("gym.authentication.failures.total")
                .description("Total number of authentication failures")
                .register(registry);

        Gauge.builder("gym.active.trainers", activeTrainersCount, AtomicLong::get)
                .description("Current number of active trainers")
                .register(registry);

        Gauge.builder("gym.active.trainees", activeTraineesCount, AtomicLong::get)
                .description("Current number of active trainees")
                .register(registry);
    }

    public void incrementTrainerRegistrations() {
        trainerRegistrations.increment();
    }

    public void incrementTraineeRegistrations() {
        traineeRegistrations.increment();
    }

    public void incrementTrainingsCreated() {
        trainingsCreated.increment();
    }

    public void incrementAuthFailures() {
        authFailures.increment();
    }

    @Scheduled(fixedRateString = "${gym.metrics.gauge.refresh-rate-ms:60000}")
    public void updateGauges() {
        activeTrainersCount.set(trainerRepository.countByUserIsActive(true));
        activeTraineesCount.set(traineeRepository.countByUserIsActive(true));
    }
}
