package cse.java2.project.service;

import com.google.gson.Gson;
import cse.java2.project.Interfaces.QuestionService;
import cse.java2.project.JSON_Model.*;
import cse.java2.project.repository.AnswerRepository;
import cse.java2.project.repository.CommentRepository;
import cse.java2.project.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    private EntityManager em;
    private final AnswerRepository answerRepository;
    private final CommentRepository commentRepository;
    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionServiceImpl(EntityManager em, AnswerRepository answerRepository, CommentRepository commentRepository,
                               QuestionRepository questionRepository) {
        this.em = em;
        this.answerRepository = answerRepository;
        this.commentRepository = commentRepository;
        this.questionRepository = questionRepository;
    }

    // Q1: 展示没有答案的 questions 的百分比 (4)
    @Override
    public String getPercentageOfUnansweredQuestions() {
        Query query = em.createNativeQuery("SELECT(CAST(SUM(CASE WHEN answer_count = 0 THEN 1 ELSE 0 END) AS FLOAT) / count(*)) as percentage FROM questions;" );
        List<String> names = new ArrayList<>();
        names.add("Not Answered Questions");
        names.add("Answered Questions");
        List<Double> values = new ArrayList<>();
        values.add((Double) query.getSingleResult());
        values.add(1 - (Double) query.getSingleResult());
        ReturnJSON returnJSON = new ReturnJSON(names, values);
        return getJSONString(returnJSON);
    }

    // Q2: 展示 answer 数量的平均值(2)和最大值(2)
    @Override
    public String getAverageAndMaxAnswerCount() {
        Query query = em.createNativeQuery("SELECT avg(answer_count) as average, max(answer_count) as maximum FROM questions", Tuple.class);
        Tuple result = (Tuple) query.getSingleResult();

        BigDecimal average = (BigDecimal) result.get("average");
        int averageValue = average.intValue();
        int maximum = (int) result.get("maximum");

        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        names.add("Average Number");
        names.add("Maximum Number");
        values.add((double) averageValue);
        values.add((double) maximum);
        ReturnJSON returnJSON = new ReturnJSON(names, values);

        return getJSONString(returnJSON);
    }

    // Q3:展示 answer 数的分布(与上一项相同则不得分) (4)
    @Override
    public String getAnswerCountDistribution() {
        Query query = em.createNativeQuery("SELECT CASE WHEN answer_count = 0 THEN '0' WHEN answer_count = 1 THEN '1' WHEN answer_count BETWEEN 2 AND 5 THEN '2-5' WHEN answer_count BETWEEN 6 AND 10 THEN '6-10' WHEN answer_count BETWEEN 11 AND 20 THEN '11-20' WHEN answer_count BETWEEN 21 AND 30 THEN '21-30' WHEN answer_count BETWEEN 31 AND 40 THEN '31-40' ELSE '40+' END AS answer_range,COUNT(*) AS count FROM questions GROUP BY answer_range;");
        List<Object[]> resultList = query.getResultList();
        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (Object[] objects : resultList){
            names.add((String) objects[0]);
            values.add((double) objects[1]);
        }
        ReturnJSON returnJSON = new ReturnJSON(names, values);

        return getJSONString(returnJSON);
    }

    // Q4:展示有 accepted answer 的问题的百分比 (4)
    @Override
    public String getPercentageOfAcceptedAnswers() {
        Query query = em.createNativeQuery("SELECT(CAST(SUM(CASE WHEN answered = true THEN 1 ELSE 0 END) AS FLOAT) / count(*)) as percentage FROM questions;\n");
        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        names.add("Accepted Answer");
        names.add("Non-Accepted Answer");
        values.add((double) query.getSingleResult());
        values.add(1 - (double) query.getSingleResult());
        ReturnJSON returnJSON = new ReturnJSON(names, values);
        return getJSONString(returnJSON);
    }

    // Q5: 展示问题从提出到解决 (answer accepted time – question post time) 的时间间隔分布 (4)
    @Override
    public String getAcceptedAnswerInterval() {
        Query query = em.createNativeQuery("SELECT CASE WHEN tmp.interval BETWEEN 0 AND 600 THEN '<= 10mins' WHEN tmp.interval BETWEEN 601 AND 3600 THEN '10mins ~ 1h' WHEN tmp.interval BETWEEN 3601 AND 18000 THEN '1h~5h' WHEN tmp.interval BETWEEN 18001 AND 36000 THEN '5h ~ 10h' WHEN tmp.interval BETWEEN 36001 AND 86400 THEN '10h ~ 1day' WHEN tmp.interval BETWEEN 86401 AND 259200 THEN '1day ~ 3day' WHEN tmp.interval BETWEEN 259201 AND 604800 THEN '3day ~ 1week' WHEN tmp.interval BETWEEN 604801 AND 2419200 THEN '1week ~ 1mon' WHEN tmp.interval BETWEEN 2419201 AND 7257600 THEN '1mon ~ 3mon' ELSE '> 3mons' END AS interval_range,count(*) as count FROM (SELECT answers.id, questions.creation_date, answers.accepted_date, answers.accepted_date - questions.creation_date AS interval FROM questions JOIN answers ON questions.id = answers.question_id WHERE answers.is_accepted = true) tmp GROUP BY interval_range;");
        List<Object[]> objects = query.getResultList();
        List <String> names = new ArrayList<>();
        List <Double> values = new ArrayList<>();
        for (Object[] result : objects) {
            names.add((String) result[0]);
            values.add( ((double) result[1]));
        }
        ReturnJSON returnJSON = new ReturnJSON(names, values);
        return getJSONString(returnJSON);
    }

    // Q6: 展示含有 non-accepted answer 的 upvote 数高于 accepted answer 的问题的百分比 (4)
    @Override
    public String getPercentageOfQuestionsWhereNonAcceptedAnswerHasHigherScore() {
        Query query = em.createNativeQuery("SELECT CAST((SELECT COUNT(*) FROM (SELECT questions.id FROM questions INNER JOIN answers AS accepted_answers ON questions.id = accepted_answers.question_id AND accepted_answers.is_accepted = true LEFT JOIN answers AS other_answers ON questions.id = other_answers.question_id AND other_answers.is_accepted = false WHERE other_answers.score > accepted_answers.score GROUP BY questions.id) AS subquery) AS FLOAT) / (SELECT COUNT(*) FROM questions WHERE answer_count > 0) AS percentage");
        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        names.add("Non-Answered with Higher Votes");
        names.add("Answered with Lower Votes");
        values.add((double)query.getSingleResult());
        values.add(1 - (double)query.getSingleResult());
        ReturnJSON returnJSON = new ReturnJSON(names, values);
        return getJSONString(returnJSON);
    }

    // Q7:展示哪些 tags 经常和 Java tag 一起出现 (4)
    public String getMostUpvotedTags() {
        Query query = em.createNativeQuery("SELECT tags, COUNT(*) as tag_frequency FROM (SELECT UNNEST(regexp_split_to_array(tags, E'[,\\\\[\\\\]\\\"]')) as tags FROM questions WHERE tags LIKE '%java%') subquery WHERE tags != 'java' and tags != '' GROUP BY tags ORDER BY tag_frequency DESC;");
        List<Object[]> objects = query.getResultList();
        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (Object[] o : objects) {
            names.add((String) o[0]);
            BigInteger bigInteger = (BigInteger) o[1];
            values.add(bigInteger.doubleValue());
        }
        ReturnJSON returnJSON = new ReturnJSON(names, values);
        return getJSONString(returnJSON);
    }

    // Q8: 展示哪些 tags 或 tag 的组合得到最多的 upvotes (4)
    @Override
    public String getMostUpvotedTagCombos() {
        // Get all questions with their tags and scores
        String sql = "select q.tags, q.score from Question q";
        Query query = em.createQuery(sql);
        List<Object[]> results = query.getResultList();

        // Process the results
        Map<String, Integer> tagComboScores = new HashMap<>();
        for (Object[] result : results) {
            String tags = (String) result[0];
            Integer score = (Integer) result[1];

            // Split the tags and sort them to form a tag combo
            List<String> tagList = Arrays.asList(tags.split("[,\\[\\]\"]")).stream().filter(str -> !str.isEmpty()).sorted().collect(Collectors.toList());
            String tagCombo = String.join(",", tagList);

            // Update the score of the tag combo
            tagComboScores.put(tagCombo, tagComboScores.getOrDefault(tagCombo, 0) + score);
        }

        // Sort the tag combos by their scores in descending order
        List<Map.Entry<String, Integer>> sortedTagCombos = tagComboScores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (Map.Entry<String, Integer> m : sortedTagCombos){
            if (!m.getKey().equals("")) {
                names.add(m.getKey());
                values.add((double) m.getValue());
            }
        }

        ReturnJSON returnJSON = new ReturnJSON(names, values);

        return getJSONString(returnJSON);
    }

    // Q9: 展示哪些 tags 或 tag 的组合得到最多的 views (4)
    @Override
    public String getMostViewedTagCombos() {
        // Get all questions with their tags and scores
        String sql = "select q.tags, q.viewCnt from Question q";
        Query query = em.createQuery(sql);
        List<Object[]> results = query.getResultList();

        // Process the results
        Map<String, Integer> tagComboScores = new HashMap<>();
        for (Object[] result : results) {
            String tags = (String) result[0];
            Integer score = (Integer) result[1];

            // Split the tags and sort them to form a tag combo
            List<String> tagList = Arrays.asList(tags.split("[,\\[\\]\"]")).stream().filter(str -> !str.isEmpty()).sorted().collect(Collectors.toList());
            String tagCombo = String.join(",", tagList);

            // Update the score of the tag combo
            tagComboScores.put(tagCombo, tagComboScores.getOrDefault(tagCombo, 0) + score);
        }

        // Sort the tag combos by their scores in descending order
        List<Map.Entry<String, Integer>> sortedTagCombos = tagComboScores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (Map.Entry<String, Integer> m : sortedTagCombos){
            if (!m.getKey().equals("")) {
                names.add(m.getKey());
                values.add((double) m.getValue());
            }
        }

        ReturnJSON returnJSON = new ReturnJSON(names, values);

        return getJSONString(returnJSON);
    }

    // Q10:
    @Override
    public String getUserThreadDistribution() {
        Query query = em.createNativeQuery("SELECT post_id, COUNT(DISTINCT user_id)FROM (SELECT id as post_id, user_id FROM questions UNION ALL SELECT question_id as post_id, user_id FROM answers UNION ALL SELECT post_id, user_id FROM comments) AS all_contributions GROUP BY post_id;");
        List<Object[]> results = query.getResultList();
        Map<String, Integer> distribution = new HashMap<>();
        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (Object[] result : results) {
            int userCount = ((BigInteger) result[1]).intValue();
            String range;
            if (userCount <= 5) {
                range = "0-5";
            } else if (userCount <= 10) {
                range = "6-10";
            } else if (userCount <= 15) {
                range = "11-15";
            } else if (userCount <=20) {
                range = "16-20";
            } else if (userCount <= 25) {
                range = "21-25";
            } else if (userCount <= 30) {
                range = "26-30";
            }
            else {
                range = "30+";
            }
            distribution.put(range, distribution.getOrDefault(range, 0) + 1);
        }
        for (Map.Entry<String, Integer> m : distribution.entrySet()) {
            names.add(m.getKey());
            values.add((double) m.getValue());
        }
        ReturnJSON returnJSON = new ReturnJSON(names, values);
        return getJSONString(returnJSON);
    }

    // Q11: 从 问 题 回 答 者 (who post answers) 和 评 论 者 (who post comment)两个角度进行统计 (4)
    // 获取用户回答的分布
    @Override
    public String getUserAnswerCountDistribution() {
        Query query = em.createNativeQuery("SELECT post_id, COUNT(Distinct user_id)From (SELECT id as post_id, user_id From questions UNION ALL SELECT question_id as post_id, user_id From answers) as ans_contribution GROUP BY post_id;");
        List<Object[]> results = query.getResultList();
        Map<String, Integer> distribution = new HashMap<>();
        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (Object[] result : results) {
            int userCount = ((BigInteger) result[1]).intValue();
            String range;
            if (userCount <= 5) {
                range = "0-5";
            } else if (userCount <= 10) {
                range = "6-10";
            } else if (userCount <= 15) {
                range = "11-15";
            } else if (userCount <=20) {
                range = "16-20";
            } else if (userCount <= 25) {
                range = "21-25";
            } else if (userCount <= 30) {
                range = "26-30";
            }
            else {
                range = "30+";
            }
            distribution.put(range, distribution.getOrDefault(range, 0) + 1);
        }
        for (Map.Entry<String, Integer> m : distribution.entrySet()) {
            names.add(m.getKey());
            values.add((double) m.getValue());
        }
        ReturnJSON returnJSON = new ReturnJSON(names, values);
        return getJSONString(returnJSON);
    }
    //获取用户评论的分布
    @Override
    public String getUserCommentCountDistribution() {
        Query query = em.createNativeQuery("SELECT post_id, COUNT(Distinct user_id)From (SELECT id as post_id, user_id From questions UNION ALL SELECT post_id as post_id, user_id From comments) as ans_contribution GROUP BY post_id;");
        List<Object[]> results = query.getResultList();
        Map<String, Integer> distribution = new HashMap<>();
        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (Object[] result : results) {
            int userCount = ((BigInteger) result[1]).intValue();
            String range;
            if (userCount <= 5) {
                range = "0-5";
            } else if (userCount <= 10) {
                range = "6-10";
            } else if (userCount <= 15) {
                range = "11-15";
            } else if (userCount <=20) {
                range = "16-20";
            } else if (userCount <= 25) {
                range = "21-25";
            } else if (userCount <= 30) {
                range = "26-30";
            }
            else {
                range = "30+";
            }
            distribution.put(range, distribution.getOrDefault(range, 0) + 1);
        }
        for (Map.Entry<String, Integer> m : distribution.entrySet()) {
            names.add(m.getKey());
            values.add((double) m.getValue());
        }
        ReturnJSON returnJSON = new ReturnJSON(names, values);
        return getJSONString(returnJSON);
    }


    // 获取每个用户的提问数
    public Map<String, Long> getQuestionCountsPerUser() {
        return questionRepository.countQuestionsPerUser().stream()
                .collect(Collectors.toMap(
                        objs -> objs[1].toString(),
                        objs -> (Long) objs[0]
                ));
    }
    // 获取每个用户的回答数
    public Map<String, Long> getAnswerCountsPerUser() {
        return answerRepository.countAnswersPerUser().stream()
                .collect(Collectors.toMap(
                        objs ->  objs[1].toString(),
                        objs -> (Long) objs[0]
                ));
    }
    // 获取每个用户的评论数
    public Map<String, Long> getCommentCountsPerUser() {
        return commentRepository.countCommentsPerUser().stream()
                .collect(Collectors.toMap(
                        objs -> objs[1].toString(),
                        objs -> (Long) objs[0]
                ));
    }

    @Override
    public String getMostActiveUsers(){
        Map<String, Long> quemap = getQuestionCountsPerUser();
        Map<String, Long> ansmap = getAnswerCountsPerUser();
        Map<String, Long> commap = getCommentCountsPerUser();
        Map<String, Long> userActivity = new HashMap<>();
        for(Map.Entry<String, Long> m : quemap.entrySet()) {
            userActivity.put(m.getKey(), userActivity.getOrDefault(m.getKey(), 0L) + m.getValue());
        }
        for(Map.Entry<String, Long> m : ansmap.entrySet()){
            userActivity.put(m.getKey(), userActivity.getOrDefault(m.getKey(), 0L) + m.getValue());
        }
        for (Map.Entry<String, Long> m : commap.entrySet()) {
            userActivity.put(m.getKey(), userActivity.getOrDefault(m.getKey(), 0L) + m.getValue());
        }
        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (Map.Entry<String, Long> m : userActivity.entrySet()) {
            names.add(m.getKey());
            values.add(m.getValue().doubleValue());
        }
        ReturnJSON returnJSON = new ReturnJSON(names, values);
        return getJSONString(returnJSON);
    }

    // 将回答或评论的分布返回为JSON字符串
    private String getString(Map<Integer, Long> distribution, Map<String, Long> commentCountsPerUser) {
        for (Long count : commentCountsPerUser.values()) {
            System.out.println(count);
            int bucket = calculateBucket(count);
            distribution.put(bucket, distribution.getOrDefault(bucket, 0L) + 1);
        }
        List<String> names = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (Map.Entry<Integer, Long> map : distribution.entrySet()){
            names.add(map.getKey().toString());
            values.add((double)map.getValue());
        }
        ReturnJSON returnJSON = new ReturnJSON(names, values);
        return getJSONString(returnJSON);
    }

    // A simple method to determine the bucket for a count. Adjust this as needed for your application.
    private int calculateBucket(Long count) {
        return count.intValue() / 1;  // This will put counts into buckets of size 10
    }



    private String getJSONString(Object o) {
        Gson gson = new Gson();
        return gson.toJson(o);
    }

}
