package com.aidsa.platform.ai;

import com.aidsa.platform.model.Problem;
import com.aidsa.platform.model.Submission;
import org.springframework.stereotype.Component;

import java.util.List;

public class MockAIReviewProvider implements AIReviewProvider {

    @Override
    public AIReviewResult review(
            Problem problem,
            Submission submission
    ) {

        return new AIReviewResult(
                "Your solution was reviewed successfully. "
                        + "The submitted code provides a valid approach "
                        + "for the selected problem.",
                "The solution uses a straightforward approach based "
                        + "on the submitted implementation.",
                "O(n)",
                "O(n)",
                80,
                List.of(
                        "Solution is structured clearly.",
                        "The implementation is readable.",
                        "The submitted code is focused on the problem."
                ),
                List.of(
                        "Consider improving variable naming where appropriate.",
                        "Look for opportunities to simplify the implementation.",
                        "Add comments for non-obvious logic."
                ),
                "mock",
                "mock-v1"
        );
    }
}