package md.fiodorov.jqa.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.view.CreateUpdateQuestionView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QuestionRestControllerTest {

  @LocalServerPort
  int port;

  @BeforeEach
  void setUp() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
  }

  @Test
  void questionCrudFlow_works() {
    // Create a question
    CreateUpdateQuestionView create = new CreateUpdateQuestionView();
    create.setTitle("REST title");
    create.setContent("REST body");
    create.setCreatedDate(Instant.parse("2024-05-01T10:00:00Z"));
    User author = new User();
    author.setUsername("rest-user");
    create.setCreatedBy(author);

    given()
        .auth().preemptive().basic("alice", "secret")
        .contentType(ContentType.JSON)
        .body(create)
        .when()
        .post("/api/questions")
        .then()
        .statusCode(201);

    // List should contain one element
    List<Map<String, Object>> content =
        given()
            .when()
            .get("/api/questions")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath().getList("content");

    Integer id = ((Number) content.get(0).get("id")).intValue();

    // Get by id should return details
    given()
        .when()
        .get("/api/questions/{id}", id)
        .then()
        .statusCode(200)
        .body("title", equalTo("REST title"));

    // Update
    CreateUpdateQuestionView update = new CreateUpdateQuestionView();
    update.setTitle("Updated title");
    update.setContent("Updated content");
    update.setCreatedDate(Instant.parse("2024-05-02T10:00:00Z"));
    update.setCreatedBy(author);

    given()
        .auth().preemptive().basic("alice", "secret")
        .contentType(ContentType.JSON)
        .body(update)
        .when()
        .put("/api/questions/{id}", id)
        .then()
        .statusCode(204);

    // Verify update
    given()
        .when()
        .get("/api/questions/{id}", id)
        .then()
        .statusCode(200)
        .body("title", equalTo("Updated title"));

    // Delete
    given()
        .auth().preemptive().basic("alice", "secret")
        .when()
        .delete("/api/questions/{id}", id)
        .then()
        .statusCode(204);

    // Verify deletion
    given()
        .when()
        .get("/api/questions/{id}", id)
        .then()
        .statusCode(404);
  }
}
