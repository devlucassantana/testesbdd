package br.com.openinvestment.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "impacto_social")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ImpactoSocial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_impacto")
    private Long idImpacto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_projeto", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "criadoPor"})
    private ProjetoSocial projeto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_beneficiario", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Beneficiario beneficiario;

    @Column(name = "tipo_impacto", nullable = false, length = 60)
    private String tipoImpacto;

    @Column(length = 500)
    private String descricao;

    @Column(name = "data_registro")
    private LocalDateTime dataRegistro;

    @PrePersist
    protected void onCreate() {
        dataRegistro = LocalDateTime.now();
    }
}
