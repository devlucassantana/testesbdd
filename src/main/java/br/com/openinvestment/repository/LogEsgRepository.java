package br.com.openinvestment.repository;

import br.com.openinvestment.entity.LogEsg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogEsgRepository extends JpaRepository<LogEsg, Long> {
    Page<LogEsg> findByTabelaOrigemOrderByDataLogDesc(String tabelaOrigem, Pageable pageable);
}
