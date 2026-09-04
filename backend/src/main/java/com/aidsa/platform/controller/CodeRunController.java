package com.aidsa.platform.controller;

import com.aidsa.platform.dto.CodeRunRequest;
import com.aidsa.platform.dto.CodeRunResponse;
import com.aidsa.platform.service.CodeExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
public class CodeRunController {

    private final CodeExecutionService codeExecutionService;

    public CodeRunController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping("/run")
    public ResponseEntity<CodeRunResponse> runCode(
            @RequestBody CodeRunRequest request) {

        CodeRunResponse response = codeExecutionService.runCode(request);

        return ResponseEntity.ok(response);
    }
}