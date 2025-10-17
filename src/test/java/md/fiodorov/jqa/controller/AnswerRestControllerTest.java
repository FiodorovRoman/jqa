package md.fiodorov.jqa.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import md.fiodorov.jqa.domain.User;
import md.fiodorov.jqa.view.CreateAnswerView;
import md.fiodorov.jqa.view.CreateUpdateQuestionView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AnswerRestControllerTest {

  @LocalServerPort
  int port;

  @BeforeEach
  void setUp() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
  }

  @Test
  void answerCrudFlow_works() {
    // Create a question to attach answers
    Integer qId = createQuestion("Question for answers");

    // Create two answers
    long a1 = createAnswer(qId, "First answer", "alice");
    long a2 = createAnswer(qId, "Second answer", "bob");

    // List answers and verify two present
    List<Map<String, Object>> answers =
        given()
            .when()
            .get("/api/questions/{qid}/answers", qId)
            .then()
            .statusCode(200)
            .extract().jsonPath().getList("");
    org.junit.jupiter.api.Assertions.assertEquals(2, answers.size(), "Expected two answers");

    // Mark first as right
    given()
        .header("X-Facebook-Id", "fb-123")
        .when()
        .post("/api/answers/{id}/mark-right", a1)
        .then()
        .statusCode(200)
        .body("right", is(true));

    // Delete second
    given()
        .auth().preemptive().basic("alice", "secret")
        .when()
        .delete("/api/answers/{id}", a2)
        .then()
        .statusCode(204);

    // List should have one remaining
    given()
        .when()
        .get("/api/questions/{qid}/answers", qId)
        .then()
        .statusCode(200)
        .body("size()", equalTo(1));
  }

  private Integer createQuestion(String title) {
    CreateUpdateQuestionView create = new CreateUpdateQuestionView();
    create.setTitle(title);
    create.setContent("Body");
    create.setCreatedDate(Instant.parse("2024-06-01T10:00:00Z"));
    User author = new User();
    author.setUsername("author");
    create.setCreatedBy(author);

    given().auth().preemptive().basic("alice", "secret").contentType(ContentType.JSON).body(create).post("/api/questions").then().statusCode(201);

    List<Map<String, Object>> content = given().get("/api/questions").then().extract().jsonPath().getList("content");
    return ((Number) content.get(content.size() - 1).get("id")).intValue();
  }

  private long createAnswer(int questionId, String content, String username) {
    CreateAnswerView view = new CreateAnswerView();
    view.setContent(content);
    view.setCreatedDate(Instant.parse("2024-06-01T11:00:00Z"));
    User user = new User();
    user.setUsername(username);
    view.setCreatedBy(user);

    Map<String, Object> resp =
        given().auth().preemptive().basic("alice", "secret").contentType(ContentType.JSON).body(view)
            .post("/api/questions/{qid}/answers", questionId)
            .then().statusCode(201)
            .extract().as(Map.class);
    return ((Number) resp.get("id")).longValue();
  }
}
