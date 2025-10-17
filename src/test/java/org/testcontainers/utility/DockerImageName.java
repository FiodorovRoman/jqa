package org.testcontainers.utility;

import java.util.Objects;

/**
 * Minimal testcontainers replacement used for offline integration tests.
 */
public final class DockerImageName {

  private final String canonicalName;

  private DockerImageName(String canonicalName) {
    this.canonicalName = canonicalName;
  }

  public static DockerImageName parse(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Docker image name must not be blank");
    }
    return new DockerImageName(name);
  }

  public String asCanonicalNameString() {
    return canonicalName;
  }

  @Override
  public String toString() {
    return canonicalName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DockerImageName other)) {
      return false;
    }
    return Objects.equals(canonicalName, other.canonicalName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(canonicalName);
  }
}
