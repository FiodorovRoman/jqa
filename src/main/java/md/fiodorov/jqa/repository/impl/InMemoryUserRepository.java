package md.fiodorov.jqa.repository.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import md.fiodorov.jqa.domain.Role;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.repository.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryUserRepository implements UserRepository {

  private final Map<String, User> byUsername = new HashMap<>();
  private final Map<String, User> byGoogle = new HashMap<>();
  private final Map<String, User> byFacebook = new HashMap<>();
  private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

  public InMemoryUserRepository(org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
    // Seed demo users
    User alice = new User();
    alice.setId(1L);
    alice.setUsername("alice");
    alice.setPassword(passwordEncoder.encode("secret"));
    alice.setGoogleId("google-123");
    alice.setRole(Role.USER);
    register(alice);

    User bob = new User();
    bob.setId(2L);
    bob.setUsername("bob");
    bob.setPassword(passwordEncoder.encode("pass"));
    bob.setFacebookId("fb-123");
    bob.setRole(Role.USER);
    register(bob);

    User admin = new User();
    admin.setId(3L);
    admin.setUsername("admin");
    admin.setPassword(passwordEncoder.encode("adminpass"));
    admin.setRole(Role.ADMIN);
    register(admin);
  }

  private void register(User u) {
    if (u.getUsername() != null) {
      byUsername.put(u.getUsername(), u);
    }
    if (u.getGoogleId() != null) {
      byGoogle.put(u.getGoogleId(), u);
    }
    if (u.getFacebookId() != null) {
      byFacebook.put(u.getFacebookId(), u);
    }
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return Optional.ofNullable(byUsername.get(username));
  }

  @Override
  public Optional<User> findByGoogleId(String googleId) {
    return Optional.ofNullable(byGoogle.get(googleId));
  }

  @Override
  public Optional<User> findByFacebookId(String facebookId) {
    return Optional.ofNullable(byFacebook.get(facebookId));
  }

  @Override
  public User save(User user) {
    if (user.getUsername() != null) {
      byUsername.put(user.getUsername(), user);
    }
    if (user.getGoogleId() != null) {
      byGoogle.put(user.getGoogleId(), user);
    }
    if (user.getFacebookId() != null) {
      byFacebook.put(user.getFacebookId(), user);
    }
    return user;
  }
}
