package cse.java2.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalyzeController {
    @GetMapping("/analyze")
    public String analyze() {
        return "analyze";
    }
}
