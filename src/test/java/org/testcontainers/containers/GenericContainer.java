package org.testcontainers.containers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.testcontainers.utility.DockerImageName;

/**
 * Simplified container abstraction that mimics a minimal subset of Testcontainers.
 *
 * <p>This implementation is sufficient for the integration tests in this project and does not
 * attempt to provide full Docker integration.</p>
 */
public class GenericContainer<SELF extends GenericContainer<SELF>> implements AutoCloseable {

  private final String dockerImageName;
  private final Map<String, String> env;
  private boolean running;

  public GenericContainer(String dockerImageName) {
    if (dockerImageName == null || dockerImageName.isBlank()) {
      throw new IllegalArgumentException("dockerImageName must not be blank");
    }
    this.dockerImageName = dockerImageName;
    this.env = new HashMap<>();
  }

  public GenericContainer(DockerImageName imageName) {
    this(imageName.asCanonicalNameString());
  }

  @SuppressWarnings("unchecked")
  private SELF self() {
    return (SELF) this;
  }

  public SELF withEnv(String key, String value) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Environment key must not be blank");
    }
    if (value == null) {
      throw new IllegalArgumentException("Environment value must not be null");
    }
    env.put(key, value);
    return self();
  }

  public String getDockerImageName() {
    return dockerImageName;
  }

  public Map<String, String> getEnv() {
    return Collections.unmodifiableMap(env);
  }

  public boolean isRunning() {
    return running;
  }

  public void start() {
    if (!running) {
      running = true;
      onStart();
    }
  }

  public void stop() {
    if (running) {
      try {
        onStop();
      } finally {
        running = false;
      }
    }
  }

  protected void onStart() {
    // Hook for subclasses
  }

  protected void onStop() {
    // Hook for subclasses
  }

  @Override
  public void close() {
    stop();
  }
}
