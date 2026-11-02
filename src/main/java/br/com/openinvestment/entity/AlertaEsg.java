package br.com.openinvestment.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerta_esg")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AlertaEsg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Long idAlerta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_projeto", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "criadoPor"})
    private ProjetoSocial projeto;

    @Column(name = "tipo_alerta", nullable = false, length = 50)
    private String tipoAlerta;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @Column(name = "status_alerta", length = 20)
    private String statusAlerta;

    @Column(name = "data_alerta")
    private LocalDateTime dataAlerta;

    @PrePersist
    protected void onCreate() {
        dataAlerta = LocalDateTime.now();
        if (statusAlerta == null) statusAlerta = "PENDENTE";
    }
}
