package com.analyzer.model;

import java.util.List;
import java.util.Map;

public record AnalysisResult(
    int wordCount,
    int sentenceCount,
    int paragraphCount,
    double readingTimeMinutes,
    List<String> topKeywords,
    String sentiment,
    double avgWordsPerSentence,
    Map<String, Integer> keywordFrequency
) {}