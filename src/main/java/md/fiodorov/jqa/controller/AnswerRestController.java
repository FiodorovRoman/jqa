package md.fiodorov.jqa.controller;

import java.util.List;
import java.util.stream.Collectors;
import md.fiodorov.jqa.domain.Answer;
import md.fiodorov.jqa.service.api.AnswerService;
import md.fiodorov.jqa.service.api.VotingService;
import md.fiodorov.jqa.view.AnswerView;
import md.fiodorov.jqa.view.CreateAnswerView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AnswerRestController {

  private final AnswerService answerService;
  private final VotingService votingService;

  public AnswerRestController(AnswerService answerService, VotingService votingService) {
    this.answerService = answerService;
    this.votingService = votingService;
  }

  @GetMapping("/questions/{questionId}/answers")
  public List<AnswerView> list(@PathVariable("questionId") Long questionId) {
    if (questionId == null || questionId < 0) {
      return java.util.List.of();
    }
    return answerService.listAnswers(questionId).stream().map(this::toView).collect(Collectors.toList());
  }

  @PostMapping("/questions/{questionId}/answers")
  public ResponseEntity<AnswerView> create(@PathVariable("questionId") Long questionId, @RequestBody CreateAnswerView view) {
    if (questionId == null || questionId < 0) {
      return ResponseEntity.badRequest().build();
    }
    if (view == null || view.getContent() == null || view.getContent().length() > 500) {
      return ResponseEntity.badRequest().build();
    }
    md.fiodorov.jqa.domain.User current = md.fiodorov.jqa.security.SecurityUserUtil.currentUserOrNull();
    Answer created = answerService.addAnswer(questionId, view.getContent(), current != null ? current : view.getCreatedBy(), view.getCreatedDate());
    return ResponseEntity.status(HttpStatus.CREATED).body(toView(created));
  }

  @PostMapping("/answers/{answerId}/mark-right")
  public ResponseEntity<AnswerView> markRight(@PathVariable("answerId") Long answerId) {
    if (answerId == null || answerId < 0) {
      return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.ok(toView(answerService.markAsRight(answerId)));
  }

  @PostMapping("/answers/{answerId}/vote")
  public ResponseEntity<Void> vote(@PathVariable("answerId") Long answerId, @RequestParam(name = "dir") String dir) {
    if (answerId == null || answerId < 0) {
      return ResponseEntity.badRequest().build();
    }
    if ("up".equalsIgnoreCase(dir)) {
      votingService.voteAnswerUp(answerId);
    } else if ("down".equalsIgnoreCase(dir)) {
      votingService.voteAnswerDown(answerId);
    } else {
      return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/answers/{answerId}")
  public ResponseEntity<Void> delete(@PathVariable("answerId") Long answerId) {
    if (answerId == null || answerId < 0) {
      return ResponseEntity.badRequest().build();
    }
    answerService.deleteAnswerById(answerId);
    return ResponseEntity.noContent().build();
  }

  private AnswerView toView(Answer answer) {
    AnswerView v = new AnswerView();
    v.setId(answer.getId());
    v.setQuestionId(answer.getQuestionId());
    v.setContent(answer.getContent());
    v.setAuthor(answer.getCreatedBy() != null ? answer.getCreatedBy().getUsername() : null);
    v.setCreatedDate(answer.getCreatedAt());
    v.setRight(answer.isRight());
    return v;
  }
}
