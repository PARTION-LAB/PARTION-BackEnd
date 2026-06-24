package com.partion.ai.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
public class AiDocumentSearchService {

    private static final List<String> DOCUMENT_PATHS = List.of(
            "ai-docs/partion-service-guide.md",
            "ai-docs/sto-guide.md"
    );
    private static final int MAX_CONTEXT_SECTION_COUNT = 1;

    public String searchRelevantContext(String question) {
        List<String> sections = DOCUMENT_PATHS.stream()
                .map(this::loadDocument)
                .flatMap(document -> splitSections(document).stream())
                .toList();

        if (isStoDefinitionQuestion(question)) {
            return findSectionByTitle(sections, "STO의 의미");
        }

        return sections.stream()
                .filter(section -> calculateScore(section, question) > 0)
                .sorted((first, second) ->
                        Integer.compare(
                                calculateScore(second, question),
                                calculateScore(first, question)
                        )
                )
                .limit(MAX_CONTEXT_SECTION_COUNT)
                .reduce((first, second) -> first + "\n\n" + second)
                .orElseGet(() -> getFallbackContext(question, sections));
    }

    private String loadDocument(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("AI 안내 문서를 읽을 수 없습니다: " + path, e);
        }
    }

    private List<String> splitSections(String document) {
        return Arrays.stream(document.split("(?=## )"))
                .map(String::trim)
                .filter(section -> !section.isBlank())
                .toList();
    }

    private int calculateScore(String section, String question) {
        List<String> keywords = extractKeywords(question);
        String normalizedSection = normalize(section);

        int score = 0;

        for (String keyword : keywords) {
            if (normalizedSection.contains(keyword)) {
                score++;
            }
        }

        return score;
    }

    private List<String> extractKeywords(String question) {
        String normalizedQuestion = normalize(question);

        if (isStoRelatedQuestion(normalizedQuestion)) {
            return List.of("sto", "security token offering", "토큰증권", "증권형토큰");
        }

        return Arrays.stream(normalizedQuestion.split("\\s+"))
                .map(String::trim)
                .filter(word -> word.length() >= 2)
                .toList();
    }

    private boolean isStoDefinitionQuestion(String question) {
        String normalizedQuestion = normalize(question);

        return isStoRelatedQuestion(normalizedQuestion)
                && (normalizedQuestion.contains("뭐")
                || normalizedQuestion.contains("의미")
                || normalizedQuestion.contains("정의")
                || normalizedQuestion.contains("란"));
    }

    private boolean isStoRelatedQuestion(String normalizedQuestion) {
        return normalizedQuestion.contains("sto")
                || normalizedQuestion.contains("토큰증권")
                || normalizedQuestion.contains("증권형토큰")
                || normalizedQuestion.contains("security token offering");
    }

    private String findSectionByTitle(List<String> sections, String title) {
        return sections.stream()
                .filter(section -> section.contains(title))
                .findFirst()
                .orElse("관련 문서를 찾지 못했습니다.");
    }

    private String getFallbackContext(String question, List<String> sections) {
        String normalizedQuestion = normalize(question);

        if (isStoRelatedQuestion(normalizedQuestion)) {
            return findSectionByTitle(sections, "STO의 의미");
        }

        return "관련 문서를 찾지 못했습니다.";
    }

    private String normalize(String text) {
        return text.toLowerCase()
                .replace("증권형 토큰", "증권형토큰")
                .replace("토큰 증권", "토큰증권")
                .replaceAll("\\bsto[가-힣]*\\b", "sto")
                .replaceAll("[^가-힣a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}