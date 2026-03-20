package com.analyzer.controller;

import com.analyzer.model.AnalysisResult;
import com.analyzer.service.AnalyzerService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnalyzerController {

    private final AnalyzerService service;

    public AnalyzerController(AnalyzerService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public AnalysisResult analyze(@RequestBody Map<String, String> body) {
        return service.analyze(body.get("text"));
    }

    @PostMapping("/summarize")
    public Map<String, String> summarize(@RequestBody Map<String, Object> body) {
        String text = (String) body.get("text");
        int maxSentences = (int) body.getOrDefault("maxSentences", 3);
        return Map.of("summary", service.summarize(text, maxSentences));
    }
}