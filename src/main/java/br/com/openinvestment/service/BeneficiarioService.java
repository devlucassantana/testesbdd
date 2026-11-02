package br.com.openinvestment.service;

import br.com.openinvestment.dto.request.BeneficiarioRequest;
import br.com.openinvestment.entity.Beneficiario;
import br.com.openinvestment.exception.BusinessException;
import br.com.openinvestment.exception.ResourceNotFoundException;
import br.com.openinvestment.repository.BeneficiarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiarioService {

    private final BeneficiarioRepository repository;

    public List<Beneficiario> listarTodos() { return repository.findAll(); }

    public Beneficiario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiário não encontrado: " + id));
    }

    @Transactional
    public Beneficiario criar(BeneficiarioRequest req) {
        if (repository.existsByCpf(req.cpf()))
            throw new BusinessException("CPF já cadastrado: " + req.cpf());

        return repository.save(Beneficiario.builder()
                .nome(req.nome()).cpf(req.cpf())
                .perfil(req.perfil()).regiao(req.regiao()).build());
    }

    @Transactional
    public Beneficiario atualizar(Long id, BeneficiarioRequest req) {
        Beneficiario b = buscarPorId(id);
        b.setNome(req.nome());
        b.setPerfil(req.perfil());
        b.setRegiao(req.regiao());
        return repository.save(b);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarPorId(id));
    }
}
