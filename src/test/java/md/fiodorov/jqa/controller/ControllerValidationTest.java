package md.fiodorov.jqa.controller;

import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Instant;
import md.fiodorov.jqa.view.CreateAnswerView;
import md.fiodorov.jqa.view.CreateUpdateQuestionView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ControllerValidationTest {

  @LocalServerPort
  int port;

  @BeforeEach
  void setUp() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @Test
  void questionValidation_enforced() {
    // Missing content -> 400
    CreateUpdateQuestionView q1 = new CreateUpdateQuestionView();
    q1.setTitle("T");

    given()
        .auth().preemptive().basic("alice", "secret")
        .contentType(ContentType.JSON)
        .body(q1)
        .when()
        .post("/api/questions")
        .then()
        .statusCode(400);

    // Too long content (>2000) -> 400
    CreateUpdateQuestionView q2 = new CreateUpdateQuestionView();
    q2.setTitle("T");
    q2.setContent("a".repeat(2001));

    given()
        .auth().preemptive().basic("alice", "secret")
        .contentType(ContentType.JSON)
        .body(q2)
        .when()
        .post("/api/questions")
        .then()
        .statusCode(400);

    // Negative id on GET -> 400
    given()
        .when()
        .get("/api/questions/{id}", -1)
        .then()
        .statusCode(400);
  }

  @Test
  void answerValidation_enforced() {
    // Create a valid question first
    CreateUpdateQuestionView createQ = new CreateUpdateQuestionView();
    createQ.setTitle("Valid Q");
    createQ.setContent("Body");
    createQ.setCreatedDate(Instant.parse("2024-07-01T10:00:00Z"));

    given()
        .auth().preemptive().basic("alice", "secret")
        .contentType(ContentType.JSON)
        .body(createQ)
        .when()
        .post("/api/questions")
        .then()
        .statusCode(201);

    Long qid = given().get("/api/questions").then().statusCode(200).extract().jsonPath().getLong("content[0].id");

    // Missing content -> 400
    CreateAnswerView a1 = new CreateAnswerView();
    given()
        .auth().preemptive().basic("alice", "secret")
        .contentType(ContentType.JSON)
        .body(a1)
        .when()
        .post("/api/questions/{qid}/answers", qid)
        .then()
        .statusCode(400);

    // Too long content (>500) -> 400
    CreateAnswerView a2 = new CreateAnswerView();
    a2.setContent("b".repeat(501));
    given()
        .auth().preemptive().basic("alice", "secret")
        .contentType(ContentType.JSON)
        .body(a2)
        .when()
        .post("/api/questions/{qid}/answers", qid)
        .then()
        .statusCode(400);

    // Negative question id -> 400
    CreateAnswerView a3 = new CreateAnswerView();
    a3.setContent("ok");
    given()
        .auth().preemptive().basic("alice", "secret")
        .contentType(ContentType.JSON)
        .body(a3)
        .when()
        .post("/api/questions/{qid}/answers", -5)
        .then()
        .statusCode(400);

    // Negative answer id for mark-right -> 400
    given()
        .header("X-Facebook-Id", "fb-123")
        .when()
        .post("/api/answers/{id}/mark-right", -10)
        .then()
        .statusCode(400);
  }
}
