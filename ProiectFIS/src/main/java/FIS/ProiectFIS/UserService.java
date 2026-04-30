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
        User user = userRepository.findByUsername(username);

        return user != null &&
               passwordEncoder.matches(password, user.getPassword());
    }

    public User getUser(String username) {
        return userRepository.findByUsername(username);
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