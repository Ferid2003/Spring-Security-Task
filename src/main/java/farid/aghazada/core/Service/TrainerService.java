package farid.aghazada.core.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import farid.aghazada.core.DTO.PasswordChangeDto;
import farid.aghazada.core.DTO.RegistrationResponseDto;
import farid.aghazada.core.DTO.Trainer.TrainerProfileResponseDto;
import farid.aghazada.core.DTO.Trainer.TrainerRegistrationDto;
import farid.aghazada.core.DTO.Trainer.TrainerUpdateDto;
import farid.aghazada.core.DTO.Trainer.TrainerUpdateProfileResponseDto;
import farid.aghazada.core.DTO.Training.TrainingTrainerCriteriaDto;
import farid.aghazada.core.DTO.Training.TrainingTrainerResponseDto;
import farid.aghazada.core.Entity.Trainer;
import farid.aghazada.core.Entity.TrainingType;
import farid.aghazada.core.Exception.UserNotFoundException;
import farid.aghazada.core.Repository.TrainerRepository;
import farid.aghazada.core.Repository.TrainingRepository;
import farid.aghazada.core.Repository.TrainingTypeRepository;

@Service
public class TrainerService {

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    private HelperService helperService;

    private final PasswordEncoder passwordEncoder;

    public TrainerService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private GymMetricsService gymMetricsService;

    @Autowired
    public void setHelperService(HelperService helperService) {
        this.helperService = helperService;
    }

    @Transactional
    public RegistrationResponseDto createTrainer(TrainerRegistrationDto dto) {
        String username = helperService.generateUsername(dto.firstName(), dto.lastName());
        String password = helperService.generatePassword();
        String encodedPassword = passwordEncoder.encode(password);
        Trainer trainer = TrainerRegistrationDto.toTrainer(dto, username, encodedPassword);

        if (dto.trainingType() != null) {
            TrainingType trainingType = trainingTypeRepository.findById(dto.trainingType().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Training type not found: " + dto.trainingType().getId()));
            trainer.setSpecialization(trainingType);
        }

        trainerRepository.save(trainer);
        gymMetricsService.incrementTrainerRegistrations();

        return RegistrationResponseDto.toRegistrationResponseDto(username, password);
    }

    public TrainerProfileResponseDto getTrainerByUsername(String username) {
        return TrainerProfileResponseDto.toTrainerProfileResponseDto(findByUsernameOrThrow(username));
    }

    @Transactional
    public TrainerUpdateProfileResponseDto updateTrainer(String username, TrainerUpdateDto dto) {
        Trainer trainer = findByUsernameOrThrow(username);

        if(dto.username() != null && !dto.username().isBlank()) {
            trainer.getUser().setUsername(dto.username());
        }
        if (dto.firstName() != null && !dto.firstName().isBlank()) {
            trainer.getUser().setFirstName(dto.firstName());
        }
        if (dto.lastName() != null && !dto.lastName().isBlank()) {
            trainer.getUser().setLastName(dto.lastName());
        }
        if (dto.specialization() != null) {
            TrainingType trainingType = trainingTypeRepository.findById(dto.specialization().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Training type not found: " + dto.specialization().getId()));
            trainer.setSpecialization(trainingType);
        }
        if (dto.isActive() != null) {
            trainer.getUser().setActive(dto.isActive());
        }

        return TrainerUpdateProfileResponseDto.toTrainerUpdateProfileResponseDto(trainer);
    }

    @Transactional
    public void changePassword(String username, PasswordChangeDto dto) {
        Trainer trainer = findByUsernameOrThrow(username);

        if (!passwordEncoder.matches(dto.oldPassword(), trainer.getUser().getPassword())) {
            throw new IllegalArgumentException("Old password does not match");
        }

        trainer.getUser().setPassword(passwordEncoder.encode(dto.newPassword()));
    }

    @Transactional
    public void changeIsActive(String username, boolean isActive) {
        Trainer trainer = findByUsernameOrThrow(username);
        if(trainer.getUser().isActive() != isActive) {
            trainer.getUser().setActive(isActive);
        }else {
            throw new IllegalStateException("Trainer is already " + (isActive ? "active" : "inactive"));
        }
    }

    public List<TrainingTrainerResponseDto> getTrainingsByCriteria(String username, TrainingTrainerCriteriaDto criteria) {
        LocalDate fromDate = criteria == null ? null : criteria.fromDate();
        LocalDate toDate = criteria == null ? null : criteria.toDate();
        String traineeName = criteria == null ? null : criteria.traineeName();
        return trainingRepository.findTrainerTrainingByCriteria(username, fromDate, toDate, traineeName).stream()
                .map(TrainingTrainerResponseDto::toTrainingResponseDto)
                .toList();
    }

    private Trainer findByUsernameOrThrow(String username) {
        return trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Trainer with username " + username + " not found"));
    }
}
