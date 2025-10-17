package md.fiodorov.jqa.repository.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import md.fiodorov.jqa.repository.ConfigRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryConfigRepository implements ConfigRepository {

  private final Map<String, Integer> ints = new ConcurrentHashMap<>();

  public InMemoryConfigRepository() {
    // Seed default point values
    ints.put("points.question.upvote", 10);
    ints.put("points.question.downvote", 2);
    ints.put("points.answer.upvote", 10);
    ints.put("points.answer.downvote", 2);
    ints.put("points.answer.accepted", 15);
  }

  @Override
  public int getInt(String key, int defaultValue) {
    return ints.getOrDefault(key, defaultValue);
  }

  @Override
  public void setInt(String key, int value) {
    ints.put(key, value);
  }
}
