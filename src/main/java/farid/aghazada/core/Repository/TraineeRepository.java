package farid.aghazada.core.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import farid.aghazada.core.Entity.Trainee;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    @Query("SELECT t FROM Trainee t JOIN FETCH t.user LEFT JOIN FETCH t.trainers tr JOIN FETCH tr.user LEFT JOIN FETCH t.trainings WHERE t.user.username = :username")
    Optional<Trainee> findByUserUsername(@Param("username") String username);

    List<Trainee> findByUserUsernameIn(List<String> usernames);

    long countByUserIsActive(boolean isActive);

}
