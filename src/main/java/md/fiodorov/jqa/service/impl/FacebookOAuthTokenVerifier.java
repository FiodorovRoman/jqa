package md.fiodorov.jqa.service.impl;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component("facebookVerifier")
public class FacebookOAuthTokenVerifier implements OAuthTokenVerifier {

  private final RestTemplate restTemplate;
  private final String debugUrl;
  private final String appToken;

  public FacebookOAuthTokenVerifier(RestTemplate restTemplate,
                                    @Value("${oauth.facebook.debugUrl:https://graph.facebook.com/debug_token}") String debugUrl,
                                    @Value("${oauth.facebook.appToken:}") String appToken) {
    this.restTemplate = restTemplate;
    this.debugUrl = debugUrl;
    this.appToken = appToken;
  }

  @Override
  public String verifyAndGetUserId(String token) {
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException("Token must not be blank");
    }
    // Backward compatibility: if it doesn't look like a bearer token/JWT, treat as already an id
    if (!token.contains(".")) {
      return token;
    }
    if (appToken == null || appToken.isBlank()) {
      throw new IllegalArgumentException("Facebook appToken must be configured to verify tokens");
    }
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> resp = restTemplate.getForObject(
          debugUrl + "?input_token={token}&access_token={appToken}", Map.class, token, appToken);
      if (resp == null) {
        throw new IllegalArgumentException("Empty response from Facebook debug_token");
      }
      Object data = resp.get("data");
      if (!(data instanceof Map)) {
        throw new IllegalArgumentException("Malformed Facebook debug_token response");
      }
      Object userId = ((Map<?, ?>) data).get("user_id");
      if (userId == null) {
        throw new IllegalArgumentException("Facebook token does not contain user_id");
      }
      return userId.toString();
    } catch (RestClientException e) {
      throw new IllegalArgumentException("Invalid Facebook token", e);
    }
  }
}
