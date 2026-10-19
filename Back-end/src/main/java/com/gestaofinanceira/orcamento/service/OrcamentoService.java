package com.gestaofinanceira.orcamento.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.orcamento.dto.OrcamentoRequest;
import com.gestaofinanceira.orcamento.dto.OrcamentoResponse;
import com.gestaofinanceira.orcamento.entity.Orcamento;
import com.gestaofinanceira.orcamento.repository.OrcamentoRepository;
import com.gestaofinanceira.transacao.entity.TipoTransacao;
import com.gestaofinanceira.transacao.repository.TransacaoRepository;
import com.gestaofinanceira.exception.RecursoNaoEncontradoException;
import com.gestaofinanceira.exception.RegraDeNegocioException;

@Service
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final TransacaoRepository transacaoRepository;

    public OrcamentoService(
            OrcamentoRepository orcamentoRepository,
            TransacaoRepository transacaoRepository) {
        this.orcamentoRepository = orcamentoRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listar(Long usuarioId) {
        return orcamentoRepository
                .findAllByUsuarioIdAndExcluidaFalse(usuarioId)
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrcamentoResponse buscarPorId(Long id, Long usuarioId) {
        return paraResponse(buscarOuFalhar(id, usuarioId));
    }

    @Transactional
    public OrcamentoResponse criar(
            OrcamentoRequest request, Long usuarioId) {

        validarDuplicidade(request, usuarioId, null);

        Orcamento orcamento = new Orcamento(
                usuarioId,
                request.categoriaId(),
                request.valorLimite(),
                request.mes(),
                request.ano());

        return paraResponse(orcamentoRepository.save(orcamento));
    }

    @Transactional
    public OrcamentoResponse atualizar(
            Long id, OrcamentoRequest request, Long usuarioId) {

        Orcamento orcamento = buscarOuFalhar(id, usuarioId);

        validarDuplicidade(request, usuarioId, id);

        orcamento.atualizar(
                request.valorLimite(),
                request.mes(),
                request.ano());

        return paraResponse(orcamentoRepository.save(orcamento));
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        Orcamento orcamento = buscarOuFalhar(id, usuarioId);
        orcamento.excluirLogicamente();
        orcamentoRepository.save(orcamento);
    }

    private void validarDuplicidade(
            OrcamentoRequest request, Long usuarioId, Long idAtual) {

        boolean existe = orcamentoRepository
                .existsByUsuarioIdAndCategoriaIdAndMesAndAnoAndExcluidaFalse(
                        usuarioId,
                        request.categoriaId(),
                        request.mes(),
                        request.ano());

        if (existe) {
            boolean mesmaConfiguracao = idAtual != null
                    && orcamentoRepository
                        .findByIdAndUsuarioIdAndExcluidaFalse(
                                idAtual, usuarioId)
                        .map(o -> o.getCategoriaId()
                                        .equals(request.categoriaId())
                                && o.getMes().equals(request.mes())
                                && o.getAno().equals(request.ano()))
                        .orElse(false);

            if (!mesmaConfiguracao) {
                throw new RegraDeNegocioException(
                        "Já existe orçamento para essa categoria nesse mês");
            }
        }
    }

    private Orcamento buscarOuFalhar(Long id, Long usuarioId) {
        return orcamentoRepository
                .findByIdAndUsuarioIdAndExcluidaFalse(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Orçamento não encontrado"));
    }

    private OrcamentoResponse paraResponse(Orcamento orcamento) {
        LocalDate inicio = LocalDate.of(
                orcamento.getAno(), orcamento.getMes(), 1);

        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        BigDecimal gasto = transacaoRepository.filtrar(
                        orcamento.getUsuarioId(),
                        inicio,
                        fim,
                        orcamento.getCategoriaId(),
                        TipoTransacao.DESPESA,
                        null,
                        null)
                .stream()
                .map(t -> t.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal limite = orcamento.getValorLimite();
        BigDecimal saldo = limite.subtract(gasto);

        BigDecimal percentual = gasto
                .multiply(new BigDecimal("100"))
                .divide(limite, 2, RoundingMode.HALF_UP);

        String classificacao;

        if (percentual.compareTo(new BigDecimal("100")) > 0) {
            classificacao = "ESTOURADO";
        } else if (percentual.compareTo(new BigDecimal("90")) >= 0) {
            classificacao = "RISCO";
        } else if (percentual.compareTo(new BigDecimal("70")) > 0) {
            classificacao = "ATENCAO";
        } else {
            classificacao = "NORMAL";
        }

        return new OrcamentoResponse(
                orcamento.getId(),
                orcamento.getCategoriaId(),
                limite,
                gasto,
                saldo,
                percentual,
                classificacao,
                orcamento.getMes(),
                orcamento.getAno());
    }
}