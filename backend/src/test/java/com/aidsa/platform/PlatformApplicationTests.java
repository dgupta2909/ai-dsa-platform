package com.aidsa.platform;

import com.aidsa.platform.dto.AuthResponse;
import com.aidsa.platform.dto.LoginRequest;
import com.aidsa.platform.dto.RegisterRequest;
import com.aidsa.platform.exception.EmailAlreadyExistsException;
import com.aidsa.platform.exception.InvalidCredentialsException;
import com.aidsa.platform.model.User;
import com.aidsa.platform.repository.UserRepository;
import com.aidsa.platform.security.JwtService;
import com.aidsa.platform.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PlatformApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void contextLoads() {
        assertNotNull(dataSource, "DataSource bean should be loaded");
    }

    @Test
    void databaseConnectionIsHealthy() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Connection should be established");
            assertTrue(connection.isValid(2), "Database connection should be valid");
        }
    }

    @Test
    void registerWithValidDataSucceeds() {
        String email = "alice@example.com";
        RegisterRequest request = new RegisterRequest("Alice Smith", email, "SecurePass123");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals("Alice Smith", response.fullName());
        assertEquals(email, response.email());
        assertTrue(response.success());
        assertNull(response.token(), "Registration should not issue JWT token automatically");

        Optional<User> savedUser = userRepository.findByEmail(email);
        assertTrue(savedUser.isPresent());
        assertEquals("Alice Smith", savedUser.get().getFullName());
    }

    @Test
    void duplicateEmailRegistrationThrowsConflict() {
        String email = "bob@example.com";
        RegisterRequest request1 = new RegisterRequest("Bob Jones", email, "Password123");
        authService.register(request1);

        RegisterRequest request2 = new RegisterRequest("Bob Duplicate", email, "AnotherPassword123");
        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request2));
    }

    @Test
    void passwordIsStoredAsBcryptHashNotPlaintext() {
        String email = "charlie@example.com";
        String rawPassword = "MySecretPassword!";
        RegisterRequest request = new RegisterRequest("Charlie Brown", email, rawPassword);

        authService.register(request);

        User savedUser = userRepository.findByEmail(email).orElseThrow();
        String storedHash = savedUser.getPasswordHash();

        assertNotNull(storedHash);
        assertFalse(storedHash.equals(rawPassword), "Stored password must not be plaintext");
        assertTrue(storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$"),
                "Stored password must be a BCrypt hash");
        assertTrue(passwordEncoder.matches(rawPassword, storedHash),
                "PasswordEncoder must match the raw password against stored hash");
    }

    @Test
    void successfulLoginReturnsAuthResponseWithJwt() {
        String email = "david@example.com";
        String rawPassword = "DavidPass789";
        authService.register(new RegisterRequest("David Miller", email, rawPassword));

        LoginRequest loginRequest = new LoginRequest(email, rawPassword);
        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("David Miller", response.fullName());
        assertEquals(email, response.email());
        assertTrue(response.success());
        assertNotNull(response.token(), "Login response must contain a JWT token");
        assertTrue(jwtService.validateToken(response.token()), "JWT token must be valid");
        assertEquals(email, jwtService.extractEmail(response.token()));
    }

    @Test
    void loginWithWrongPasswordThrowsInvalidCredentials() {
        String email = "emma@example.com";
        authService.register(new RegisterRequest("Emma Watson", email, "CorrectPass123"));

        LoginRequest badPassRequest = new LoginRequest(email, "WrongPass123");
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> authService.login(badPassRequest));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void loginWithNonExistentEmailThrowsGenericInvalidCredentials() {
        LoginRequest nonExistent = new LoginRequest("nonexistent@example.com", "SomePassword");
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> authService.login(nonExistent));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void jwtTokenValidationAndClaimsExtraction() {
        String email = "token.test@example.com";
        String token = jwtService.generateToken(email);

        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals(email, jwtService.extractEmail(token));
        assertTrue(jwtService.isTokenValid(token, email));
        assertFalse(jwtService.isTokenValid(token, "other@example.com"));
    }

    @Test
    void expiredJwtTokenFailsValidation() {
        String email = "expired.test@example.com";
        // generate token with negative expiration (-1000ms)
        String expiredToken = jwtService.generateToken(email, -1000L);

        assertFalse(jwtService.validateToken(expiredToken));
        assertFalse(jwtService.isTokenValid(expiredToken, email));
    }

    @Test
    void tamperedJwtTokenFailsValidation() {
        String email = "tampered.test@example.com";
        String token = jwtService.generateToken(email);
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        assertFalse(jwtService.validateToken(tamperedToken));
        assertFalse(jwtService.isTokenValid(tamperedToken, email));
    }

    @Autowired
    private com.aidsa.platform.service.ProblemService problemService;

    @Test
    void problemServiceReturnsSeedProblems() {
        var problems = problemService.getProblems(null, null, null);
        assertNotNull(problems);
        assertTrue(problems.size() >= 8, "Should have at least 8 seed problems");

        var twoSum = problemService.getProblemBySlug("two-sum");
        assertNotNull(twoSum);
        assertEquals("Two Sum", twoSum.title());
        assertEquals(com.aidsa.platform.model.Difficulty.EASY, twoSum.difficulty());
        assertEquals("Array", twoSum.category());
        assertNotNull(twoSum.description());

        var problemById = problemService.getProblemById(twoSum.id());
        assertNotNull(problemById);
        assertEquals("Two Sum", problemById.title());

        long count = problemService.getTotalProblemCount();
        assertTrue(count >= 8);
    }
}
