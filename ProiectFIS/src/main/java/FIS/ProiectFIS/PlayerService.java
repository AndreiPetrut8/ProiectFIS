package FIS.ProiectFIS;

import FIS.ProiectFIS.Player;
import FIS.ProiectFIS.User;
import FIS.ProiectFIS.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    public void savePlayer(Player player) {
        playerRepository.save(player);
    }

    public List<Player> getPlayersByUser(User user) {
        return playerRepository.findByUser(user);
    }

    public boolean shirtNumberExists(int shirtNumber) {
        return playerRepository.findByShirtNumber(shirtNumber).isPresent();
    }

    public Player getPlayerById(Integer id) {
        return playerRepository.findById(id).orElse(null);
    }

    public void deletePlayer(Integer id) {
        playerRepository.deleteById(id);
    }

    public Player getByFirstNameAndShirtNumber(String name, int shirtNumber) {
        return playerRepository.findByFirstNameAndShirtNumber(name, shirtNumber);
    }



}
