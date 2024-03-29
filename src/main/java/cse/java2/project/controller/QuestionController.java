package cse.java2.project.controller;

import com.google.gson.JsonObject;
import cse.java2.project.Interfaces.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Controller
@RequestMapping("/api/questions")
public class QuestionController {

  private final QuestionService questionService;

  @Autowired
  public QuestionController(QuestionService questionService) {
    this.questionService = questionService;
  }

  @GetMapping("/unansweredPercentage")
  @ResponseBody
  public String getUnansweredPercentage() {
    return questionService.getPercentageOfUnansweredQuestions();
  }

  @GetMapping("/answerStats")
  @ResponseBody
  public String getAnswerStats() {
    return questionService.getAverageAndMaxAnswerCount();
  }

  @GetMapping("/answerCountDistribution")
  @ResponseBody
  public String getAnswerCountDistribution() {
    return questionService.getAnswerCountDistribution();
  }

  @GetMapping("/acceptedPercentage")
  @ResponseBody
  public String getAcceptedPercentage() {
    return questionService.getPercentageOfAcceptedAnswers();
  }

  @GetMapping("/acceptedAnswerInterval")
  @ResponseBody
  public String getAcceptedAnswerInterval() {
    return questionService.getAcceptedAnswerInterval();
  }

  @GetMapping("/nonAcceptedHigherUpvotesPercentage")
  @ResponseBody
  public String getNonAcceptedHigherUpvotesPercentage() {
    return questionService.getPercentageOfQuestionsWhereNonAcceptedAnswerHasHigherScore();
  }

  @GetMapping("/most-upvoted-tags")
  @ResponseBody
  public String getMostUpvotedTags() {
    return questionService.getMostUpvotedTags();
  }

  @GetMapping("/most-upvoted-tag-combos")
  @ResponseBody
  public String getMostUpvotedTagCombos() {
    return questionService.getMostUpvotedTagCombos();
  }

  @GetMapping("/most-viewed-tag-combos")
  @ResponseBody
  public String getMostViewedTagCombos() {
    return questionService.getMostViewedTagCombos();
  }

  @GetMapping("/user-thread-count-distribution")
  @ResponseBody
  public String getUserThreadCountDistribution() {
    return questionService.getUserThreadDistribution();
  }

  @GetMapping("/user-answer-count-distribution")
  @ResponseBody
  public String getUserAnswerCountDistribution() {
    return questionService.getUserAnswerCountDistribution();
  }

  @GetMapping("/user-comment-count-distribution")
  @ResponseBody
  public String getUserCommentCountDistribution() {
    return questionService.getUserCommentCountDistribution();
  }

  @GetMapping("/most-active-users")
  @ResponseBody
  public String getMostActiveUsers() {
    return questionService.getMostActiveUsers();
  }

  @GetMapping("/api-stats")
  @ResponseBody
  public String getFrequentAPIs() {
    return questionService.getFrequentAPIs();
  }
}
