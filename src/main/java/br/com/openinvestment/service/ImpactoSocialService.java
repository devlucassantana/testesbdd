package br.com.openinvestment.service;

import br.com.openinvestment.dto.request.ImpactoRequest;
import br.com.openinvestment.entity.Beneficiario;
import br.com.openinvestment.entity.ImpactoSocial;
import br.com.openinvestment.entity.ProjetoSocial;
import br.com.openinvestment.exception.ResourceNotFoundException;
import br.com.openinvestment.repository.BeneficiarioRepository;
import br.com.openinvestment.repository.ImpactoSocialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImpactoSocialService {

    private final ImpactoSocialRepository impactoRepository;
    private final ProjetoService projetoService;
    private final BeneficiarioRepository beneficiarioRepository;

    public List<ImpactoSocial> listarPorProjeto(Long idProjeto) {
        projetoService.findOrThrow(idProjeto);
        return impactoRepository.findByProjetoIdProjeto(idProjeto);
    }

    @Transactional
    public ImpactoSocial registrar(ImpactoRequest req) {
        ProjetoSocial projeto = projetoService.findOrThrow(req.idProjeto());
        Beneficiario beneficiario = beneficiarioRepository.findById(req.idBeneficiario())
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiário não encontrado: " + req.idBeneficiario()));

        ImpactoSocial impacto = ImpactoSocial.builder()
                .projeto(projeto)
                .beneficiario(beneficiario)
                .tipoImpacto(req.tipoImpacto())
                .descricao(req.descricao())
                .build();
        impactoRepository.save(impacto);

        // Transição automática PLANEJADO → EM_ANDAMENTO (equivale ao TRG_INICIA_PROJETO_POR_IMPACTO)
        projetoService.iniciarSeNecessario(projeto);

        return impacto;
    }

    public long contarPorProjeto(Long idProjeto) {
        return impactoRepository.countByProjetoIdProjeto(idProjeto);
    }
}
