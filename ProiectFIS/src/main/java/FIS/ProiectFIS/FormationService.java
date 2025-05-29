package FIS.ProiectFIS;

import FIS.ProiectFIS.Formation;
import FIS.ProiectFIS.User;
import FIS.ProiectFIS.FormationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormationService {

    @Autowired
    private FormationRepository formationRepository;

    public void saveFormation(Formation formation) {
        formationRepository.save(formation);
    }

    public List<Formation> getFormationsByUser(User user) {
        return formationRepository.findByUser(user);
    }
}

