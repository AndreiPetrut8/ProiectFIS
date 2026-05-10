package FIS.ProiectFIS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

   @Autowired
private PasswordEncoder passwordEncoder;

    public boolean authenticate(String username, String password) {
        User user = userRepository.findFirstByUsername(username);

        if (user == null) return false;

        String stored = user.getPassword();

        // Normal case: stored is a bcrypt hash
        if (passwordEncoder.matches(password, stored)) {
            return true;
        }

        // Migration case: stored password is still plain text
        if (stored != null && stored.equals(password)) {
            String encoded = passwordEncoder.encode(password);
            user.setPassword(encoded);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    public User getUser(String username) {
        return userRepository.findFirstByUsername(username);
    }

  public void save(User user) {
    //System.out.println("SERVICE SAVE CALLED");
    //System.out.println("BEFORE: " + user.getPassword());

    String encoded = passwordEncoder.encode(user.getPassword());

    //System.out.println("AFTER: " + encoded);

    user.setPassword(encoded);
    userRepository.save(user);
}

    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }
}