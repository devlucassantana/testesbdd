package br.com.openinvestment.repository;

import br.com.openinvestment.entity.ImpactoSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ImpactoSocialRepository extends JpaRepository<ImpactoSocial, Long> {
    List<ImpactoSocial> findByProjetoIdProjeto(Long idProjeto);
    long countByProjetoIdProjeto(Long idProjeto);
}
