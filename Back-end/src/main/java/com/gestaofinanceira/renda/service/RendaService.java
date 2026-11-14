package com.gestaofinanceira.renda.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.exception.RecursoNaoEncontradoException;
import com.gestaofinanceira.exception.RegraDeNegocioException;
import com.gestaofinanceira.renda.dto.RendaRequest;
import com.gestaofinanceira.renda.dto.RendaResponse;
import com.gestaofinanceira.renda.entity.Periodicidade;
import com.gestaofinanceira.renda.entity.Renda;
import com.gestaofinanceira.renda.repository.RendaRepository;

@Service
public class RendaService {

    private final RendaRepository rendaRepository;

    public RendaService(RendaRepository rendaRepository) {
        this.rendaRepository = rendaRepository;
    }

    @Transactional(readOnly = true)
    public List<RendaResponse> listar(Long usuarioId) {
        return rendaRepository.findAllByUsuarioIdAndAtivoTrueOrderByNome(usuarioId)
                .stream()
                .map(RendaResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public RendaResponse buscarPorId(Long id, Long usuarioId) {
        return RendaResponse.de(buscarOuFalhar(id, usuarioId));
    }

    @Transactional
    public RendaResponse criar(RendaRequest request, Long usuarioId) {
        boolean recorrente = Boolean.TRUE.equals(request.recorrente());
        Periodicidade periodicidade = validarPeriodicidade(recorrente, request.periodicidade());

        Renda renda = new Renda(
                request.nome().trim(),
                request.tipo(),
                request.valorReferencia(),
                recorrente,
                periodicidade,
                usuarioId);
        return RendaResponse.de(rendaRepository.save(renda));
    }

    @Transactional
    public RendaResponse atualizar(Long id, RendaRequest request, Long usuarioId) {
        Renda renda = buscarOuFalhar(id, usuarioId);
        boolean recorrente = Boolean.TRUE.equals(request.recorrente());
        Periodicidade periodicidade = validarPeriodicidade(recorrente, request.periodicidade());

        renda.atualizarDados(
                request.nome().trim(),
                request.tipo(),
                request.valorReferencia(),
                recorrente,
                periodicidade);
        return RendaResponse.de(rendaRepository.save(renda));
    }

    /** Exclusao logica: a linha continua no banco (RN005). */
    @Transactional
    public void excluir(Long id, Long usuarioId) {
        Renda renda = buscarOuFalhar(id, usuarioId);
        renda.desativar();
        rendaRepository.save(renda);
    }

    private Periodicidade validarPeriodicidade(boolean recorrente, Periodicidade periodicidade) {
        if (recorrente && periodicidade == null) {
            throw new RegraDeNegocioException(
                    "Informe a periodicidade para uma renda recorrente");
        }
        // renda nao recorrente nao tem periodicidade
        return recorrente ? periodicidade : null;
    }

    private Renda buscarOuFalhar(Long id, Long usuarioId) {
        return rendaRepository.findByIdAndUsuarioIdAndAtivoTrue(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Renda com id " + id + " não encontrada"));
    }
}
