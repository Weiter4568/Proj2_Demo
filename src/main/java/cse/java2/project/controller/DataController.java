package cse.java2.project.controller;

import com.google.gson.JsonObject;
import cse.java2.project.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data")
public class DataController {

  private final DataService dataService;

  @Autowired
  public DataController(DataService dataService) {
    this.dataService = dataService;
  }

  @GetMapping("/fetch")
  public ResponseEntity<String> fetchData() {
    try {
      dataService.fetchAndStoreData();
      return ResponseEntity.ok("Data fetching and storing process initiated successfully.");
    } catch (Exception e) {
      return ResponseEntity.status(500)
          .body(
              "An error occurred while initiating the data fetching and storing process: "
                  + e.getMessage());
    }
  }
}
