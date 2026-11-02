package br.com.openinvestment.controller;

import br.com.openinvestment.dto.request.VoluntarioRequest;
import br.com.openinvestment.entity.Voluntario;
import br.com.openinvestment.service.VoluntarioService;
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
@RequestMapping("/api/voluntarios")
@RequiredArgsConstructor
@Tag(name = "Voluntários", description = "Gestão de voluntários e vínculo com projetos sociais")
@SecurityRequirement(name = "bearerAuth")
public class VoluntarioController {

    private final VoluntarioService service;

    @GetMapping
    @Operation(summary = "Listar todos os voluntários")
    public ResponseEntity<List<Voluntario>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar voluntário por ID")
    public ResponseEntity<Voluntario> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo voluntário")
    public ResponseEntity<Voluntario> criar(@Valid @RequestBody VoluntarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do voluntário")
    public ResponseEntity<Voluntario> atualizar(
            @PathVariable Long id, @Valid @RequestBody VoluntarioRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping("/{id}/inativar")
    @Operation(summary = "Inativar voluntário")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/vincular-projeto/{idProjeto}")
    @Operation(summary = "Vincular voluntário a projeto — gera log de auditoria e alerta ESG automaticamente")
    public ResponseEntity<Void> vincular(
            @PathVariable Long id, @PathVariable Long idProjeto) {
        service.vincularAoProjeto(id, idProjeto);
        return ResponseEntity.noContent().build();
    }
}
