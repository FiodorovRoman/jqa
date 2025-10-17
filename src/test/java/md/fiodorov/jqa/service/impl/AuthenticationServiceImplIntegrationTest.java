package md.fiodorov.jqa.service.impl;

import md.fiodorov.jqa.domain.Role;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.repository.UserRepository;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Integration tests that exercise password-based authentication against a container-backed user
 * repository. The container abstraction is powered by a lightweight Testcontainers replacement that
 * allows running the tests without external dependencies while retaining the familiar API surface.
 */
public class AuthenticationServiceImplIntegrationTest {

  public static void main(String[] args) {
    AuthenticationServiceImplIntegrationTest test =
        new AuthenticationServiceImplIntegrationTest();
    test.loginWithPassword_succeedsForContainerSeededUser();
    test.loginWithPassword_rejectsUnknownUser();
    System.out.println("All AuthenticationServiceImpl integration tests passed");
  }

  private void loginWithPassword_succeedsForContainerSeededUser() {
    DockerImageName imageName = DockerImageName.parse("ghcr.io/test/inline-user-store:latest");

    try (UserDataContainer container =
        new UserDataContainer(imageName)
            .withEnv("APP_USERNAME", "carol")
            .withEnv("APP_PASSWORD", "top-secret")
            .withEnv("APP_ROLE", "USER")) {
      container.start();

      AuthenticationServiceImpl service =
          new AuthenticationServiceImpl(new ContainerBackedUserRepository(container));
      User authenticated = service.loginWithPassword("carol", "top-secret");

      assertEquals("carol", authenticated.getUsername(), "username should match container data");
      assertEquals("top-secret", authenticated.getPassword(), "password should match container data");
      assertEquals(Role.USER, authenticated.getRole(), "role should match container data");
      assertTrue(container.isRunning(), "container should still be running after login");
    }
  }

  private void loginWithPassword_rejectsUnknownUser() {
    DockerImageName imageName = DockerImageName.parse("ghcr.io/test/inline-user-store:latest");

    try (UserDataContainer container =
        new UserDataContainer(imageName)
            .withEnv("APP_USERNAME", "dave")
            .withEnv("APP_PASSWORD", "s3cret")
            .withEnv("APP_ROLE", "USER")) {
      container.start();

      AuthenticationServiceImpl service =
          new AuthenticationServiceImpl(new ContainerBackedUserRepository(container));

      try {
        service.loginWithPassword("mallory", "s3cret");
        throw new AssertionError("Expected IllegalArgumentException for unknown user");
      } catch (IllegalArgumentException expected) {
        assertTrue(
            expected.getMessage().contains("Invalid credentials"),
            "Error message should refer to invalid credentials");
      }
    }
  }

  private void assertEquals(Object expected, Object actual, String message) {
    if (expected == null ? actual != null : !expected.equals(actual)) {
      throw new AssertionError(message + ": expected=" + expected + ", actual=" + actual);
    }
  }

  private void assertTrue(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
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

    private ContainerBackedUserRepository(UserDataContainer container) {
      this.container = container;
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

    private User copy(User user) {
      User clone = new User();
      clone.setUsername(user.getUsername());
      clone.setPassword(user.getPassword());
      clone.setRole(user.getRole());
      clone.setGoogleId(user.getGoogleId());
      clone.setFacebookId(user.getFacebookId());
      return clone;
    }
  }
}
