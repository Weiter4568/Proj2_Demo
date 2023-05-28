package cse.java2.project.service;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cse.java2.project.model.Answer;
import cse.java2.project.model.Comment;
import cse.java2.project.model.Question;
import cse.java2.project.repository.AnswerRepository;
import cse.java2.project.repository.CommentRepository;
import cse.java2.project.repository.QuestionRepository;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataService {

    private static final String API_URL = "https://api.stackexchange.com/2.3/questions?order=desc&sort=activity&tagged=java&site=stackoverflow";

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public DataService(QuestionRepository questionRepository, AnswerRepository answerRepository, CommentRepository commentRepository) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.commentRepository = commentRepository;
    }

    public void fetchAndStoreData() {
        // Build the HTTP client
        CloseableHttpClient client = HttpClientBuilder.create().build();

        // Construct the API endpoint URL
        String url = "https://api.stackexchange.com/2.3/questions?order=desc&sort=activity&tagged=java&site=stackoverflow";

        try {
            // Make the API request
            CloseableHttpResponse response = getCloseableHttpResponse(url, client);

            // Parse the response JSON
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            JsonObject jsonResponse = new Gson().fromJson(reader, JsonObject.class);

            Gson gson = new Gson();

            String test = gson.toJson(jsonResponse);
//            System.out.println(test);

            // 指定输出文件路径
            String filePath = "/Users/weiter/Documents/SUSTech/2023Spring/CS209 Computer System Design & Application/Final Project/Final_Proj_Demo/questions/questions.json";

            try (FileWriter writer = new FileWriter(filePath)) {
                // 将 JSON 字符串写入文件
                writer.write(test);
                System.out.println("Question JSON 文件已成功写入：" + filePath);
            } catch (IOException e) {
                System.out.println("写入 Question JSON 文件时出错：" + e.getMessage());
            }

            // Extract the questions array from the response
            JsonArray questionsArray = jsonResponse.getAsJsonArray("items");
            List<Question> questions = new ArrayList<>();

            // Loop through the questions and retrieve relevant information
            for (JsonElement questionElement : questionsArray) {
                JsonObject questionObject = questionElement.getAsJsonObject();

                // Get info from qustionJSON object
                int id = questionObject.get("question_id").getAsInt();
                String questionTitle = questionObject.get("title").getAsString();
                boolean isAnsweredd = questionObject.get("is_answered").getAsBoolean();
                int ansCnt = questionObject.get("answer_count").getAsInt();
                int creDate = questionObject.get("creation_date").getAsInt();
                String tags = questionObject.get("tags").getAsJsonArray().toString();
                int score = questionObject.get("score").getAsInt();
                int userID = questionObject.get("owner").getAsJsonObject().get("user_id").getAsInt();

                // Process and store the Question in PostgreSQL
                Question question = new Question();
                question.setId(id);
                question.setTitle(questionTitle);
                question.setAnswered(isAnsweredd);
                question.setAnswerCount(ansCnt);
                question.setCreationDate(creDate);
                question.setTags(tags);
                question.setScore(score);
                question.setUserID(userID);

//                System.out.println(question.toString());
                // Add question to array
                questions.add(question);

//                System.out.println(questionTitle);
//                System.out.println("--------------------------------------------");
            }
            // Store questions in database
            questionRepository.saveAll(questions);

            // Store answers in database
            fetchAns(questions);

            // Store commments in database
            fetchComment(questions);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Fetch answer for each question
    private void fetchAns(List<Question> questions){
        String url = "https://api.stackexchange.com/2.3/questions/";
        for (int i = 0; i < questions.size(); i++){
            url += questions.get(i).getId().toString();
            if (i != questions.size() - 1)
                url += ";";
        }
        url += "/answers?order=desc&sort=votes&site=stackoverflow";

        // Build the HTTP client
        CloseableHttpClient client = HttpClientBuilder.create().build();

        try {
            // Make the API request
            CloseableHttpResponse response = getCloseableHttpResponse(url, client);

            // Parse the response JSON
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            JsonObject jsonResponse = new Gson().fromJson(reader, JsonObject.class);

            // Output answer JSON file to the corresponding position
            Gson gson = new Gson();

            String test = gson.toJson(jsonResponse);
//            System.out.println(test);

            // 指定输出文件路径
            String filePath = "/Users/weiter/Documents/SUSTech/2023Spring/CS209 Computer System Design & Application/Final Project/Final_Proj_Demo/questions/answers.json";

            try (FileWriter writer = new FileWriter(filePath)) {
                // 将 JSON 字符串写入文件
                writer.write(test);
                System.out.println("Ans JSON 文件已成功写入：" + filePath);
            } catch (IOException e) {
                System.out.println("写入Ans JSON 文件时出错：" + e.getMessage());
            }

            // Extract the questions array from the response
            JsonArray answersArray = jsonResponse.getAsJsonArray("items");
            List<Answer> answers = new ArrayList<>();

            // Loop through answers and retrieve relevant information
            for(JsonElement answerElement : answersArray) {
                JsonObject answerObject = answerElement.getAsJsonObject();

                // Get info from answerObject
                int ansID = answerObject.get("answer_id").getAsInt();
                int questionID = answerObject.get("question_id").getAsInt();
                boolean isAccepted = answerObject.get("is_accepted").getAsBoolean();
                int score = answerObject.get("score").getAsInt();
                int userID = 0;
                if (! answerObject.getAsJsonObject("owner").get("user_type").getAsString().equals("does_not_exist")) {
                    userID = answerObject.getAsJsonObject("owner").get("user_id").getAsInt();
                }

                // Store the info into the PostgreSQL
                Answer answer = new Answer();
                answer.setId(ansID);
                answer.setQuestionId(questionID);
                answer.setIsAccepted(isAccepted);
                answer.setScore(score);
                answer.setUserID(userID);

                answers.add(answer);
            }
            answerRepository.saveAll(answers);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Fetch comment for each question
    private void fetchComment(List<Question> questions){
        String url = "https://api.stackexchange.com/2.3/questions/";
        for (int i = 0; i < questions.size(); i++){
            url += questions.get(i).getId().toString();
            if (i != questions.size() - 1)
                url += ";";
        }
        url += "/comments?order=desc&sort=votes&site=stackoverflow";

        // Build the HTTP client
        CloseableHttpClient client = HttpClientBuilder.create().build();

        try {
            // Make the API request
            CloseableHttpResponse response = getCloseableHttpResponse(url, client);

            // Parse the response JSON
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            JsonObject jsonResponse = new Gson().fromJson(reader, JsonObject.class);

            // Output comment JSON file to the corresponding position
            Gson gson = new Gson();

            String test = gson.toJson(jsonResponse);
//            System.out.println(test);

            // 指定输出文件路径
            String filePath = "/Users/weiter/Documents/SUSTech/2023Spring/CS209 Computer System Design & Application/Final Project/Final_Proj_Demo/questions/comments.json";

            try (FileWriter writer = new FileWriter(filePath)) {
                // 将 JSON 字符串写入文件
                writer.write(test);
                System.out.println("Comments JSON 文件已成功写入：" + filePath);
            } catch (IOException e) {
                System.out.println("写入 Comments JSON 文件时出错：" + e.getMessage());
            }

            // Extract the questions array from the response
            JsonArray commentsArray = jsonResponse.getAsJsonArray("items");
            List<Comment> comments = new ArrayList<>();

            // Loop through comments and retrieve relevant information
            for(JsonElement commentElement : commentsArray) {
                JsonObject commentObject = commentElement.getAsJsonObject();

                // Get info from commentObject
                int commentId = commentObject.get("comment_id").getAsInt();
                int postID = commentObject.get("post_id").getAsInt();
                int score = commentObject.get("score").getAsInt();
                int userID = 0;
                if (! commentObject.getAsJsonObject("owner").get("user_type").getAsString().equals("does_not_exist")) {
                    userID = commentObject.getAsJsonObject("owner").get("user_id").getAsInt();
                }

                // Store the info into the PostgreSQL
                Comment comment = new Comment();
                comment.setId(commentId);
                comment.setPostId(postID);
                comment.setScore(score);
                comment.setUserId(userID);

                comments.add(comment);
            }
            commentRepository.saveAll(comments);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private CloseableHttpResponse getCloseableHttpResponse(String url, CloseableHttpClient client) throws IOException {
        return client.execute(new HttpGet(url));
    }


}
