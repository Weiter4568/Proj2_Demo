package cse.java2.project.Interfaces;

public interface QuestionService {
    String getPercentageOfUnansweredQuestions();
    String getAverageAndMaxAnswerCount();
    String getAnswerCountDistribution();
    String getPercentageOfAcceptedAnswers();
    String getAcceptedAnswerInterval();
    String getPercentageOfQuestionsWhereNonAcceptedAnswerHasHigherScore();
    String getMostUpvotedTags();
}
