package md.fiodorov.jqa.domain;

import java.time.Instant;
import javax.persistence.MappedSuperclass;
import lombok.Data;

@MappedSuperclass
@Data
abstract class BaseModel {

  private Long id;

  private Instant createdAt;

  private Instant editedAt;

  private int rank;

}
