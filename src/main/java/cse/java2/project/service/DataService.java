package cse.java2.project.service;

import cse.java2.project.model.Question;
import cse.java2.project.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataService {

    private static final String API_URL = "https://api.stackexchange.com/2.3/questions?order=desc&sort=activity&tagged=java&site=stackoverflow";

    private final QuestionRepository questionRepository;

    @Autowired
    public DataService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public void fetchAndStoreData() {
        RestTemplate restTemplate = new RestTemplate();

        // Fetch data
        ResponseEntity<Map> response = restTemplate.getForEntity(API_URL, Map.class);
        Map<String, Object> fetchedData = response.getBody();

        // Parse questions
        List<LinkedHashMap> items = (ArrayList) fetchedData.get("items");
        List<Question> questions = new ArrayList<>();

        for (LinkedHashMap item : items) {
            Question question = new Question();
            question.setId(Long.parseLong(item.get("question_id").toString()));
            question.setTitle(item.get("title").toString());
            questions.add(question);
        }

        // Store questions in database
        questionRepository.saveAll(questions);
    }
}
