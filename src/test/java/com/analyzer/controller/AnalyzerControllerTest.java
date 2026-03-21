package com.analyzer.controller;

import com.analyzer.model.AnalysisResult;
import com.analyzer.service.AnalyzerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyzerController.class)
public class AnalyzerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyzerService analyzerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAnalyzeEndpoint() throws Exception {
        // Arrange
        String testText = "This is a test text for analysis.";
        AnalysisResult expectedResult = new AnalysisResult(
                7, 1, 1, 0.5, Collections.emptyList(), "positive", 7.0, Collections.emptyMap());

        when(analyzerService.analyze(testText)).thenReturn(expectedResult);

        // Act & Assert
        mockMvc.perform(post("/api/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("text", testText))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wordCount").value(7))
                .andExpect(jsonPath("$.sentenceCount").value(1));
    }

    @Test
    void testSummarizeEndpoint() throws Exception {
        // Arrange
        String testText = "This is a test. Another sentence. Third sentence. Fourth sentence.";
        String expectedSummary = "This is a test. Another sentence. Third sentence.";

        when(analyzerService.summarize(testText, 3)).thenReturn(expectedSummary);

        // Act & Assert
        mockMvc.perform(post("/api/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("text", testText, "maxSentences", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value(expectedSummary));
    }

    @Test
    void testSummarizeWithDefaultMaxSentences() throws Exception {
        // Arrange
        String testText = "First sentence. Second sentence. Third sentence. Fourth sentence.";
        String expectedSummary = "First sentence. Second sentence. Third sentence.";

        when(analyzerService.summarize(testText, 3)).thenReturn(expectedSummary);

        // Act & Assert
        mockMvc.perform(post("/api/summarize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("text", testText))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value(expectedSummary));
    }
}
