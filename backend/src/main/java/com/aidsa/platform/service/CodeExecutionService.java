package com.aidsa.platform.service;

import com.aidsa.platform.dto.CodeRunRequest;
import com.aidsa.platform.dto.CodeRunResponse;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

@Service
public class CodeExecutionService {

    private static final long TIMEOUT_SECONDS = 5;

    public CodeRunResponse runCode(CodeRunRequest request) {

        if (request == null || request.getCode() == null ||
                request.getCode().trim().isEmpty()) {

            return new CodeRunResponse(
                    false,
                    "",
                    "Code cannot be empty."
            );
        }

        String language = request.getLanguage();

        if (language == null || language.trim().isEmpty()) {
            return new CodeRunResponse(
                    false,
                    "",
                    "Programming language is required."
            );
        }

        Path tempDirectory = null;

        try {

            tempDirectory = Files.createTempDirectory("ai-dsa-code-");

            String fileName;
            List<String> compileCommand = null;
            List<String> runCommand;

            switch (language.toLowerCase()) {

                case "java":

                    fileName = "Solution.java";

                    Files.writeString(
                            tempDirectory.resolve(fileName),
                            request.getCode(),
                            StandardCharsets.UTF_8
                    );

                    compileCommand = List.of(
                            "javac",
                            fileName
                    );

                    runCommand = List.of(
                            "java",
                            "Solution"
                    );

                    break;

                case "python":

                    fileName = "solution.py";

                    Files.writeString(
                            tempDirectory.resolve(fileName),
                            request.getCode(),
                            StandardCharsets.UTF_8
                    );

                    runCommand = List.of(
                            "python",
                            fileName
                    );

                    break;

                case "javascript":

                    fileName = "solution.js";

                    Files.writeString(
                            tempDirectory.resolve(fileName),
                            request.getCode(),
                            StandardCharsets.UTF_8
                    );

                    runCommand = List.of(
                            "node",
                            fileName
                    );

                    break;

                case "cpp":

                    fileName = "solution.cpp";

                    Files.writeString(
                            tempDirectory.resolve(fileName),
                            request.getCode(),
                            StandardCharsets.UTF_8
                    );

                    compileCommand = List.of(
                            "g++",
                            fileName,
                            "-o",
                            "solution.exe"
                    );

                    runCommand = List.of(
                            "solution.exe"
                    );

                    break;

                default:

                    return new CodeRunResponse(
                            false,
                            "",
                            "Unsupported language: " + language
                    );
            }

            /*
             * ---------------------------------------------------------
             * COMPILE
             * ---------------------------------------------------------
             */

            if (compileCommand != null) {

                ProcessResult compileResult =
                        executeProcess(
                                compileCommand,
                                tempDirectory
                        );

                if (compileResult.timedOut) {

                    return new CodeRunResponse(
                            false,
                            "",
                            "Compilation timed out."
                    );
                }

                if (compileResult.exitCode != 0) {

                    return new CodeRunResponse(
                            false,
                            "",
                            compileResult.error
                    );
                }
            }

            /*
             * ---------------------------------------------------------
             * RUN
             * ---------------------------------------------------------
             */

            ProcessResult runResult =
                    executeProcess(
                            runCommand,
                            tempDirectory
                    );

            if (runResult.timedOut) {

                return new CodeRunResponse(
                        false,
                        "",
                        "Execution timed out. Maximum allowed time is "
                                + TIMEOUT_SECONDS + " seconds."
                );
            }

            if (runResult.exitCode != 0) {

                return new CodeRunResponse(
                        false,
                        runResult.output,
                        runResult.error
                );
            }

            return new CodeRunResponse(
                    true,
                    runResult.output,
                    ""
            );

        } catch (Exception e) {

            return new CodeRunResponse(
                    false,
                    "",
                    "Execution error: " + e.getMessage()
            );

        } finally {

            if (tempDirectory != null) {
                deleteDirectory(tempDirectory);
            }
        }
    }

    /*
     * -------------------------------------------------------------
     * PROCESS EXECUTION
     * -------------------------------------------------------------
     */

    private ProcessResult executeProcess(
            List<String> command,
            Path workingDirectory
    ) throws IOException, InterruptedException {

        ProcessBuilder processBuilder =
                new ProcessBuilder(command);

        processBuilder.directory(workingDirectory.toFile());

        processBuilder.redirectErrorStream(false);

        Process process = processBuilder.start();

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        Future<String> outputFuture =
                executor.submit(() ->
                        readStream(process.getInputStream())
                );

        Future<String> errorFuture =
                executor.submit(() ->
                        readStream(process.getErrorStream())
                );

        boolean finished =
                process.waitFor(
                        TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                );

        if (!finished) {

            process.destroyForcibly();

            executor.shutdownNow();

            return new ProcessResult(
                    -1,
                    "",
                    "Process exceeded execution time limit.",
                    true
            );
        }

        String output;

        String error;

        try {

            output = outputFuture.get(
                    1,
                    TimeUnit.SECONDS
            );

        } catch (Exception e) {

            output = "";
        }

        try {

            error = errorFuture.get(
                    1,
                    TimeUnit.SECONDS
            );

        } catch (Exception e) {

            error = "";
        }

        executor.shutdown();

        return new ProcessResult(
                process.exitValue(),
                output,
                error,
                false
        );
    }

    /*
     * -------------------------------------------------------------
     * READ PROCESS STREAM
     * -------------------------------------------------------------
     */

    private String readStream(InputStream inputStream)
            throws IOException {

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     inputStream,
                                     StandardCharsets.UTF_8
                             )
                     )) {

            StringBuilder result =
                    new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {

                result.append(line)
                        .append(System.lineSeparator());
            }

            return result.toString();
        }
    }

    /*
     * -------------------------------------------------------------
     * DELETE TEMPORARY DIRECTORY
     * -------------------------------------------------------------
     */

    private void deleteDirectory(Path directory) {

        try {

            if (!Files.exists(directory)) {
                return;
            }

            Files.walk(directory)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {

                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }

                    });

        } catch (IOException ignored) {
        }
    }

    /*
     * -------------------------------------------------------------
     * PROCESS RESULT
     * -------------------------------------------------------------
     */

    private static class ProcessResult {

        private final int exitCode;
        private final String output;
        private final String error;
        private final boolean timedOut;

        ProcessResult(
                int exitCode,
                String output,
                String error,
                boolean timedOut
        ) {

            this.exitCode = exitCode;
            this.output = output;
            this.error = error;
            this.timedOut = timedOut;
        }
    }
}