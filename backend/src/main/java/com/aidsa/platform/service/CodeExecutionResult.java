package com.aidsa.platform.service;

public class CodeExecutionResult {

    private final boolean success;
    private final String output;
    private final String error;
    private final boolean compilationError;
    private final boolean timedOut;

    public CodeExecutionResult(
            boolean success,
            String output,
            String error,
            boolean compilationError,
            boolean timedOut
    ) {
        this.success = success;
        this.output = output;
        this.error = error;
        this.compilationError = compilationError;
        this.timedOut = timedOut;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getOutput() {
        return output;
    }

    public String getError() {
        return error;
    }

    public boolean isCompilationError() {
        return compilationError;
    }

    public boolean isTimedOut() {
        return timedOut;
    }
}