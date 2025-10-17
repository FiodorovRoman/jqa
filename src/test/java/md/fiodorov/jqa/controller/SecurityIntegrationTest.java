package md.fiodorov.jqa.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import md.fiodorov.jqa.JqaApplication;
import md.fiodorov.jqa.view.CreateAnswerView;
import md.fiodorov.jqa.view.CreateUpdateQuestionView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(classes = JqaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SecurityIntegrationTest {

  @LocalServerPort
  int port;

  @BeforeAll
  static void setupRestAssured() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @Test
  void getEndpoints_arePublic() {
    RestAssured.port = port;
    // list questions public
    given().get("/api/questions").then().statusCode(200);
  }

  @Test
  void postQuestion_requiresAuth_andWorksWithBasic() {
    RestAssured.port = port;

    CreateUpdateQuestionView q = new CreateUpdateQuestionView();
    q.setTitle("Secured question");
    q.setContent("Body");

    // Without auth
    given()
        .contentType(ContentType.JSON)
        .body(q)
        .post("/api/questions")
        .then()
        .statusCode(401);

    // With Basic auth alice/secret
    given()
        .auth().preemptive().basic("alice", "secret")
        .contentType(ContentType.JSON)
        .body(q)
        .post("/api/questions")
        .then()
        .statusCode(201);
  }

  @Test
  void postAnswer_requiresAuth_andWorksWithGoogleHeader() {
    RestAssured.port = port;

    // First create a question with basic auth
    CreateUpdateQuestionView q = new CreateUpdateQuestionView();
    q.setTitle("Q1");
    q.setContent("C1");
    given()
        .auth().preemptive().basic("alice", "secret")
        .contentType(ContentType.JSON)
        .body(q)
        .post("/api/questions")
        .then()
        .statusCode(201);

    // Assume question listing returns items, take first id
    Long questionId =
        given().get("/api/questions").then().statusCode(200).extract().jsonPath().getLong("content[0].id");

    CreateAnswerView a = new CreateAnswerView();
    a.setContent("Answer body");

    // Without auth
    given()
        .contentType(ContentType.JSON)
        .body(a)
        .post("/api/questions/" + questionId + "/answers")
        .then()
        .statusCode(401);

    // With Google header auth
    given()
        .header("X-Google-Id", "google-123")
        .contentType(ContentType.JSON)
        .body(a)
        .post("/api/questions/" + questionId + "/answers")
        .then()
        .statusCode(201)
        .body("author", equalTo("alice"));
  }

  @Test
  void markRight_requiresAuth_andWorksWithFacebookHeader() {
    RestAssured.port = port;

    // Create question
    CreateUpdateQuestionView q = new CreateUpdateQuestionView();
    q.setTitle("Q2");
    q.setContent("C2");
    given()
        .auth().preemptive().basic("alice", "secret")
        .contentType(ContentType.JSON)
        .body(q)
        .post("/api/questions")
        .then()
        .statusCode(201);

    Long questionId =
        given().get("/api/questions").then().statusCode(200).extract().jsonPath().getLong("content[0].id");

    CreateAnswerView a = new CreateAnswerView();
    a.setContent("Answer body 2");
    Long answerId =
        given()
            .auth().preemptive().basic("alice", "secret")
            .contentType(ContentType.JSON)
            .body(a)
            .post("/api/questions/" + questionId + "/answers")
            .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

    // Mark right requires auth
    given()
        .post("/api/answers/" + answerId + "/mark-right")
        .then()
        .statusCode(401);

    given()
        .header("X-Facebook-Id", "fb-123")
        .post("/api/answers/" + answerId + "/mark-right")
        .then()
        .statusCode(200)
        .body("right", equalTo(true));
  }
}
