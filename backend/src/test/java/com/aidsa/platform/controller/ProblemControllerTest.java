package com.aidsa.platform.controller;

import com.aidsa.platform.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProblemControllerTest {

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
        String email = "problem.tester@example.com";
        if (!userRepository.existsByEmail(email)) {
            userRepository.save(new com.aidsa.platform.model.User("Problem Tester", email, passwordEncoder.encode("Password123!")));
        }
        validToken = "Bearer " + jwtService.generateToken(email);
    }

    @Test
    void getProblemsWithoutTokenReturnsUnauthorized401() throws Exception {
        mockMvc.perform(get("/api/problems"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProblemsWithTokenReturnsListOfProblems() throws Exception {
        mockMvc.perform(get("/api/problems")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(8))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].slug").exists())
                .andExpect(jsonPath("$[0].difficulty").exists())
                .andExpect(jsonPath("$[0].category").exists())
                .andExpect(jsonPath("$[0].tags").isArray());
    }

    @Test
    void getProblemsFilteredByDifficultyEasy() throws Exception {
        mockMvc.perform(get("/api/problems?difficulty=EASY")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(6))))
                .andExpect(jsonPath("$[0].difficulty").value("EASY"));
    }

    @Test
    void getProblemsFilteredByDifficultyMedium() throws Exception {
        mockMvc.perform(get("/api/problems?difficulty=MEDIUM")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].difficulty").value("MEDIUM"));
    }

    @Test
    void getProblemsFilteredByCategory() throws Exception {
        mockMvc.perform(get("/api/problems?category=Stack")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title").value("Valid Parentheses"));
    }

    @Test
    void getProblemsSearchByTitle() throws Exception {
        mockMvc.perform(get("/api/problems?search=Reverse")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Reverse Linked List"));
    }

    @Test
    void getProblemBySlugSucceeds() throws Exception {
        mockMvc.perform(get("/api/problems/slug/two-sum")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Two Sum"))
                .andExpect(jsonPath("$.slug").value("two-sum"))
                .andExpect(jsonPath("$.difficulty").value("EASY"))
                .andExpect(jsonPath("$.category").value("Array"))
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.tags").isArray());
    }

    @Test
    void getProblemByNonExistentSlugReturnsNotFound404() throws Exception {
        mockMvc.perform(get("/api/problems/slug/non-existent-slug-xyz")
                        .header("Authorization", validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getProblemByNonExistentIdReturnsNotFound404() throws Exception {
        mockMvc.perform(get("/api/problems/999999")
                        .header("Authorization", validToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
