package br.com.openinvestment.service;

import br.com.openinvestment.dto.request.VoluntarioRequest;
import br.com.openinvestment.entity.*;
import br.com.openinvestment.exception.BusinessException;
import br.com.openinvestment.exception.ResourceNotFoundException;
import br.com.openinvestment.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VoluntarioService {

    private final VoluntarioRepository voluntarioRepository;
    private final ProjetoSocialRepository projetoRepository;
    private final AlertaEsgRepository alertaRepository;
    private final LogEsgRepository logRepository;

    public List<Voluntario> listarTodos() { return voluntarioRepository.findAll(); }

    public Voluntario buscarPorId(Long id) {
        return voluntarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voluntário não encontrado: " + id));
    }

    @Transactional
    public Voluntario criar(VoluntarioRequest req) {
        if (voluntarioRepository.existsByCpf(req.cpf()))
            throw new BusinessException("CPF já cadastrado: " + req.cpf());

        return voluntarioRepository.save(Voluntario.builder()
                .nome(req.nome()).cpf(req.cpf())
                .email(req.email()).especialidade(req.especialidade()).build());
    }

    @Transactional
    public Voluntario atualizar(Long id, VoluntarioRequest req) {
        Voluntario v = buscarPorId(id);
        v.setNome(req.nome());
        v.setEmail(req.email());
        v.setEspecialidade(req.especialidade());
        return voluntarioRepository.save(v);
    }

    @Transactional
    public void inativar(Long id) {
        Voluntario v = buscarPorId(id);
        v.setStatusVol("INATIVO");
        voluntarioRepository.save(v);
    }

    // Vincula voluntário a projeto + gera log e alerta (equivale ao TRG_REGISTRA_VOLUNTARIO)
    @Transactional
    public void vincularAoProjeto(Long idVoluntario, Long idProjeto) {
        Voluntario voluntario = buscarPorId(idVoluntario);
        ProjetoSocial projeto = projetoRepository.findById(idProjeto)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado: " + idProjeto));

        // Log de auditoria
        logRepository.save(LogEsg.builder()
                .tabelaOrigem("voluntario_projeto")
                .operacao("INSERT")
                .idReferencia(idProjeto)
                .descricao(String.format("Voluntário '%s' (ID %d) vinculado ao projeto '%s' (ID %d).",
                        voluntario.getNome(), idVoluntario, projeto.getNomeProjeto(), idProjeto))
                .build());

        // Alerta
        alertaRepository.save(AlertaEsg.builder()
                .projeto(projeto)
                .tipoAlerta("NOVO_VOLUNTARIO")
                .mensagem(String.format("Novo voluntário '%s' — especialidade '%s' — vinculado ao projeto '%s'.",
                        voluntario.getNome(), voluntario.getEspecialidade(), projeto.getNomeProjeto()))
                .build());
    }
}
