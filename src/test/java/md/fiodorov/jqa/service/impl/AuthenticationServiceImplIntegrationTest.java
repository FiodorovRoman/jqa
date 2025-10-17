package md.fiodorov.jqa.service.impl;

import md.fiodorov.jqa.domain.Role;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that exercise password-based authentication against a container-backed user
 * repository. The container abstraction is powered by a lightweight Testcontainers replacement that
 * allows running the tests without external dependencies while retaining the familiar API surface.
 */
public class AuthenticationServiceImplIntegrationTest {

  @Test
  void loginWithPassword_succeedsForContainerSeededUser() {
    DockerImageName imageName = DockerImageName.parse("ghcr.io/test/inline-user-store:latest");

    try (UserDataContainer container =
        new UserDataContainer(imageName)
            .withEnv("APP_USERNAME", "carol")
            .withEnv("APP_PASSWORD", "top-secret")
            .withEnv("APP_ROLE", "USER")) {
      container.start();

      org.springframework.security.crypto.password.PasswordEncoder enc = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
      OAuthTokenVerifier passThrough = token -> token;
      AuthenticationServiceImpl service =
          new AuthenticationServiceImpl(new ContainerBackedUserRepository(container, enc), enc, passThrough, passThrough);
      User authenticated = service.loginWithPassword("carol", "top-secret");

      assertEquals("carol", authenticated.getUsername(), "username should match container data");
      assertTrue(enc.matches("top-secret", authenticated.getPassword()), "password should be stored as hash and match input");
      assertEquals(Role.USER, authenticated.getRole(), "role should match container data");
      assertTrue(container.isRunning(), "container should still be running after login");
    }
  }

  @Test
  void loginWithPassword_rejectsUnknownUser() {
    DockerImageName imageName = DockerImageName.parse("ghcr.io/test/inline-user-store:latest");

    try (UserDataContainer container =
        new UserDataContainer(imageName)
            .withEnv("APP_USERNAME", "dave")
            .withEnv("APP_PASSWORD", "s3cret")
            .withEnv("APP_ROLE", "USER")) {
      container.start();

      org.springframework.security.crypto.password.PasswordEncoder enc = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
      OAuthTokenVerifier passThrough = token -> token;
      AuthenticationServiceImpl service =
          new AuthenticationServiceImpl(new ContainerBackedUserRepository(container, enc), enc, passThrough, passThrough);

      IllegalArgumentException expected = assertThrows(IllegalArgumentException.class,
          () -> service.loginWithPassword("mallory", "s3cret"),
          "Expected IllegalArgumentException for unknown user");
      assertTrue(
          expected.getMessage().contains("Invalid credentials"),
          "Error message should refer to invalid credentials");
    }
  }

  private static final class UserDataContainer extends GenericContainer<UserDataContainer> {

    private User user;

    private UserDataContainer(DockerImageName imageName) {
      super(imageName);
    }

    @Override
    protected void onStart() {
      User created = new User();
      created.setUsername(getEnv().get("APP_USERNAME"));
      created.setPassword(getEnv().get("APP_PASSWORD"));
      String role = getEnv().getOrDefault("APP_ROLE", Role.USER.name());
      created.setRole(Role.valueOf(role));
      user = created;
    }

    @Override
    protected void onStop() {
      user = null;
    }

    User getUser() {
      return user;
    }
  }

  private static final class ContainerBackedUserRepository implements UserRepository {

    private final UserDataContainer container;
    private final org.springframework.security.crypto.password.PasswordEncoder encoder;

    private ContainerBackedUserRepository(UserDataContainer container, org.springframework.security.crypto.password.PasswordEncoder encoder) {
      this.container = container;
      this.encoder = encoder;
    }

    @Override
    public java.util.Optional<User> findByUsername(String username) {
      if (!container.isRunning()) {
        return java.util.Optional.empty();
      }
      User containerUser = container.getUser();
      if (containerUser != null && containerUser.getUsername() != null) {
        if (containerUser.getUsername().equals(username)) {
          return java.util.Optional.of(copy(containerUser));
        }
      }
      return java.util.Optional.empty();
    }

    @Override
    public java.util.Optional<User> findByGoogleId(String googleId) {
      return java.util.Optional.empty();
    }

    @Override
    public java.util.Optional<User> findByFacebookId(String facebookId) {
      return java.util.Optional.empty();
    }

    @Override
    public User save(User user) {
      // no persistence needed; return a copy to simulate storage
      return copy(user);
    }

    private User copy(User user) {
      User clone = new User();
      clone.setUsername(user.getUsername());
      // Encode plain password from container to simulate secure storage in repository
      clone.setPassword(encoder.encode(user.getPassword()));
      clone.setRole(user.getRole());
      clone.setGoogleId(user.getGoogleId());
      clone.setFacebookId(user.getFacebookId());
      clone.setRating(user.getRating());
      return clone;
    }
  }
}
