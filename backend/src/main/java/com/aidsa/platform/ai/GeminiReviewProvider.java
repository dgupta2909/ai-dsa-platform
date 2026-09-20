package com.aidsa.platform.ai;

import com.aidsa.platform.model.Problem;
import com.aidsa.platform.model.Submission;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeminiReviewProvider implements AIReviewProvider {

    private final Client client;
    private final String model;
    private final ObjectMapper objectMapper;

    public GeminiReviewProvider(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model,
            ObjectMapper objectMapper) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY is missing or empty."
            );
        }

        System.out.println(
                "Gemini API key loaded. Length: " + apiKey.length()
        );

        this.client = Client.builder()
                .apiKey(apiKey)
                .build();

        this.model = model;
        this.objectMapper = objectMapper;
    }

    @Override
    public AIReviewResult review(
            Problem problem,
            Submission submission) {

        String prompt = buildPrompt(problem, submission);

        try {
            GenerateContentResponse response =
                    client.models.generateContent(
                            model,
                            prompt,
                            null
                    );

            String feedback = response.text();

            // Remove Markdown code fences if Gemini adds them.
            feedback = cleanJsonResponse(feedback);

            GeminiReviewJson review =
                    objectMapper.readValue(
                            feedback,
                            GeminiReviewJson.class
                    );

            return new AIReviewResult(
                    review.overallFeedback(),
                    review.approach(),
                    review.timeComplexity(),
                    review.spaceComplexity(),
                    review.codeQualityScore(),
                    review.strengths(),
                    review.improvements(),
                    "gemini",
                    model
            );

        } catch (Exception e) {
            System.err.println("===== GEMINI API ERROR =====");
            e.printStackTrace();
            System.err.println("===== END GEMINI API ERROR =====");

            throw new RuntimeException(
                    "Failed to generate structured Gemini code review",
                    e
            );
        }
    }

    private String cleanJsonResponse(String response) {

        if (response == null || response.isBlank()) {
            throw new IllegalStateException(
                    "Gemini returned an empty response."
            );
        }

        response = response.trim();

        if (response.startsWith("```json")) {
            response = response.substring(7);
        } else if (response.startsWith("```")) {
            response = response.substring(3);
        }

        if (response.endsWith("```")) {
            response = response.substring(
                    0,
                    response.length() - 3
            );
        }

        return response.trim();
    }

    private String buildPrompt(
            Problem problem,
            Submission submission) {

        return """
                You are an expert DSA interview code reviewer.

                Analyze the coding submission below.

                Problem:
                %s

                Language:
                %s

                Submitted Code:
                %s

                Return ONLY valid JSON.
                Do not use Markdown.
                Do not use ```json code fences.

                The JSON must have exactly these fields:

                {
                  "overallFeedback": "Concise overall assessment",
                  "approach": "Explain the algorithmic approach used",
                  "timeComplexity": "Example: O(N)",
                  "spaceComplexity": "Example: O(N)",
                  "codeQualityScore": 0,
                  "strengths": [
                    "Strength 1",
                    "Strength 2"
                  ],
                  "improvements": [
                    "Improvement 1",
                    "Improvement 2"
                  ]
                }

                Rules:

                1. Determine correctness from the submitted code.
                2. Identify the actual algorithm used.
                3. Determine the actual time complexity.
                4. Determine the actual space complexity.
                5. Give a code quality score from 0 to 100.
                6. List 2 to 5 specific strengths.
                7. List 2 to 5 specific improvements.
                8. Do not invent problems that are not present in the code.
                9. Do not rewrite the entire solution.
                10. Explain feedback clearly for a college student preparing
                    for software engineering interviews.
                """.formatted(
                problem.getDescription(),
                submission.getLanguage(),
                submission.getCode()
        );
    }

    /*
     * Internal DTO used only for parsing Gemini's JSON response.
     */
    private record GeminiReviewJson(
            String overallFeedback,
            String approach,
            String timeComplexity,
            String spaceComplexity,
            Integer codeQualityScore,
            List<String> strengths,
            List<String> improvements
    ) {
    }
}