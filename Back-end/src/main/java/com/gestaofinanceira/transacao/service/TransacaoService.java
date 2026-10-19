package com.gestaofinanceira.transacao.service;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.categoria.dto.CategoriaResponse;
import com.gestaofinanceira.categoria.service.CategoriaService;
import com.gestaofinanceira.exception.RecursoNaoEncontradoException;
import com.gestaofinanceira.exception.RegraDeNegocioException;
import com.gestaofinanceira.transacao.dto.TransacaoRequest;
import com.gestaofinanceira.transacao.dto.TransacaoResponse;
import com.gestaofinanceira.transacao.entity.Recorrencia;
import com.gestaofinanceira.transacao.entity.TipoTransacao;
import com.gestaofinanceira.transacao.entity.Transacao;
import com.gestaofinanceira.transacao.repository.TransacaoRepository;
import com.gestaofinanceira.historico.entity.AcaoHistorico;
import com.gestaofinanceira.historico.service.HistoricoTransacaoService;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final CategoriaService categoriaService;
    private final HistoricoTransacaoService historicoService;

    public TransacaoService(TransacaoRepository transacaoRepository,
                            CategoriaService categoriaService,
                            HistoricoTransacaoService historicoService) {
        this.transacaoRepository = transacaoRepository;
        this.categoriaService = categoriaService;
        this.historicoService = historicoService;
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponse> listar(Long usuarioId, LocalDate dataInicial,
                                            LocalDate dataFinal, Long categoriaId,
                                            TipoTransacao tipo, Recorrencia recorrencia,
                                            String estabelecimento) {
        if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
            throw new RegraDeNegocioException(
                    "A data inicial não pode ser posterior à data final");
        }
        String padrao = (estabelecimento == null || estabelecimento.isBlank())
                ? null
                : "%" + estabelecimento.trim().toLowerCase() + "%";

        return transacaoRepository
                .filtrar(usuarioId, dataInicial, dataFinal, categoriaId,
                        tipo, recorrencia, padrao)
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransacaoResponse buscarPorId(Long id, Long usuarioId) {
        return paraResponse(buscarOuFalhar(id, usuarioId));
    }

    @Transactional
    public TransacaoResponse criar(TransacaoRequest request, Long usuarioId) {
        validarCategoria(request, usuarioId);
        Transacao transacao = new Transacao(
                request.descricao().trim(),
                request.valor(),
                request.data(),
                request.tipo(),
                limpar(request.estabelecimento()),
                recorrenciaOuPadrao(request),
                usuarioId,
                request.categoriaId());
        return paraResponse(transacaoRepository.save(transacao));
    }

    @Transactional
    public TransacaoResponse atualizar(Long id, TransacaoRequest request, Long usuarioId) {
        Transacao transacao = buscarOuFalhar(id, usuarioId);
        validarCategoria(request, usuarioId);
        historicoService.registrar(transacao, AcaoHistorico.EDICAO);
        transacao.atualizarDados(
                request.descricao().trim(),
                request.valor(),
                request.data(),
                request.tipo(),
                limpar(request.estabelecimento()),
                recorrenciaOuPadrao(request),
                request.categoriaId());
        return paraResponse(transacaoRepository.save(transacao));
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        Transacao transacao = buscarOuFalhar(id, usuarioId);
        historicoService.registrar(transacao, AcaoHistorico.EXCLUSAO);
        transacao.excluir();
        transacaoRepository.save(transacao);
    }

        @Transactional(readOnly = true)
    public boolean existeDuplicada(Long usuarioId, LocalDate data, BigDecimal valor,
                                    TipoTransacao tipo, String descricao) {
        return transacaoRepository.contarIguais(
                usuarioId, data, valor, tipo, descricao.trim().toLowerCase()) > 0;
    }

    @Transactional
    public void criarImportada(Long usuarioId, Long importacaoId, String descricao,
                                BigDecimal valor, LocalDate data, TipoTransacao tipo,
                                String estabelecimento) {
        Transacao transacao = new Transacao(descricao, valor, data, tipo,
                estabelecimento, Recorrencia.NENHUMA, usuarioId, null);
        transacao.vincularImportacao(importacaoId);
        transacaoRepository.save(transacao);
    }

    private Transacao buscarOuFalhar(Long id, Long usuarioId) {
        return transacaoRepository.buscarAtivaPorId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Transação com id " + id + " não encontrada"));
    }

    private void validarCategoria(TransacaoRequest request, Long usuarioId) {
        if (request.categoriaId() == null) {
            return;
        }
        // lança 404 se a categoria não existir ou não for do usuário
        CategoriaResponse categoria =
                categoriaService.buscarPorId(request.categoriaId(), usuarioId);
        if (!categoria.tipo().name().equals(request.tipo().name())) {
            throw new RegraDeNegocioException(
                    "A categoria informada não é compatível com o tipo da transação");
        }
    }

    private Recorrencia recorrenciaOuPadrao(TransacaoRequest request) {
        return request.recorrencia() != null ? request.recorrencia() : Recorrencia.NENHUMA;
    }

    private String limpar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private TransacaoResponse paraResponse(Transacao t) {
        return new TransacaoResponse(
                t.getId(), t.getDescricao(), t.getValor(), t.getData(),
                t.getTipo(), t.getEstabelecimento(), t.getRecorrencia(),
                t.getCategoriaId());
    }
}