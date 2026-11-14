package com.gestaofinanceira.transacao.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.categoria.dto.CategoriaResponse;
import com.gestaofinanceira.categoria.service.CategoriaService;
import com.gestaofinanceira.exception.RecursoNaoEncontradoException;
import com.gestaofinanceira.exception.RegraDeNegocioException;
import com.gestaofinanceira.historico.entity.AcaoHistorico;
import com.gestaofinanceira.historico.service.HistoricoTransacaoService;
import com.gestaofinanceira.renda.service.RendaService;
import com.gestaofinanceira.transacao.dto.SugestaoCategoriaResponse;
import com.gestaofinanceira.transacao.dto.TransacaoRequest;
import com.gestaofinanceira.transacao.dto.TransacaoResponse;
import com.gestaofinanceira.transacao.entity.Recorrencia;
import com.gestaofinanceira.transacao.entity.TipoTransacao;
import com.gestaofinanceira.transacao.entity.Transacao;
import com.gestaofinanceira.transacao.event.TransacaoAlteradaEvent;
import com.gestaofinanceira.transacao.repository.TransacaoRepository;

@Service
public class TransacaoService {

    private static final int TAMANHO_MAXIMO_PAGINA = 500;

    private final TransacaoRepository transacaoRepository;
    private final CategoriaService categoriaService;
    private final HistoricoTransacaoService historicoService;
    private final RendaService rendaService;
    private final SugestaoCategoriaService sugestaoCategoriaService;
    private final ApplicationEventPublisher eventos;

