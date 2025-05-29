package FIS.ProiectFIS;

import FIS.ProiectFIS.Player;
import FIS.ProiectFIS.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Integer> {
    List<Player> findByUser(User user);  // toți jucătorii adăugați de un user (antrenor)
    Optional<Player> findByShirtNumber(int shirtNumber);  // pentru a verifica duplicat
    Player findByFirstNameAndShirtNumber(String first_name, int shirtNumber);

}
