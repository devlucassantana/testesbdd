package br.com.openinvestment.controller;

import br.com.openinvestment.dto.request.ImpactoRequest;
import br.com.openinvestment.entity.ImpactoSocial;
import br.com.openinvestment.service.ImpactoSocialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/impactos")
@RequiredArgsConstructor
@Tag(name = "Impacto Social", description = "Registro de impactos e beneficiários atendidos")
@SecurityRequirement(name = "bearerAuth")
public class ImpactoController {

    private final ImpactoSocialService service;

    @GetMapping("/projeto/{idProjeto}")
    @Operation(summary = "Listar impactos de um projeto")
    public ResponseEntity<List<ImpactoSocial>> listar(@PathVariable Long idProjeto) {
        return ResponseEntity.ok(service.listarPorProjeto(idProjeto));
    }

    @GetMapping("/projeto/{idProjeto}/contagem")
    @Operation(summary = "Contar total de impactos registrados em um projeto")
    public ResponseEntity<Map<String, Long>> contar(@PathVariable Long idProjeto) {
        return ResponseEntity.ok(Map.of("totalImpactos", service.contarPorProjeto(idProjeto)));
    }

    @PostMapping
    @Operation(summary = "Registrar novo impacto social (inicia projeto automaticamente se PLANEJADO)")
    public ResponseEntity<ImpactoSocial> registrar(@Valid @RequestBody ImpactoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request));
    }
}
