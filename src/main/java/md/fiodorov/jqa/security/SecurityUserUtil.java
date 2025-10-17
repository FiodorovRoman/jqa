package md.fiodorov.jqa.security;

import md.fiodorov.jqa.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUserUtil {
  private SecurityUserUtil() {}

  public static User currentUserOrNull() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
      return (User) auth.getPrincipal();
    }
    return null;
  }
}
