package br.com.openinvestment.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "alocacao_recurso")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AlocacaoRecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alocacao")
    private Long idAlocacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_projeto", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "alocacoes", "impactos", "alertas", "criadoPor"})
    private ProjetoSocial projeto;

    @Column(length = 300)
    private String descricao;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "tipo_recurso", nullable = false, length = 50)
    private String tipoRecurso;

    @Column(name = "data_alocacao")
    private LocalDate dataAlocacao;

    @PrePersist
    protected void onCreate() {
        if (dataAlocacao == null) dataAlocacao = LocalDate.now();
    }
}
