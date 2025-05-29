package FIS.ProiectFIS;

import FIS.ProiectFIS.Formation;
import FIS.ProiectFIS.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormationRepository extends JpaRepository<Formation, Long> {
    List<Formation> findByUser(User user);
}
