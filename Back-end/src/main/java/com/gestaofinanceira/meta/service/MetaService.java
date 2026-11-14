package com.gestaofinanceira.meta.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.exception.RecursoNaoEncontradoException;
import com.gestaofinanceira.exception.RegraDeNegocioException;
import com.gestaofinanceira.meta.dto.AporteRequest;
import com.gestaofinanceira.meta.dto.MetaRequest;
import com.gestaofinanceira.meta.dto.MetaResponse;
import com.gestaofinanceira.meta.entity.Meta;
import com.gestaofinanceira.meta.repository.MetaRepository;

@Service
public class MetaService {

    private static final BigDecimal CEM = new BigDecimal("100");
    private static final BigDecimal LIMITE_ACUMULADO = new BigDecimal("99999999.99");

    private final MetaRepository metaRepository;

    public MetaService(MetaRepository metaRepository) {
        this.metaRepository = metaRepository;
    }

    @Transactional(readOnly = true)
    public List<MetaResponse> listar(Long usuarioId) {
        return metaRepository.findAllByUsuarioIdOrderByDataDesejadaAscIdAsc(usuarioId)
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MetaResponse buscarPorId(Long id, Long usuarioId) {
        return paraResponse(buscarOuFalhar(id, usuarioId));
    }

    @Transactional
    public MetaResponse criar(MetaRequest request, Long usuarioId) {
        Meta meta = new Meta(request.nome().trim(), request.valorObjetivo(),
                request.dataDesejada(), usuarioId);
        return paraResponse(metaRepository.save(meta));
    }

    @Transactional
    public MetaResponse atualizar(Long id, MetaRequest request, Long usuarioId) {
        Meta meta = buscarOuFalhar(id, usuarioId);
        meta.atualizarDados(request.nome().trim(), request.valorObjetivo(),
                request.dataDesejada());
        return paraResponse(metaRepository.save(meta));
    }

    /** Soma um valor ao acumulado da meta (US20: acompanhar o progresso). */
    @Transactional
    public MetaResponse registrarAporte(Long id, AporteRequest request, Long usuarioId) {
        Meta meta = buscarOuFalhar(id, usuarioId);
        if (meta.getValorAcumulado().add(request.valor()).compareTo(LIMITE_ACUMULADO) > 0) {
            throw new RegraDeNegocioException("O valor acumulado excede o limite permitido");
        }
        meta.registrarAporte(request.valor());
        return paraResponse(metaRepository.save(meta));
    }

    /**
     * Exclusao fisica: a tabela meta_financeira nao tem coluna de status e nenhuma
     * outra tabela depende dela.
     */
    @Transactional
    public void excluir(Long id, Long usuarioId) {
        metaRepository.delete(buscarOuFalhar(id, usuarioId));
    }

    private Meta buscarOuFalhar(Long id, Long usuarioId) {
        return metaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Meta com id " + id + " não encontrada"));
    }

    private MetaResponse paraResponse(Meta meta) {
        BigDecimal objetivo = meta.getValorObjetivo();
        BigDecimal acumulado = meta.getValorAcumulado();

        BigDecimal restante = objetivo.subtract(acumulado).max(BigDecimal.ZERO);
        BigDecimal percentual = acumulado.multiply(CEM)
                .divide(objetivo, 2, RoundingMode.HALF_UP);
        boolean concluida = acumulado.compareTo(objetivo) >= 0;

        long meses = Math.max(0,
                ChronoUnit.MONTHS.between(LocalDate.now().withDayOfMonth(1),
                        meta.getDataDesejada().withDayOfMonth(1)));

        // quanto guardar por mes ate a data desejada (se o prazo for neste mes, tudo agora)
        BigDecimal mensal = concluida
                ? BigDecimal.ZERO.setScale(2)
                : restante.divide(BigDecimal.valueOf(Math.max(meses, 1)), 2, RoundingMode.CEILING);

        return new MetaResponse(
                meta.getId(), meta.getNome(), objetivo, acumulado,
                meta.getDataDesejada(), restante, percentual, meses, mensal, concluida);
    }
}
