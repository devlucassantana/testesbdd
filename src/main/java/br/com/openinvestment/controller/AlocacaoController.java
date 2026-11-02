package br.com.openinvestment.controller;

import br.com.openinvestment.dto.request.AlocacaoRequest;
import br.com.openinvestment.entity.AlocacaoRecurso;
import br.com.openinvestment.service.AlocacaoService;
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
@RequestMapping("/api/alocacoes")
@RequiredArgsConstructor
@Tag(name = "Alocação de Recursos", description = "Gestão financeira dos projetos sociais")
@SecurityRequirement(name = "bearerAuth")
public class AlocacaoController {

    private final AlocacaoService service;

    @GetMapping("/projeto/{idProjeto}")
    @Operation(summary = "Listar alocações de um projeto")
    public ResponseEntity<List<AlocacaoRecurso>> listarPorProjeto(@PathVariable Long idProjeto) {
        return ResponseEntity.ok(service.listarPorProjeto(idProjeto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar alocação por ID")
    public ResponseEntity<AlocacaoRecurso> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar alocação de recurso (valida limite de orçamento)")
    public ResponseEntity<AlocacaoRecurso> alocar(@Valid @RequestBody AlocacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.alocar(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir alocação e recalcular verba utilizada")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
