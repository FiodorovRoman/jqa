package md.fiodorov.jqa.service.impl;

/**
 * Simple abstraction to validate an OAuth access/id token and extract the external provider user id.
 */
public interface OAuthTokenVerifier {
  /**
   * Validates the token and returns the provider user id (e.g., Google sub or Facebook user_id).
   * Implementations may return the input if it already looks like an id (for backward-compat during tests).
   * @throws IllegalArgumentException if the token is invalid or cannot be verified
   */
  String verifyAndGetUserId(String token);
}
