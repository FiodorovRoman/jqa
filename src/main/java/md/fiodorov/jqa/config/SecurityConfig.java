package md.fiodorov.jqa.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import md.fiodorov.jqa.auth.AuthProvider;
import md.fiodorov.jqa.domain.Role;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.service.api.AuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class SecurityConfig {

  @Bean
  public org.springframework.web.client.RestTemplate restTemplate() {
    return new org.springframework.web.client.RestTemplate();
  }

  @Bean
  public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
    return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
  }

  @Bean
  public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
    org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOriginPatterns(java.util.List.of("http://localhost:*", "http://127.0.0.1:*", "http://0.0.0.0:*", "http://192.168.*:*", "http://10.*:*"));
    config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "X-Google-Id", "X-Facebook-Id"));
    config.setExposedHeaders(java.util.List.of("Location"));
    org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, AuthHeaderFilter authHeaderFilter) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> {})
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/**").authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/**").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/api/**").authenticated()
            .anyRequest().permitAll()
        )
        .exceptionHandling(eh -> eh.authenticationEntryPoint((request, response, authException) -> {
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }))
        // Use custom header/basic handling in AuthHeaderFilter; disable default httpBasic to avoid double-processing
        .addFilterBefore(authHeaderFilter, org.springframework.security.web.authentication.AnonymousAuthenticationFilter.class);
    return http.build();
  }

  @Component
  public static class AuthHeaderFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;

    public AuthHeaderFilter(AuthenticationService authenticationService) {
      this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

      // If already authenticated (e.g., from previous filter), continue
      Authentication existing = SecurityContextHolder.getContext().getAuthentication();
      if (existing != null && existing.isAuthenticated()) {
        filterChain.doFilter(request, response);
        return;
      }

      String auth = request.getHeader("Authorization");
      String googleId = request.getHeader("X-Google-Id");
      String facebookId = request.getHeader("X-Facebook-Id");

      try {
        User user = null;
        if (auth != null && auth.startsWith("Basic ")) {
          // Let Spring Security's BasicAuth handle credentials if present; we only parse if needed
          // But Basic filter runs after us; we parse here to create Authentication
          String base64 = auth.substring(6);
          String decoded = new String(java.util.Base64.getDecoder().decode(base64));
          int idx = decoded.indexOf(":");
          if (idx > 0) {
            String username = decoded.substring(0, idx);
            String password = decoded.substring(idx + 1);
            user = authenticationService.loginWithPassword(username, password);
          }
        } else if (googleId != null && !googleId.isBlank()) {
          user = authenticationService.loginWithOAuth(AuthProvider.GOOGLE, googleId);
        } else if (facebookId != null && !facebookId.isBlank()) {
          user = authenticationService.loginWithOAuth(AuthProvider.FACEBOOK, facebookId);
        }

        if (user != null) {
          Authentication authentication = new UserAuthenticationToken(user);
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } catch (IllegalArgumentException | IllegalStateException e) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Unauthorized: " + e.getMessage());
        return;
      }

      filterChain.doFilter(request, response);
    }
  }

  public static class UserAuthenticationToken extends AbstractAuthenticationToken {
    private final User principal;

    public UserAuthenticationToken(User principal) {
      super(authorities(principal));
      this.principal = Objects.requireNonNull(principal);
      setAuthenticated(true);
    }

    private static Collection<? extends GrantedAuthority> authorities(User user) {
      Role role = user.getRole() != null ? user.getRole() : Role.USER;
      return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public Object getCredentials() {
      return "N/A";
    }

    @Override
    public Object getPrincipal() {
      return principal;
    }
  }
}
