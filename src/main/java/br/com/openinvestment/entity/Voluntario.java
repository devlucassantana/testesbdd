package br.com.openinvestment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "voluntario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Voluntario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_voluntario")
    private Long idVoluntario;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false, unique = true, length = 200)
    private String email;

    @Column(length = 100)
    private String especialidade;

    @Column(name = "status_vol", length = 20)
    private String statusVol;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
        if (statusVol == null) statusVol = "ATIVO";
    }
}
