package cse.java2.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ApiTestController {
  @GetMapping("/test/number/a")
  @ResponseBody
  public String numberA() {
    return "{\"name\":[\"10h ~ 1day\",\"10mins ~ 1h\",\"1week ~ 1mon\",\"1day ~ 3days\",\"1h~5h\",\"\\u003c\\u003d 10min\",\"3day ~ 1week\",\"\\u003e 3mons\"],\"value\":[1,1,1,3,3,2,3,2]}";
  }
}
