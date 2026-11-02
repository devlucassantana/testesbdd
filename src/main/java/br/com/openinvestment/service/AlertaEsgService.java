package br.com.openinvestment.service;

import br.com.openinvestment.entity.AlertaEsg;
import br.com.openinvestment.exception.ResourceNotFoundException;
import br.com.openinvestment.repository.AlertaEsgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaEsgService {

    private final AlertaEsgRepository alertaRepository;

    public List<AlertaEsg> listarPendentes() {
        return alertaRepository.findByStatusAlertaOrderByDataAlertaDesc("PENDENTE");
    }

    public List<AlertaEsg> listarPorProjeto(Long idProjeto) {
        return alertaRepository.findByProjetoIdProjetoOrderByDataAlertaDesc(idProjeto);
    }

    @Transactional
    public AlertaEsg marcarComoLido(Long id) {
        AlertaEsg alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta não encontrado: " + id));
        alerta.setStatusAlerta("LIDO");
        return alertaRepository.save(alerta);
    }

    @Transactional
    public AlertaEsg resolver(Long id) {
        AlertaEsg alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta não encontrado: " + id));
        alerta.setStatusAlerta("RESOLVIDO");
        return alertaRepository.save(alerta);
    }
}
