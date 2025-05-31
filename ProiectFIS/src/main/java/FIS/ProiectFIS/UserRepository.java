package FIS.ProiectFIS;

import FIS.ProiectFIS.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    @Query("SELECT u FROM User u WHERE u.id = :id")
    User findByIdNoOptional(@Param("id") Integer id);


    boolean existsByUsername(String username);
}

