package md.fiodorov.jqa.service.api;

import md.fiodorov.jqa.auth.AuthProvider;
import md.fiodorov.jqa.domain.User;

public interface AuthenticationService {

  User loginWithPassword(String username, String password);

  User loginWithOAuth(AuthProvider provider, String externalId);
}
