package br.com.openinvestment.controller;

import br.com.openinvestment.dto.request.ProjetoRequest;
import br.com.openinvestment.dto.response.ProjetoResponse;
import br.com.openinvestment.service.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projetos")
@RequiredArgsConstructor
@Tag(name = "Projetos Sociais", description = "CRUD de projetos sociais ESG")
@SecurityRequirement(name = "bearerAuth")
public class ProjetoController {

    private final ProjetoService service;

    @GetMapping
    @Operation(summary = "Listar todos os projetos (filtrar por status opcional)")
    public ResponseEntity<List<ProjetoResponse>> listar(
            @RequestParam(required = false) String status) {
        List<ProjetoResponse> projetos = status != null
                ? service.listarPorStatus(status)
                : service.listarTodos();
        return ResponseEntity.ok(projetos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar projeto por ID")
    public ResponseEntity<ProjetoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/orcamento-critico")
    @Operation(summary = "Listar projetos com orçamento ≥ 80% utilizado")
    public ResponseEntity<List<ProjetoResponse>> orcamentoCritico() {
        return ResponseEntity.ok(service.listarOrcamentoCritico());
    }

    @PostMapping
    @Operation(summary = "Criar novo projeto social")
    public ResponseEntity<ProjetoResponse> criar(@Valid @RequestBody ProjetoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar projeto existente")
    public ResponseEntity<ProjetoResponse> atualizar(
            @PathVariable Long id, @Valid @RequestBody ProjetoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do projeto")
    public ResponseEntity<Void> atualizarStatus(
            @PathVariable Long id, @RequestParam String status) {
        service.atualizarStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir projeto (somente ADMIN)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
