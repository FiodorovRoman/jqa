package md.fiodorov.jqa.controller;

import java.net.URI;
import java.util.Optional;
import md.fiodorov.jqa.service.api.QuestionService;
import md.fiodorov.jqa.service.api.VotingService;
import md.fiodorov.jqa.util.pagination.Page;
import md.fiodorov.jqa.util.pagination.PageRequest;
import md.fiodorov.jqa.util.pagination.Pageable;
import md.fiodorov.jqa.util.pagination.Sort;
import md.fiodorov.jqa.view.CreateUpdateQuestionView;
import md.fiodorov.jqa.view.QuestionDetailsView;
import md.fiodorov.jqa.view.QuestionListItemView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
public class QuestionRestController {

  private final QuestionService questionService;
  private final VotingService votingService;

  public QuestionRestController(QuestionService questionService, VotingService votingService) {
    this.questionService = questionService;
    this.votingService = votingService;
  }

  @GetMapping
  public Page<QuestionListItemView> list(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size,
      @RequestParam(name = "dir", defaultValue = "DESC") Sort.Direction dir,
      @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy
  ) {
    Sort sort = Sort.by(dir, sortBy);
    Pageable pageable = PageRequest.of(page, size, sort);
    return questionService.getSortedAndPagedQuestionList(sort, pageable);
  }

  @GetMapping("/{id}")
  public ResponseEntity<QuestionDetailsView> get(@PathVariable("id") Long id) {
    Optional<QuestionDetailsView> maybe = questionService.getQuestionDetailsById(id);
    return maybe.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Void> create(@RequestBody CreateUpdateQuestionView view) {
    // Ensure createdBy uses the authenticated user if available
    md.fiodorov.jqa.domain.User current = md.fiodorov.jqa.security.SecurityUserUtil.currentUserOrNull();
    if (current != null) {
      view.setCreatedBy(current);
    }
    questionService.addQuestion(view);
    return ResponseEntity.status(HttpStatus.CREATED).location(URI.create("/api/questions"))
        .build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<Void> update(@PathVariable("id") Long id, @RequestBody CreateUpdateQuestionView view) {
    view.setId(id);
    questionService.updateQuestion(view);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
    questionService.deleteQuestionById(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/vote")
  public ResponseEntity<Void> vote(@PathVariable("id") Long id, @RequestParam(name = "dir") String dir) {
    if ("up".equalsIgnoreCase(dir)) {
      votingService.voteQuestionUp(id);
    } else if ("down".equalsIgnoreCase(dir)) {
      votingService.voteQuestionDown(id);
    } else {
      return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.noContent().build();
  }
}
