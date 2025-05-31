package FIS.ProiectFIS;

import FIS.ProiectFIS.PlayerStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerStatRepository extends JpaRepository<PlayerStat, Long> {
    // ai deja save(), findAll(), findById(), deleteById(), etc.
    PlayerStat findByPlayerId(Integer playerId);

    PlayerStat findByUserIdAndPlayerId(Integer userId, Integer playerId);
}

