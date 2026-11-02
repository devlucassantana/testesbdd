package br.com.openinvestment.repository;

import br.com.openinvestment.entity.ProjetoSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjetoSocialRepository extends JpaRepository<ProjetoSocial, Long> {

    List<ProjetoSocial> findByStatusProjeto(String status);

    @Query("SELECT p FROM ProjetoSocial p WHERE p.odsVinculacao LIKE %:ods%")
    List<ProjetoSocial> findByOds(@Param("ods") String ods);

    @Query("""
        SELECT p FROM ProjetoSocial p
        WHERE p.orcamentoTotal > 0
          AND (COALESCE(p.verbaUtilizada, 0) / p.orcamentoTotal) >= 0.8
    """)
    List<ProjetoSocial> findProjetosComOrcamentoCritico();
}
