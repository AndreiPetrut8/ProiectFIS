package FIS.ProiectFIS;

import FIS.ProiectFIS.Player;
import FIS.ProiectFIS.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Integer> {

    Player findByUserId(Integer userId);
    Player findByFirstName(String firstName);
    Player findByShirtNumber(int shirtNumber);
    Player findByFirstNameAndShirtNumber(String first_name, int shirtNumber);

    List<Player> findAllByCoachUserId(Integer id);
}
