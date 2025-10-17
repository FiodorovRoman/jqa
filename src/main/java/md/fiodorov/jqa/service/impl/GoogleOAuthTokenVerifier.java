package md.fiodorov.jqa.service.impl;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component("googleVerifier")
@Primary
public class GoogleOAuthTokenVerifier implements OAuthTokenVerifier {

  private final RestTemplate restTemplate;
  private final String tokenInfoUrl;

  public GoogleOAuthTokenVerifier(RestTemplate restTemplate,
                                  @Value("${oauth.google.tokenInfoUrl:https://oauth2.googleapis.com/tokeninfo}") String tokenInfoUrl) {
    this.restTemplate = restTemplate;
    this.tokenInfoUrl = tokenInfoUrl;
  }

  @Override
  public String verifyAndGetUserId(String token) {
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException("Token must not be blank");
    }
    // Backward compatibility: if it doesn't look like a JWT (no dots), treat as already an id
    if (!token.contains(".")) {
      return token;
    }
    try {
      // Google supports both access_token introspection and id_token tokeninfo endpoints; we target tokeninfo
      // For id_token use: tokeninfo?id_token=...; for access_token it's largely deprecated.
      @SuppressWarnings("unchecked")
      Map<String, Object> map = restTemplate.getForObject(tokenInfoUrl + "?id_token={token}", Map.class, token);
      if (map == null) {
        throw new IllegalArgumentException("Empty response from Google tokeninfo");
      }
      Object sub = map.get("sub");
      if (sub == null) {
        throw new IllegalArgumentException("Google token does not contain 'sub'");
      }
      return sub.toString();
    } catch (RestClientException e) {
      throw new IllegalArgumentException("Invalid Google token", e);
    }
  }
}
