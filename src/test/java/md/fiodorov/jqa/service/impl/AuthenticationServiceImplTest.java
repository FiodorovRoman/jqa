package md.fiodorov.jqa.service.impl;

import md.fiodorov.jqa.auth.AuthProvider;
import md.fiodorov.jqa.domain.Role;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.repository.UserRepository;

public class AuthenticationServiceImplTest {

  public static void main(String[] args) {
    AuthenticationServiceImplTest test = new AuthenticationServiceImplTest();
    test.loginWithPassword_returnsUserForValidCredentials();
    test.loginWithPassword_rejectsInvalidCredentials();
    test.loginWithOAuth_allowsGoogleForUserRole();
    test.loginWithOAuth_allowsFacebookForUserRole();
    test.loginWithOAuth_rejectsAdminRole();
    test.loginWithOAuth_rejectsUnknownAccount();
    System.out.println("All AuthenticationServiceImpl tests passed");
  }

  private void loginWithPassword_returnsUserForValidCredentials() {
    StubUserRepository repository = new StubUserRepository();
    User user = new User();
    user.setUsername("alice");
    user.setPassword("secret");
    repository.setUser(user);

    AuthenticationServiceImpl service = new AuthenticationServiceImpl(repository);
    User authenticated = service.loginWithPassword("alice", "secret");

    assertSame(user, authenticated, "Expected repository user to be returned");
  }

  private void loginWithPassword_rejectsInvalidCredentials() {
    StubUserRepository repository = new StubUserRepository();
    User user = new User();
    user.setUsername("alice");
    user.setPassword("secret");
    repository.setUser(user);

    AuthenticationServiceImpl service = new AuthenticationServiceImpl(repository);

    try {
      service.loginWithPassword("alice", "wrong");
      throw new AssertionError("Expected IllegalArgumentException for invalid password");
    } catch (IllegalArgumentException expected) {
      // expected path
    }
  }

  private void loginWithOAuth_allowsGoogleForUserRole() {
    StubUserRepository repository = new StubUserRepository();
    User user = new User();
    user.setUsername("alice");
    user.setGoogleId("google-123");
    user.setRole(Role.USER);
    repository.setUser(user);

    AuthenticationServiceImpl service = new AuthenticationServiceImpl(repository);
    User authenticated = service.loginWithOAuth(AuthProvider.GOOGLE, "google-123");

    assertSame(user, authenticated, "Expected google linked user to be returned");
  }

  private void loginWithOAuth_allowsFacebookForUserRole() {
    StubUserRepository repository = new StubUserRepository();
    User user = new User();
    user.setUsername("bob");
    user.setFacebookId("fb-123");
    user.setRole(Role.USER);
    repository.setUser(user);

    AuthenticationServiceImpl service = new AuthenticationServiceImpl(repository);
    User authenticated = service.loginWithOAuth(AuthProvider.FACEBOOK, "fb-123");

    assertSame(user, authenticated, "Expected facebook linked user to be returned");
  }

  private void loginWithOAuth_rejectsAdminRole() {
    StubUserRepository repository = new StubUserRepository();
    User user = new User();
    user.setUsername("admin");
    user.setGoogleId("admin-google");
    user.setRole(Role.ADMIN);
    repository.setUser(user);

    AuthenticationServiceImpl service = new AuthenticationServiceImpl(repository);

    try {
      service.loginWithOAuth(AuthProvider.GOOGLE, "admin-google");
      throw new AssertionError("Expected IllegalStateException for admin OAuth login");
    } catch (IllegalStateException expected) {
      assertTrue(
          expected
              .getMessage()
              .contains("Administrators must sign in with username and password"),
          "Exception message should mention admin restriction");
    }
  }

  private void loginWithOAuth_rejectsUnknownAccount() {
    StubUserRepository repository = new StubUserRepository();
    AuthenticationServiceImpl service = new AuthenticationServiceImpl(repository);

    try {
      service.loginWithOAuth(AuthProvider.GOOGLE, "missing");
      throw new AssertionError("Expected IllegalArgumentException for missing account");
    } catch (IllegalArgumentException expected) {
      assertTrue(
          expected.getMessage().contains("Google account not linked"),
          "Exception message should mention missing Google link");
    }
  }

  private void assertSame(Object expected, Object actual, String message) {
    if (expected != actual) {
      throw new AssertionError(message + ": expected reference=" + expected + ", actual=" + actual);
    }
  }

  private void assertTrue(boolean condition, String message) {
    if (!condition) {
      throw new AssertionError(message);
    }
  }

  private static final class StubUserRepository implements UserRepository {

    private User user;

    void setUser(User user) {
      this.user = user;
    }

    @Override
    public java.util.Optional<User> findByUsername(String username) {
      if (user != null && username != null && username.equals(user.getUsername())) {
        return java.util.Optional.of(user);
      }
      return java.util.Optional.empty();
    }

    @Override
    public java.util.Optional<User> findByGoogleId(String googleId) {
      if (user != null && googleId != null && googleId.equals(user.getGoogleId())) {
        return java.util.Optional.of(user);
      }
      return java.util.Optional.empty();
    }

    @Override
    public java.util.Optional<User> findByFacebookId(String facebookId) {
      if (user != null && facebookId != null && facebookId.equals(user.getFacebookId())) {
        return java.util.Optional.of(user);
      }
      return java.util.Optional.empty();
    }
  }
}
