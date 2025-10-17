package md.fiodorov.jqa.repository;

public interface ConfigRepository {
  int getInt(String key, int defaultValue);
  void setInt(String key, int value);
}
