package md.fiodorov.jqa.service.impl;

import java.util.Objects;
import md.fiodorov.jqa.auth.AuthProvider;
import md.fiodorov.jqa.domain.Role;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.repository.UserRepository;
import md.fiodorov.jqa.service.api.AuthenticationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final OAuthTokenVerifier googleVerifier;
  private final OAuthTokenVerifier facebookVerifier;

  public AuthenticationServiceImpl(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   @org.springframework.beans.factory.annotation.Qualifier("googleVerifier") OAuthTokenVerifier googleVerifier,
                                   @org.springframework.beans.factory.annotation.Qualifier("facebookVerifier") OAuthTokenVerifier facebookVerifier) {
    this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
    this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder");
    this.googleVerifier = Objects.requireNonNull(googleVerifier, "googleVerifier");
    this.facebookVerifier = Objects.requireNonNull(facebookVerifier, "facebookVerifier");
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

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new IllegalArgumentException("Invalid credentials");
    }

    return user;
  }

  @Override
  public User loginWithOAuth(AuthProvider provider, String token) {
    Objects.requireNonNull(provider, "provider");
    if (isBlank(token)) {
      throw new IllegalArgumentException("External account id is required");
    }

    // Treat the second argument as an OAuth token and verify it to extract the external user id
    String externalId;
    switch (provider) {
      case GOOGLE -> externalId = googleVerifier.verifyAndGetUserId(token);
      case FACEBOOK -> externalId = facebookVerifier.verifyAndGetUserId(token);
      default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
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
