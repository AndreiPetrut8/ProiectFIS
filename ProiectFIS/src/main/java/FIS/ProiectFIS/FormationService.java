package FIS.ProiectFIS;

import FIS.ProiectFIS.Formation;
import FIS.ProiectFIS.User;
import FIS.ProiectFIS.FormationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FormationService {

    @Autowired
    private FormationRepository formationRepository;

    public void saveFormation(Formation formation) {
        formationRepository.save(formation);
    }

    public void saveOrUpdateFormation(Formation formation) {
        Formation existing = formationRepository.findByUserIdAndName(formation.getUserId(), formation.getName());

        if (existing != null) {
            existing.setDescription(formation.getDescription());

            formationRepository.save(existing);
        } else {
            formationRepository.save(formation);
        }
    }


}

