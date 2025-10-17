package md.fiodorov.jqa.service.impl;

import java.util.Objects;
import md.fiodorov.jqa.auth.AuthProvider;
import md.fiodorov.jqa.domain.Role;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.repository.UserRepository;
import md.fiodorov.jqa.service.api.AuthenticationService;

public class AuthenticationServiceImpl implements AuthenticationService {

  private final UserRepository userRepository;

  public AuthenticationServiceImpl(UserRepository userRepository) {
    this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
  }

  @Override
  public User loginWithPassword(String username, String password) {
    if (isBlank(username) || isBlank(password)) {
      throw new IllegalArgumentException("Username and password are required");
    }

    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    if (!password.equals(user.getPassword())) {
      throw new IllegalArgumentException("Invalid credentials");
    }

    return user;
  }

  @Override
  public User loginWithOAuth(AuthProvider provider, String externalId) {
    Objects.requireNonNull(provider, "provider");
    if (isBlank(externalId)) {
      throw new IllegalArgumentException("External account id is required");
    }

    User user =
        switch (provider) {
          case GOOGLE ->
              userRepository
                  .findByGoogleId(externalId)
                  .orElseThrow(() -> new IllegalArgumentException("Google account not linked"));
          case FACEBOOK ->
              userRepository
                  .findByFacebookId(externalId)
                  .orElseThrow(() -> new IllegalArgumentException("Facebook account not linked"));
        };

    if (user.getRole() == Role.ADMIN) {
      throw new IllegalStateException("Administrators must sign in with username and password");
    }

    return user;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
