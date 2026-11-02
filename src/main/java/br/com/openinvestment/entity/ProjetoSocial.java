package br.com.openinvestment.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projeto_social")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjetoSocial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_projeto")
    private Long idProjeto;

    @Column(name = "nome_projeto", nullable = false, length = 200)
    private String nomeProjeto;

    @Column(name = "descricao", columnDefinition = "CLOB")
    private String descricao;

    @Column(name = "ods_vinculacao", length = 100)
    private String odsVinculacao;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim_prev")
    private LocalDate dataFimPrev;

    @Column(name = "orcamento_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal orcamentoTotal;

    @Column(name = "verba_utilizada", precision = 15, scale = 2)
    private BigDecimal verbaUtilizada;

    @Column(name = "status_projeto", nullable = false, length = 20)
    private String statusProjeto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "senha", "authorities",
            "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled"})
    private Usuario criadoPor;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
        if (statusProjeto == null) statusProjeto = "PLANEJADO";
        if (verbaUtilizada == null) verbaUtilizada = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }
}
