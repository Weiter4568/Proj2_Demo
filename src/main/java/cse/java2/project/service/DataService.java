package cse.java2.project.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cse.java2.project.model.Answer;
import cse.java2.project.model.Codeblock;
import cse.java2.project.model.Comment;
import cse.java2.project.model.Question;
import cse.java2.project.repository.AnswerRepository;
import cse.java2.project.repository.CodeBlockRepository;
import cse.java2.project.repository.CommentRepository;
import cse.java2.project.repository.QuestionRepository;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.aspectj.apache.bcel.classfile.Code;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

  private static int ansNum = 1;
  private static int commentNum = 1;
  private final QuestionRepository questionRepository;
  private final AnswerRepository answerRepository;
  private final CommentRepository commentRepository;
  private final CodeBlockRepository codeBlockRepository;

  @Autowired
  public DataService(
      QuestionRepository questionRepository,
      AnswerRepository answerRepository,
      CommentRepository commentRepository,
      CodeBlockRepository codeBlockRepository) {
    this.questionRepository = questionRepository;
    this.answerRepository = answerRepository;
    this.commentRepository = commentRepository;
    this.codeBlockRepository = codeBlockRepository;
  }

  // Fetch question from the stack overflow
  public void fetchAndStoreData() {
    // Build the HTTP client
    CloseableHttpClient client = HttpClientBuilder.create().build();

    // Construct the API endpoint URL
    String url =
        "https://api.stackexchange.com/2.3/questions?order=desc&sort=activity&tagged=java&filter=withbody&site=stackoverflow&pagesize=100&&key=JIOpBoY6dilGh0AY)KI*NA((&page=";

    for (int i = 1; i <= 10; i++) {
      String requestURL = url;
      requestURL += i;
      try (CloseableHttpResponse response = getCloseableHttpResponse(requestURL, client)) {
        // Parse the response JSON
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        JsonObject jsonResponse = new Gson().fromJson(reader, JsonObject.class);

        Gson gson = new Gson();
        String test = gson.toJson(jsonResponse);
        //            System.out.println(test);

        // 指定输出文件路径
        String filePath =
            "/Users/weiter/Documents/SUSTech/2023Spring/CS209 Computer System Design & Application/Final Project/Final_Proj_Demo/questions/questions_";
        filePath += i;
        filePath += ".json";

        try (FileWriter writer = new FileWriter(filePath)) {
          // 将 JSON 字符串写入文件
          writer.write(test);
          System.out.println("Question: " + i + " JSON 文件已成功写入：" + filePath);
        } catch (IOException e) {
          System.out.println("写入 Question: " + i + " JSON 文件时出错：" + e.getMessage());
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
          int userID = 0;
          if (!questionObject
              .getAsJsonObject("owner")
              .get("user_type")
              .getAsString()
              .equals("does_not_exist")) {
            userID = questionObject.getAsJsonObject("owner").get("user_id").getAsInt();
          }
          int viewCnt = questionObject.get("view_count").getAsInt();

          // Process the body of the question, to extract the code block in the body
          String body = questionObject.get("body").getAsString();
          extractCodeBlocksFromHTML(body, id);

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
          question.setViewCnt(viewCnt);

          //                System.out.println(question.toString());
          // Add question to array
          questions.add(question);

          //                System.out.println(questionTitle);
          //                System.out.println("--------------------------------------------");
        }
        // Store questions in database
        questionRepository.saveAll(questions);
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        // Store answers in database
        fetchAns(questions);

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  // Fetch answer for each question
  private void fetchAns(List<Question> questions) {
    if (questions.size() == 0) return;
    String url = "https://api.stackexchange.com/2.3/questions/";
    for (int i = 0; i < questions.size(); i++) {
      url += questions.get(i).getId().toString();
      if (i != questions.size() - 1) url += ";";
    }
    url +=
        "/answers?order=desc&sort=votes&filter=withbody&site=stackoverflow&pagesize=100&key=JIOpBoY6dilGh0AY)KI*NA((&page=";

    // Build the HTTP client
    CloseableHttpClient client = HttpClientBuilder.create().build();
    int cnt = 1;
    while (true) {
      String requestURL = url;
      requestURL += cnt;
      cnt++;
      try (CloseableHttpResponse response = getCloseableHttpResponse(requestURL, client)) {

        // Parse the response JSON
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        JsonObject jsonResponse = new Gson().fromJson(reader, JsonObject.class);

        // Output answer JSON file to the corresponding position
        Gson gson = new Gson();

        String test = gson.toJson(jsonResponse);
        //            System.out.println(test);

        // 指定输出文件路径
        String filePath =
            "/Users/weiter/Documents/SUSTech/2023Spring/CS209 Computer System Design & Application/Final Project/Final_Proj_Demo/questions/answers_";
        filePath += ansNum;
        filePath += ".json";

        try (FileWriter writer = new FileWriter(filePath)) {
          // 将 JSON 字符串写入文件
          writer.write(test);
          System.out.println("Ans: " + ansNum + "JSON 文件已成功写入：" + filePath);
        } catch (IOException e) {
          System.out.println("写入Ans: " + ansNum + " JSON 文件时出错：" + e.getMessage());
        }
        ansNum++;

        // Extract the questions array from the response
        JsonArray answersArray = jsonResponse.getAsJsonArray("items");
        List<Answer> answers = new ArrayList<>();

        // Loop through answers and retrieve relevant information
        for (JsonElement answerElement : answersArray) {
          JsonObject answerObject = answerElement.getAsJsonObject();

          // Get info from answerObject
          int ansID = answerObject.get("answer_id").getAsInt();
          int questionID = answerObject.get("question_id").getAsInt();
          boolean isAccepted = answerObject.get("is_accepted").getAsBoolean();
          int score = answerObject.get("score").getAsInt();
          int userID = 0;
          if (!answerObject
              .getAsJsonObject("owner")
              .get("user_type")
              .getAsString()
              .equals("does_not_exist")) {
            userID = answerObject.getAsJsonObject("owner").get("user_id").getAsInt();
          }
          int acceptedDate = 0;
          if (isAccepted) {
            acceptedDate = answerObject.get("last_activity_date").getAsInt();
          }

          // Process the body of the answer, to extract the code block in the body
          String body = answerObject.get("body").getAsString();
          extractCodeBlocksFromHTML(body, ansID);

          // Store the info into the PostgreSQL
          Answer answer = new Answer();
          answer.setId(ansID);
          answer.setQuestionId(questionID);
          answer.setIsAccepted(isAccepted);
          answer.setScore(score);
          answer.setUserID(userID);
          answer.setAcceptedDate(acceptedDate);

          answers.add(answer);
        }
        answerRepository.saveAll(answers);
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        fetchComment(answers);

        if (!jsonResponse.get("has_more").getAsBoolean()) break;

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  
  // Fetch comment for each answer
  private void fetchComment(List<Answer> answers) {
    if (answers.size() == 0) return;
    String url = "https://api.stackexchange.com/2.3/answers/";
    for (int i = 0; i < answers.size(); i++) {
      url += answers.get(i).getId();
      if (i != answers.size() - 1) url += ";";
    }
    url +=
        "/comments?order=desc&sort=votes&site=stackoverflow&pagesize=100&key=JIOpBoY6dilGh0AY)KI*NA((&page=";

    // Build the HTTP client
    CloseableHttpClient client = HttpClientBuilder.create().build();

    int cnt = 1;
    while (true) {
      String requestURL = url;
      requestURL += cnt;
      cnt++;
      try (CloseableHttpResponse response = getCloseableHttpResponse(requestURL, client)) {
        // Parse the response JSON
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        JsonObject jsonResponse = new Gson().fromJson(reader, JsonObject.class);

        // Output comment JSON file to the corresponding position
        Gson gson = new Gson();

        String test = gson.toJson(jsonResponse);
        //            System.out.println(test);

        // 指定输出文件路径
        String filePath =
            "/Users/weiter/Documents/SUSTech/2023Spring/CS209 Computer System Design & Application/Final Project/Final_Proj_Demo/questions/comments_";
        filePath += commentNum;
        filePath += ".json";

        try (FileWriter writer = new FileWriter(filePath)) {
          // 将 JSON 字符串写入文件
          writer.write(test);
          System.out.println("Comments: " + commentNum + " JSON 文件已成功写入：" + filePath);
        } catch (IOException e) {
          System.out.println("写入 Comments: " + commentNum + " JSON 文件时出错：" + e.getMessage());
        }
        commentNum++;

        // Extract the questions array from the response
        JsonArray commentsArray = jsonResponse.getAsJsonArray("items");
        List<Comment> comments = new ArrayList<>();

        // Loop through comments and retrieve relevant information
        for (JsonElement commentElement : commentsArray) {
          JsonObject commentObject = commentElement.getAsJsonObject();

          // Get info from commentObject
          int commentId = commentObject.get("comment_id").getAsInt();
          int postID = commentObject.get("post_id").getAsInt();
          int score = commentObject.get("score").getAsInt();
          int userID = 0;
          if (!commentObject
              .getAsJsonObject("owner")
              .get("user_type")
              .getAsString()
              .equals("does_not_exist")) {
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

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        if (!jsonResponse.get("has_more").getAsBoolean()) break;

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private CloseableHttpResponse getCloseableHttpResponse(String url, CloseableHttpClient client)
      throws IOException {
    return client.execute(new HttpGet(url));
  }

  // Extract code block of each question or ans
  public void extractCodeBlocksFromHTML(String html, int post_id) {
    Document document = Jsoup.parse(html);
    Elements codeElements = document.select("pre > code");
    List<Codeblock> codeBlocks = new ArrayList<>();
    for (Element codeElement : codeElements) {
      Codeblock codeblock = new Codeblock();
      codeblock.setCodeBlock(codeElement.text());
      codeblock.setPostId(post_id);
      codeBlocks.add(codeblock);
    }
    this.codeBlockRepository.saveAll(codeBlocks);
  }
}
