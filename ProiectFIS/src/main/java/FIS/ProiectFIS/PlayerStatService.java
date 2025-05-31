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

    public void saveOrUpdateStat(PlayerStat playerStat) {
        PlayerStat existing = playerStatRepository.findByUserIdAndPlayerId(playerStat.getUserId(), playerStat.getPlayerId());
        if (existing != null) {
            existing.setMinutesPlayed(playerStat.getMinutesPlayed());
            existing.setGoals(playerStat.getGoals());
            existing.setAssists(playerStat.getAssists());
            existing.setYellowCards(playerStat.getYellowCards());
            existing.setRedCards(playerStat.getRedCards());
            existing.setPassesCompleted(playerStat.getPassesCompleted());
            existing.setShotsOnTarget(playerStat.getShotsOnTarget());
            playerStatRepository.save(existing);
        } else {
            playerStatRepository.save(playerStat);
        }
    }

}

