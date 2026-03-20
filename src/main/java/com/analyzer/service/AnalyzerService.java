package com.analyzer.service;

import com.analyzer.model.AnalysisResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyzerService {

    // common words to ignore in keyword extraction
    private static final Set<String> STOP_WORDS = Set.of(
        "the","a","an","and","or","but","in","on","at",
        "to","for","of","with","by","is","are","was",
        "were","it","this","that","be","as","from","have"
    );

    public AnalysisResult analyze(String text) {
        // word count
        String[] words = text.trim().split("\\s+");
        int wordCount = words.length;

        // sentence count (split on . ! ?)
        String[] sentences = text.split("[.!?]+");
        int sentenceCount = (int) Arrays.stream(sentences)
            .filter(s -> !s.isBlank())
            .count();

        // paragraph count (split on blank lines)
        String[] paragraphs = text.split("\\n\\s*\\n");
        int paragraphCount = (int) Arrays.stream(paragraphs)
            .filter(p -> !p.isBlank())
            .count();

        // reading time (avg 200 words per minute)
        double readingTimeMinutes = wordCount / 200.0;

        // keyword frequency (excluding stop words)
        Map<String, Integer> freq = new HashMap<>();
        for (String word : words) {
            String clean = word.toLowerCase()
                               .replaceAll("[^a-z]", "");
            if (!clean.isEmpty() && !STOP_WORDS.contains(clean)) {
                freq.merge(clean, 1, Integer::sum);
            }
        }

        // top 5 keywords by frequency
        List<String> topKeywords = freq.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        // basic rule-based sentiment
        String sentiment = detectSentiment(text.toLowerCase());

        // avg words per sentence
        double avgWordsPerSentence = sentenceCount > 0
            ? (double) wordCount / sentenceCount
            : 0;

        return new AnalysisResult(
            wordCount,
            sentenceCount,
            paragraphCount,
            readingTimeMinutes,
            topKeywords,
            sentiment,
            avgWordsPerSentence,
            freq
        );
    }

    public String summarize(String text, int maxSentences) {
        String[] sentences = text.split("[.!?]+");

        // score each sentence by keyword frequency in full text
        Map<String, Integer> freq = new HashMap<>();
        for (String word : text.toLowerCase().split("\\s+")) {
            String clean = word.replaceAll("[^a-z]", "");
            if (!clean.isEmpty() && !STOP_WORDS.contains(clean))
                freq.merge(clean, 1, Integer::sum);
        }

        // score sentence = sum of frequencies of its words
        return Arrays.stream(sentences)
            .filter(s -> !s.isBlank())
            .map(s -> Map.entry(s.trim(), scoreSentence(s, freq)))
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(maxSentences)
            .map(Map.Entry::getKey)
            .collect(Collectors.joining(". ")) + ".";
    }

    private int scoreSentence(String sentence, Map<String, Integer> freq) {
        int score = 0;
        for (String word : sentence.toLowerCase().split("\\s+")) {
            String clean = word.replaceAll("[^a-z]", "");
            score += freq.getOrDefault(clean, 0);
        }
        return score;
    }

    private String detectSentiment(String text) {
        long positive = Arrays.stream(new String[]{
            "good","great","excellent","happy","love",
            "wonderful","fantastic","amazing","best","positive"
        }).filter(text::contains).count();

        long negative = Arrays.stream(new String[]{
            "bad","terrible","awful","hate","worst",
            "horrible","negative","poor","disappointing","sad"
        }).filter(text::contains).count();

        if (positive > negative) return "POSITIVE";
        if (negative > positive) return "NEGATIVE";
        return "NEUTRAL";
    }
}