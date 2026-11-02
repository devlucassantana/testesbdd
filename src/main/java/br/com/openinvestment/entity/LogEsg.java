package br.com.openinvestment.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_esg")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LogEsg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @Column(name = "tabela_origem", nullable = false, length = 60)
    private String tabelaOrigem;

    @Column(nullable = false, length = 30)
    private String operacao;

    @Column(name = "id_referencia")
    private Long idReferencia;

    @Column(nullable = false, length = 500)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "senha", "authorities"})
    private Usuario usuario;

    @Column(name = "data_log")
    private LocalDateTime dataLog;

    @PrePersist
    protected void onCreate() {
        dataLog = LocalDateTime.now();
    }
}
