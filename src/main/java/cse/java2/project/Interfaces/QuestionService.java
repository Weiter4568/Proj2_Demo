package cse.java2.project.Interfaces;

public interface QuestionService {
    String getPercentageOfUnansweredQuestions();
    String getAverageAndMaxAnswerCount();
    String getAnswerCountDistribution();
    String getPercentageOfAcceptedAnswers();
    String getAcceptedAnswerInterval();
    String getPercentageOfQuestionsWhereNonAcceptedAnswerHasHigherScore();
    String getMostUpvotedTags();
    String getMostUpvotedTagCombos();
    String getMostViewedTagCombos();
    String getUserThreadDistribution();
    String getUserAnswerCountDistribution();
    String getUserCommentCountDistribution();
    String getMostActiveUsers();
    String getFrequentAPIs();
}
