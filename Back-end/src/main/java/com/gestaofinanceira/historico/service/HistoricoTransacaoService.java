package com.gestaofinanceira.historico.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.exception.RecursoNaoEncontradoException;
import com.gestaofinanceira.historico.dto.HistoricoResponse;
import com.gestaofinanceira.historico.entity.AcaoHistorico;
import com.gestaofinanceira.historico.entity.HistoricoTransacao;
import com.gestaofinanceira.historico.repository.HistoricoTransacaoRepository;
import com.gestaofinanceira.transacao.entity.Transacao;

@Service
public class HistoricoTransacaoService {

    private final HistoricoTransacaoRepository historicoRepository;

    public HistoricoTransacaoService(HistoricoTransacaoRepository historicoRepository) {
        this.historicoRepository = historicoRepository;
    }

    // Chamado pelo TransacaoService ANTES de alterar ou excluir.
    @Transactional
    public void registrar(Transacao transacao, AcaoHistorico acao) {
        HistoricoTransacao registro = new HistoricoTransacao(
                transacao.getId(),
                acao,
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getData(),
                transacao.getCategoriaId(),
                transacao.getUsuarioId());
        historicoRepository.save(registro);
    }

    @Transactional(readOnly = true)
    public List<HistoricoResponse> listar(Long usuarioId, Long transacaoId) {
        return historicoRepository.listar(usuarioId, transacaoId).stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HistoricoResponse buscarPorId(Long id, Long usuarioId) {
        return historicoRepository.buscarPorIdDoUsuario(id, usuarioId)
                .map(this::paraResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Histórico com id " + id + " não encontrado"));
    }

    private HistoricoResponse paraResponse(HistoricoTransacao h) {
        return new HistoricoResponse(
                h.getId(), h.getTransacaoId(), h.getAcao(),
                h.getDescricaoAnterior(), h.getValorAnterior(),
                h.getDataAnterior(), h.getCategoriaAnteriorId(), h.getDataAcao());
    }
}