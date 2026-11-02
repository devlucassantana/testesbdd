package br.com.openinvestment.repository;

import br.com.openinvestment.entity.AlocacaoRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface AlocacaoRecursoRepository extends JpaRepository<AlocacaoRecurso, Long> {
    List<AlocacaoRecurso> findByProjetoIdProjeto(Long idProjeto);

    @Query("SELECT COALESCE(SUM(a.valor), 0) FROM AlocacaoRecurso a WHERE a.projeto.idProjeto = :idProjeto")
    BigDecimal sumValorByProjeto(@Param("idProjeto") Long idProjeto);
}
