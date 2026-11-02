package br.com.openinvestment.controller;

import br.com.openinvestment.dto.request.BeneficiarioRequest;
import br.com.openinvestment.entity.Beneficiario;
import br.com.openinvestment.service.BeneficiarioService;
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
@RequestMapping("/api/beneficiarios")
@RequiredArgsConstructor
@Tag(name = "Beneficiários", description = "Cadastro de pessoas atendidas pelos projetos sociais")
@SecurityRequirement(name = "bearerAuth")
public class BeneficiarioController {

    private final BeneficiarioService service;

    @GetMapping
    @Operation(summary = "Listar todos os beneficiários")
    public ResponseEntity<List<Beneficiario>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar beneficiário por ID")
    public ResponseEntity<Beneficiario> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo beneficiário")
    public ResponseEntity<Beneficiario> criar(@Valid @RequestBody BeneficiarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar beneficiário")
    public ResponseEntity<Beneficiario> atualizar(
            @PathVariable Long id, @Valid @RequestBody BeneficiarioRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir beneficiário")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
