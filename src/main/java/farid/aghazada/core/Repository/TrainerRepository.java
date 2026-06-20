package farid.aghazada.core.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import farid.aghazada.core.Entity.Trainer;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    @Query("SELECT t FROM Trainer t JOIN FETCH t.user JOIN FETCH t.specialization LEFT JOIN FETCH t.trainees WHERE t.user.username = :username")
    Optional<Trainer> findByUserUsername(@Param("username") String username);

    List<Trainer> findByUserUsernameIn(List<String> usernames);

    long countByUserIsActive(boolean isActive);

    @Query("""
           SELECT tr
           FROM Trainer tr
           WHERE tr.id NOT IN (
               SELECT assigned.id
               FROM Trainee t
               JOIN t.trainers assigned
               WHERE t.user.username = :traineeUsername
           )
           """)
    List<Trainer> findTrainersNotAssignedToTrainee(@Param("traineeUsername") String traineeUsername);
}
