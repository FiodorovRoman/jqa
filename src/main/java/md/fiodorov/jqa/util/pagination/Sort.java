package md.fiodorov.jqa.util.pagination;

import java.util.Objects;

public final class Sort {

  public enum Direction {
    ASC,
    DESC
  }

  private final Direction direction;
  private final String property;

  private Sort(Direction direction, String property) {
    this.direction = Objects.requireNonNull(direction, "direction");
    this.property = Objects.requireNonNull(property, "property");
  }

  public static Sort by(Direction direction, String property) {
    return new Sort(direction, property);
  }

  public Direction getDirection() {
    return direction;
  }

  public String getProperty() {
    return property;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Sort other)) {
      return false;
    }
    return direction == other.direction && property.equals(other.property);
  }

  @Override
  public int hashCode() {
    return Objects.hash(direction, property);
  }

  @Override
  public String toString() {
    return direction + ":" + property;
  }
}
