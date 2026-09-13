package com.aidsa.platform.controller;

import com.aidsa.platform.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private com.aidsa.platform.repository.UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private String validToken;

    @BeforeEach
    void setUp() {
        String email = "recommendation.tester@example.com";

        if (!userRepository.existsByEmail(email)) {
            userRepository.save(
                    new com.aidsa.platform.model.User(
                            "Recommendation Tester",
                            email,
                            passwordEncoder.encode("Password123!")
                    )
            );
        }

        validToken = "Bearer " + jwtService.generateToken(email);
    }

    @Test
    void getRecommendationsWithoutTokenReturnsUnauthorized401()
            throws Exception {

        mockMvc.perform(get("/api/recommendations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getRecommendationsWithTokenReturnsRecommendations()
            throws Exception {

        mockMvc.perform(
                        get("/api/recommendations")
                                .header("Authorization", validToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()")
                        .value(lessThanOrEqualTo(10)));
    }

    @Test
    void recommendationContainsExpectedFields()
            throws Exception {

        mockMvc.perform(
                        get("/api/recommendations")
                                .header("Authorization", validToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].problem",
                        notNullValue()))
                .andExpect(jsonPath("$[0].score",
                        notNullValue()))
                .andExpect(jsonPath("$[0].reason",
                        notNullValue()))
                .andExpect(jsonPath("$[0].priority",
                        notNullValue()));
    }
}