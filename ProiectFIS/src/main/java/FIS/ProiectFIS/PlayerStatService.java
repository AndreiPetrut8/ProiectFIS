package FIS.ProiectFIS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PlayerStatService {

    @Autowired
    private PlayerStatRepository playerStatRepository;

    public void saveStat(PlayerStat playerStat) {
        playerStatRepository.save(playerStat);
    }
}

