package com.aidsa.platform.ai;

import com.aidsa.platform.model.Problem;
import com.aidsa.platform.model.Submission;

public interface AIReviewProvider {

    AIReviewResult review(
            Problem problem,
            Submission submission
    );
}