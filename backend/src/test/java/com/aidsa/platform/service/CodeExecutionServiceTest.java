package com.aidsa.platform.service;

import com.aidsa.platform.dto.CodeRunRequest;
import com.aidsa.platform.dto.CodeRunResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeExecutionServiceTest {

    private final CodeExecutionService service = new CodeExecutionService();

    @Test
    void javaCodeExecutesSuccessfully() {
        String code = """
                public class Solution {
                    public static void main(String[] args) {
                        System.out.println("Hello Java");
                    }
                }
                """;

        CodeRunResponse response =
                service.runCode(new CodeRunRequest("java", code));

        assertTrue(response.isSuccess());
        assertEquals("Hello Java" + System.lineSeparator(), response.getOutput());
        assertTrue(response.getError().isEmpty());
    }

    @Test
    void pythonCodeExecutesSuccessfully() {
        String code = """
                print("Hello Python")
                """;

        CodeRunResponse response =
                service.runCode(new CodeRunRequest("python", code));

        assertTrue(response.isSuccess());
        assertEquals("Hello Python" + System.lineSeparator(), response.getOutput());
        assertTrue(response.getError().isEmpty());
    }

    @Test
    void javascriptCodeExecutesSuccessfully() {
        String code = """
                console.log("Hello JavaScript");
                """;

        CodeRunResponse response =
                service.runCode(new CodeRunRequest("javascript", code));

        assertTrue(response.isSuccess());
        assertEquals("Hello JavaScript" + System.lineSeparator(), response.getOutput());
        assertTrue(response.getError().isEmpty());
    }

    @Test
    void cppCodeExecutesSuccessfully() {
        String code = """
                #include <iostream>

                int main() {
                    std::cout << "Hello C++" << std::endl;
                    return 0;
                }
                """;

        CodeRunResponse response =
                service.runCode(new CodeRunRequest("cpp", code));

        assertTrue(response.isSuccess(), "C++ execution failed: " + response.getError());
        assertEquals("Hello C++" + System.lineSeparator(), response.getOutput());
        assertTrue(response.getError().isEmpty());
    }

    @Test
    void emptyCodeIsRejected() {
        CodeRunResponse response =
                service.runCode(new CodeRunRequest("java", ""));

        assertFalse(response.isSuccess());
        assertEquals("Code cannot be empty.", response.getError());
    }

    @Test
    void missingLanguageIsRejected() {
        String code = """
                public class Solution {
                    public static void main(String[] args) {
                        System.out.println("Hello");
                    }
                }
                """;

        CodeRunResponse response =
                service.runCode(new CodeRunRequest(null, code));

        assertFalse(response.isSuccess());
        assertEquals("Programming language is required.", response.getError());
    }

    @Test
    void unsupportedLanguageIsRejected() {
        CodeRunResponse response =
                service.runCode(new CodeRunRequest("ruby", "puts 'Hello'"));

        assertFalse(response.isSuccess());
        assertEquals("Unsupported language: ruby", response.getError());
    }

    @Test
    void javaCompilationErrorIsHandled() {
        String code = """
                public class Solution {
                    public static void main(String[] args) {
                        this is invalid Java;
                    }
                }
                """;

        CodeRunResponse response =
                service.runCode(new CodeRunRequest("java", code));

        assertFalse(response.isSuccess());
        assertFalse(response.getError().isEmpty());
    }

    @Test
    void javaRuntimeErrorIsHandled() {
        String code = """
                public class Solution {
                    public static void main(String[] args) {
                        int value = 10 / 0;
                        System.out.println(value);
                    }
                }
                """;

        CodeRunResponse response =
                service.runCode(new CodeRunRequest("java", code));

        assertFalse(response.isSuccess());
        assertFalse(response.getError().isEmpty());
    }

    @Test
    void javaInfiniteLoopTimesOut() {
        String code = """
                public class Solution {
                    public static void main(String[] args) {
                        while (true) {
                        }
                    }
                }
                """;

        CodeRunResponse response =
                service.runCode(new CodeRunRequest("java", code));

        assertFalse(response.isSuccess());
        assertTrue(
                response.getError().contains("timed out")
                        || response.getError().contains("time limit")
        );
    }
    @Test
    void javaCodeAcceptsInput() {
        String code = """
                import java.util.Scanner;

                public class Solution {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        int n = sc.nextInt();
                        System.out.println(n * 2);
                    }
                }
                """;

        CodeRunResponse response =
                service.runCode(
                        new CodeRunRequest("java", code),
                        "5\n"
                );

        assertTrue(
                response.isSuccess(),
                "Java stdin execution failed: " + response.getError()
        );
        assertEquals(
                "10" + System.lineSeparator(),
                response.getOutput()
        );
    }

    @Test
    void pythonCodeAcceptsInput() {
        String code = """
                n = int(input())
                print(n * 2)
                """;

        CodeRunResponse response =
                service.runCode(
                        new CodeRunRequest("python", code),
                        "5\n"
                );

        assertTrue(
                response.isSuccess(),
                "Python stdin execution failed: " + response.getError()
        );
        assertEquals(
                "10" + System.lineSeparator(),
                response.getOutput()
        );
    }

    @Test
    void javascriptCodeAcceptsInput() {
        String code = """
                let input = "";
                process.stdin.on("data", data => input += data);
                process.stdin.on("end", () => {
                    const n = parseInt(input.trim());
                    console.log(n * 2);
                });
                """;

        CodeRunResponse response =
                service.runCode(
                        new CodeRunRequest("javascript", code),
                        "5\n"
                );

        assertTrue(
                response.isSuccess(),
                "JavaScript stdin execution failed: " + response.getError()
        );
        assertEquals(
                "10" + System.lineSeparator(),
                response.getOutput()
        );
    }

    @Test
    void cppCodeAcceptsInput() {
        String code = """
                #include <iostream>

                int main() {
                    int n;
                    std::cin >> n;
                    std::cout << n * 2 << std::endl;
                    return 0;
                }
                """;

        CodeRunResponse response =
                service.runCode(
                        new CodeRunRequest("cpp", code),
                        "5\n"
                );

        assertTrue(
                response.isSuccess(),
                "C++ stdin execution failed: " + response.getError()
        );
        assertEquals(
                "10" + System.lineSeparator(),
                response.getOutput()
        );
    }
}