    public TransacaoService(TransacaoRepository transacaoRepository,
                            CategoriaService categoriaService,
                            HistoricoTransacaoService historicoService,
                            RendaService rendaService,
                            SugestaoCategoriaService sugestaoCategoriaService,
                            ApplicationEventPublisher eventos) {
        this.transacaoRepository = transacaoRepository;
        this.categoriaService = categoriaService;
        this.historicoService = historicoService;
        this.rendaService = rendaService;
        this.sugestaoCategoriaService = sugestaoCategoriaService;
        this.eventos = eventos;
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponse> listar(Long usuarioId, LocalDate dataInicial,
                                          LocalDate dataFinal, Long categoriaId,
                                          TipoTransacao tipo, Recorrencia recorrencia,
                                          String estabelecimento, int pagina, int tamanho) {
        if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
            throw new RegraDeNegocioException(
                    "A data inicial não pode ser posterior à data final");
        }
        if (pagina < 0 || tamanho < 1 || tamanho > TAMANHO_MAXIMO_PAGINA) {
            throw new RegraDeNegocioException(
                    "Paginação inválida: pagina >= 0 e tamanho entre 1 e " + TAMANHO_MAXIMO_PAGINA);
        }
        String padrao = (estabelecimento == null || estabelecimento.isBlank())
                ? null
                : "%" + estabelecimento.trim().toLowerCase() + "%";

        return transacaoRepository
                .filtrar(usuarioId, dataInicial, dataFinal, categoriaId,
                        tipo, recorrencia, padrao, PageRequest.of(pagina, tamanho))
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransacaoResponse buscarPorId(Long id, Long usuarioId) {
        return paraResponse(buscarOuFalhar(id, usuarioId));
    }

    @Transactional(readOnly = true)
    public Optional<SugestaoCategoriaResponse> sugerirCategoria(
            Long usuarioId, TipoTransacao tipo, String estabelecimento, String descricao) {
        return sugestaoCategoriaService.sugerir(usuarioId, tipo, estabelecimento, descricao);
    }

    @Transactional
    public TransacaoResponse criar(TransacaoRequest request, Long usuarioId) {
        String estabelecimento = limpar(request.estabelecimento());
        Long categoriaId = resolverCategoria(request.categoriaId(), request.tipo(),
                estabelecimento, request.descricao(), usuarioId);
        validarRenda(request, usuarioId);

        Transacao transacao = new Transacao(
                request.descricao().trim(),
                request.valor(),
                request.data(),
                request.tipo(),
                estabelecimento,
                recorrenciaOuPadrao(request),
                usuarioId,
                categoriaId);
        transacao.vincularRenda(request.rendaId());

        TransacaoResponse resposta = paraResponse(transacaoRepository.save(transacao));
        eventos.publishEvent(new TransacaoAlteradaEvent(usuarioId));
        return resposta;
    }

    @Transactional
    public TransacaoResponse atualizar(Long id, TransacaoRequest request, Long usuarioId) {
        Transacao transacao = buscarOuFalhar(id, usuarioId);
        String estabelecimento = limpar(request.estabelecimento());
        Long categoriaId = resolverCategoria(request.categoriaId(), request.tipo(),
                estabelecimento, request.descricao(), usuarioId);
        validarRenda(request, usuarioId);

        historicoService.registrar(transacao, AcaoHistorico.EDICAO);
        transacao.atualizarDados(
                request.descricao().trim(),
                request.valor(),
                request.data(),
                request.tipo(),
                estabelecimento,
                recorrenciaOuPadrao(request),
                categoriaId);
        transacao.vincularRenda(request.rendaId());

        TransacaoResponse resposta = paraResponse(transacaoRepository.save(transacao));
        eventos.publishEvent(new TransacaoAlteradaEvent(usuarioId));
        return resposta;
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        Transacao transacao = buscarOuFalhar(id, usuarioId);
        historicoService.registrar(transacao, AcaoHistorico.EXCLUSAO);
        transacao.excluir();
        transacaoRepository.save(transacao);
        eventos.publishEvent(new TransacaoAlteradaEvent(usuarioId));
    }

    @Transactional(readOnly = true)
    public boolean existeDuplicada(Long usuarioId, LocalDate data, BigDecimal valor,
                                   TipoTransacao tipo, String descricao) {
        return transacaoRepository.contarIguais(
                usuarioId, data, valor, tipo, descricao.trim().toLowerCase()) > 0;
    }

    /** Usada pela importacao. Despesas importadas tambem ganham categoria automatica. */
    @Transactional
    public void criarImportada(Long usuarioId, Long importacaoId, String descricao,
                               BigDecimal valor, LocalDate data, TipoTransacao tipo,
                               String estabelecimento) {
        Long categoriaId = sugestaoCategoriaService
                .sugerir(usuarioId, tipo, estabelecimento, descricao)
                .map(SugestaoCategoriaResponse::categoriaId)
                .orElse(null);

        Transacao transacao = new Transacao(descricao, valor, data, tipo,
                estabelecimento, Recorrencia.NENHUMA, usuarioId, categoriaId);
        transacao.vincularImportacao(importacaoId);
        transacaoRepository.save(transacao);
    }

    @Transactional(readOnly = true)
    public BigDecimal somarDespesasDaCategoria(Long usuarioId, Long categoriaId,
                                               LocalDate inicio, LocalDate fim) {
        return transacaoRepository.somarDespesasPorCategoriaEPeriodo(
                usuarioId, categoriaId, inicio, fim);
    }

    private Transacao buscarOuFalhar(Long id, Long usuarioId) {
        return transacaoRepository.buscarAtivaPorId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Transação com id " + id + " não encontrada"));
    }

    /**
     * Categoria informada: precisa ser do usuario (ou padrao) e do mesmo tipo.
     * Categoria ausente em DESPESA: o sistema sugere (historico, palavra-chave ou "Outros"),
     * porque "toda despesa deverá possuir uma categoria" (documento, secao 3.2).
     * Receita pode ficar sem categoria.
     */
    private Long resolverCategoria(Long categoriaId, TipoTransacao tipo,
                                   String estabelecimento, String descricao, Long usuarioId) {
        if (categoriaId != null) {
            // lanca 404 se a categoria nao existir ou nao for do usuario
            CategoriaResponse categoria = categoriaService.buscarPorId(categoriaId, usuarioId);
            if (!categoria.tipo().name().equals(tipo.name())) {
                throw new RegraDeNegocioException(
                        "A categoria informada não é compatível com o tipo da transação");
            }
            return categoriaId;
        }
        if (tipo == TipoTransacao.DESPESA) {
            return sugestaoCategoriaService
                    .sugerir(usuarioId, tipo, estabelecimento, descricao)
                    .map(SugestaoCategoriaResponse::categoriaId)
                    .orElse(null);
        }
        return null;
    }

    private void validarRenda(TransacaoRequest request, Long usuarioId) {
        if (request.rendaId() == null) {
            return;
        }
        if (request.tipo() != TipoTransacao.RECEITA) {
            throw new RegraDeNegocioException(
                    "Só receitas podem ser vinculadas a uma fonte de renda");
        }
        // lanca 404 se a renda nao existir ou nao for do usuario
        rendaService.buscarPorId(request.rendaId(), usuarioId);
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
                t.getCategoriaId(), t.getRendaUsuarioId());
    }
}
