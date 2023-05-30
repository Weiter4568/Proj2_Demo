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

    private QuestionService questionService;

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


}
