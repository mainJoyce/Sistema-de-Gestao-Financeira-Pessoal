package com.gestaofinanceira.orcamento.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.categoria.dto.CategoriaResponse;
import com.gestaofinanceira.categoria.entity.TipoCategoria;
import com.gestaofinanceira.categoria.service.CategoriaService;
import com.gestaofinanceira.exception.RecursoNaoEncontradoException;
import com.gestaofinanceira.exception.RegraDeNegocioException;
import com.gestaofinanceira.orcamento.dto.OrcamentoRequest;
import com.gestaofinanceira.orcamento.dto.OrcamentoResponse;
import com.gestaofinanceira.orcamento.entity.Orcamento;
import com.gestaofinanceira.orcamento.repository.OrcamentoRepository;
import com.gestaofinanceira.transacao.service.TransacaoService;

@Service
public class OrcamentoService {

    private static final BigDecimal CEM = new BigDecimal("100");

    private final OrcamentoRepository orcamentoRepository;
    private final CategoriaService categoriaService;
    private final TransacaoService transacaoService;

    public OrcamentoService(OrcamentoRepository orcamentoRepository,
                            CategoriaService categoriaService,
                            TransacaoService transacaoService) {
        this.orcamentoRepository = orcamentoRepository;
        this.categoriaService = categoriaService;
        this.transacaoService = transacaoService;
    }

    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listar(Long usuarioId) {
        return orcamentoRepository
                .findAllByUsuarioIdOrderByMesReferenciaDescIdDesc(usuarioId)
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    /** Orcamentos de um mes (usado pelo Score e pelos Alertas). */
    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listarDoMes(Long usuarioId, YearMonth mes) {
        return orcamentoRepository
                .findAllByUsuarioIdAndMesReferencia(usuarioId, mes.atDay(1))
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrcamentoResponse buscarPorId(Long id, Long usuarioId) {
        return paraResponse(buscarOuFalhar(id, usuarioId));
    }

    @Transactional
    public OrcamentoResponse criar(OrcamentoRequest request, Long usuarioId) {
        validarCategoria(request.categoriaId(), usuarioId);
        LocalDate mesReferencia = LocalDate.of(request.ano(), request.mes(), 1);

        if (orcamentoRepository.existsByUsuarioIdAndCategoriaIdAndMesReferencia(
                usuarioId, request.categoriaId(), mesReferencia)) {
            throw new RegraDeNegocioException(
                    "Já existe orçamento para essa categoria nesse mês");
        }

        Orcamento orcamento = new Orcamento(
                usuarioId, request.categoriaId(), request.valorLimite(), mesReferencia);
        return paraResponse(orcamentoRepository.save(orcamento));
    }

    @Transactional
    public OrcamentoResponse atualizar(Long id, OrcamentoRequest request, Long usuarioId) {
        Orcamento orcamento = buscarOuFalhar(id, usuarioId);
        LocalDate mesReferencia = LocalDate.of(request.ano(), request.mes(), 1);

        // a categoria de um orcamento nao muda: para outra categoria, crie outro orcamento
        if (!orcamento.getCategoriaId().equals(request.categoriaId())) {
            throw new RegraDeNegocioException(
                    "A categoria de um orçamento não pode ser alterada");
        }

        boolean mudouMes = !orcamento.getMesReferencia().equals(mesReferencia);
        if (mudouMes && orcamentoRepository.existsByUsuarioIdAndCategoriaIdAndMesReferencia(
                usuarioId, orcamento.getCategoriaId(), mesReferencia)) {
            throw new RegraDeNegocioException(
                    "Já existe orçamento para essa categoria nesse mês");
        }

        orcamento.atualizar(request.valorLimite(), mesReferencia);
        return paraResponse(orcamentoRepository.save(orcamento));
    }

    /**
     * Exclusao fisica: a tabela orcamento nao tem coluna de status e nenhuma outra
     * tabela depende dela, entao nao ha historico a preservar.
     */
    @Transactional
    public void excluir(Long id, Long usuarioId) {
        orcamentoRepository.delete(buscarOuFalhar(id, usuarioId));
    }

    private void validarCategoria(Long categoriaId, Long usuarioId) {
        // lanca 404 se a categoria nao existir ou nao for do usuario
        CategoriaResponse categoria = categoriaService.buscarPorId(categoriaId, usuarioId);
        if (categoria.tipo() != TipoCategoria.DESPESA) {
            throw new RegraDeNegocioException(
                    "Só é possível criar orçamento para categorias de despesa");
        }
    }

    private Orcamento buscarOuFalhar(Long id, Long usuarioId) {
        return orcamentoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Orçamento com id " + id + " não encontrado"));
    }

    private OrcamentoResponse paraResponse(Orcamento orcamento) {
        LocalDate inicio = orcamento.getMesReferencia();
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        BigDecimal gasto = transacaoService.somarDespesasDaCategoria(
                orcamento.getUsuarioId(), orcamento.getCategoriaId(), inicio, fim);

        BigDecimal limite = orcamento.getValorLimite();
        BigDecimal percentual = gasto.multiply(CEM)
                .divide(limite, 2, RoundingMode.HALF_UP);

        return new OrcamentoResponse(
                orcamento.getId(),
                orcamento.getCategoriaId(),
                limite,
                gasto,
                limite.subtract(gasto),
                percentual,
                FaixaOrcamento.classificar(percentual),
                orcamento.getMes(),
                orcamento.getAno());
    }

}
