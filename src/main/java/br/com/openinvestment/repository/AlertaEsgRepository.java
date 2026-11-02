package br.com.openinvestment.repository;

import br.com.openinvestment.entity.AlertaEsg;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertaEsgRepository extends JpaRepository<AlertaEsg, Long> {
    List<AlertaEsg> findByProjetoIdProjetoOrderByDataAlertaDesc(Long idProjeto);
    List<AlertaEsg> findByStatusAlertaOrderByDataAlertaDesc(String status);
}
