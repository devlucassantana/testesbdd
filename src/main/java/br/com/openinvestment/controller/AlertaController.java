package br.com.openinvestment.controller;

import br.com.openinvestment.entity.AlertaEsg;
import br.com.openinvestment.service.AlertaEsgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas ESG", description = "Alertas automáticos de governança e orçamento (ADMIN/GESTOR)")
@SecurityRequirement(name = "bearerAuth")
public class AlertaController {

    private final AlertaEsgService service;

    @GetMapping("/pendentes")
    @Operation(summary = "Listar todos os alertas pendentes")
    public ResponseEntity<List<AlertaEsg>> pendentes() {
        return ResponseEntity.ok(service.listarPendentes());
    }

    @GetMapping("/projeto/{idProjeto}")
    @Operation(summary = "Listar alertas de um projeto específico")
    public ResponseEntity<List<AlertaEsg>> porProjeto(@PathVariable Long idProjeto) {
        return ResponseEntity.ok(service.listarPorProjeto(idProjeto));
    }

    @PatchMapping("/{id}/ler")
    @Operation(summary = "Marcar alerta como lido")
    public ResponseEntity<AlertaEsg> marcarLido(@PathVariable Long id) {
        return ResponseEntity.ok(service.marcarComoLido(id));
    }

    @PatchMapping("/{id}/resolver")
    @Operation(summary = "Marcar alerta como resolvido")
    public ResponseEntity<AlertaEsg> resolver(@PathVariable Long id) {
        return ResponseEntity.ok(service.resolver(id));
    }
}
