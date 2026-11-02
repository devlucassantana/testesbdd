package br.com.openinvestment.controller;

import br.com.openinvestment.entity.LogEsg;
import br.com.openinvestment.repository.LogEsgRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "Log de Auditoria", description = "Rastreamento completo de operações ESG (somente ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class LogController {

    private final LogEsgRepository logRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar logs com paginação")
    public ResponseEntity<Page<LogEsg>> listar(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<LogEsg> logs = logRepository.findAll(
                PageRequest.of(page, size, Sort.by("dataLog").descending()));
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/tabela/{tabela}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Filtrar logs por tabela de origem")
    public ResponseEntity<Page<LogEsg>> porTabela(
            @PathVariable String tabela,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<LogEsg> logs = logRepository.findByTabelaOrigemOrderByDataLogDesc(
                tabela, PageRequest.of(page, size));
        return ResponseEntity.ok(logs);
    }
}
