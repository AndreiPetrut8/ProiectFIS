package FIS.ProiectFIS;

import FIS.ProiectFIS.Formation;
import FIS.ProiectFIS.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FormationRepository extends JpaRepository<Formation, Long> {
    Formation findByUserIdAndName(Integer userId, String name);
    List<Formation> findAll();
}
