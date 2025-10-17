package md.fiodorov.jqa.repository;

import java.util.Optional;
import md.fiodorov.jqa.domain.User;

public interface UserRepository {

  Optional<User> findByUsername(String username);

  Optional<User> findByGoogleId(String googleId);

  Optional<User> findByFacebookId(String facebookId);

  User save(User user);
}
