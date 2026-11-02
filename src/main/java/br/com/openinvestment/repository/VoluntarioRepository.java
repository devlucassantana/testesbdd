package br.com.openinvestment.repository;

import br.com.openinvestment.entity.Voluntario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VoluntarioRepository extends JpaRepository<Voluntario, Long> {
    Optional<Voluntario> findByEmail(String email);
    boolean existsByCpf(String cpf);
    List<Voluntario> findByStatusVol(String status);
}
