package md.fiodorov.jqa.service.impl;

import md.fiodorov.jqa.auth.AuthProvider;
import md.fiodorov.jqa.domain.Role;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.repository.UserRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationServiceImplTest {

  private static final org.springframework.security.crypto.password.PasswordEncoder NOOP_ENCODER = new org.springframework.security.crypto.password.PasswordEncoder() {
    @Override public String encode(CharSequence rawPassword) { return rawPassword == null ? null : rawPassword.toString(); }
    @Override public boolean matches(CharSequence rawPassword, String encodedPassword) { return encodedPassword != null && encodedPassword.equals(rawPassword == null ? null : rawPassword.toString()); }
    @Override public boolean upgradeEncoding(String encodedPassword) { return false; }
  };

  private static final OAuthTokenVerifier PASS_THROUGH = token -> token;

  @Test
  void loginWithPassword_returnsUserForValidCredentials() {
    StubUserRepository repository = new StubUserRepository();
    User user = new User();
    user.setUsername("alice");
    user.setPassword(NOOP_ENCODER.encode("secret"));
    repository.setUser(user);

    AuthenticationServiceImpl service = new AuthenticationServiceImpl(repository, NOOP_ENCODER, PASS_THROUGH, PASS_THROUGH);
    User authenticated = service.loginWithPassword("alice", "secret");

    assertSame(user, authenticated, "Expected repository user to be returned");
  }

  @Test
  void loginWithPassword_rejectsInvalidCredentials() {
    StubUserRepository repository = new StubUserRepository();
    User user = new User();
    user.setUsername("alice");
    user.setPassword(NOOP_ENCODER.encode("secret"));
    repository.setUser(user);

    AuthenticationServiceImpl service = new AuthenticationServiceImpl(repository, NOOP_ENCODER, PASS_THROUGH, PASS_THROUGH);

    assertThrows(IllegalArgumentException.class,
        () -> service.loginWithPassword("alice", "wrong"),
        "Expected IllegalArgumentException for invalid password");
  }

  @Test
  void loginWithOAuth_allowsGoogleForUserRole() {
    StubUserRepository repository = new StubUserRepository();
    User user = new User();
    user.setUsername("alice");
    user.setGoogleId("google-123");
    user.setRole(Role.USER);
    repository.setUser(user);

    AuthenticationServiceImpl service = new AuthenticationServiceImpl(repository, NOOP_ENCODER, PASS_THROUGH, PASS_THROUGH);
    User authenticated = service.loginWithOAuth(AuthProvider.GOOGLE, "google-123");

    assertSame(user, authenticated, "Expected google linked user to be returned");
  }

  @Test
  void loginWithOAuth_allowsFacebookForUserRole() {
    StubUserRepository repository = new StubUserRepository();
    User user = new User();
    user.setUsername("bob");
    user.setFacebookId("fb-123");
    user.setRole(Role.USER);
    repository.setUser(user);

    AuthenticationServiceImpl service = new AuthenticationServiceImpl(repository, NOOP_ENCODER, PASS_THROUGH, PASS_THROUGH);
    User authenticated = service.loginWithOAuth(AuthProvider.FACEBOOK, "fb-123");

    assertSame(user, authenticated, "Expected facebook linked user to be returned");
  }

  @Test
  void loginWithOAuth_rejectsAdminRole() {
    StubUserRepository repository = new StubUserRepository();
    User user = new User();
    user.setUsername("admin");
    user.setGoogleId("admin-google");
    user.setRole(Role.ADMIN);
    repository.setUser(user);

    AuthenticationServiceImpl service = new AuthenticationServiceImpl(repository, NOOP_ENCODER, PASS_THROUGH, PASS_THROUGH);

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> service.loginWithOAuth(AuthProvider.GOOGLE, "admin-google"),
        "Expected IllegalStateException for admin OAuth login");
    assertTrue(
        ex.getMessage().contains("Administrators must sign in with username and password"),
        "Exception message should mention admin restriction");
  }

  @Test
  void loginWithOAuth_rejectsUnknownAccount() {
    StubUserRepository repository = new StubUserRepository();
    AuthenticationServiceImpl service = new AuthenticationServiceImpl(repository, NOOP_ENCODER, PASS_THROUGH, PASS_THROUGH);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> service.loginWithOAuth(AuthProvider.GOOGLE, "missing"),
        "Expected IllegalArgumentException for missing account");
    assertTrue(
        ex.getMessage().contains("Google account not linked"),
        "Exception message should mention missing Google link");
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

    @Override
    public User save(User user) {
      this.user = user;
      return user;
    }
  }
}
