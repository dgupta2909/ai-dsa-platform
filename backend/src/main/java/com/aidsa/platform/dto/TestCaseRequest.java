package com.aidsa.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TestCaseRequest(

        @NotBlank(message = "Input data is required") String inputData,

        @NotBlank(message = "Expected output is required") String expectedOutput,

        @NotNull(message = "Hidden flag is required") Boolean hidden) {
}