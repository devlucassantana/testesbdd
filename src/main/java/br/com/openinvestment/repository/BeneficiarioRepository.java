package br.com.openinvestment.repository;

import br.com.openinvestment.entity.Beneficiario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BeneficiarioRepository extends JpaRepository<Beneficiario, Long> {
    Optional<Beneficiario> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
}
