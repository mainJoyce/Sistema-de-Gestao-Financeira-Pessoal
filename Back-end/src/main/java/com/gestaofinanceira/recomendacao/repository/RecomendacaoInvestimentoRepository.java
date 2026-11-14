package com.gestaofinanceira.recomendacao.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestaofinanceira.recomendacao.entity.RecomendacaoInvestimento;

public interface RecomendacaoInvestimentoRepository
        extends JpaRepository<RecomendacaoInvestimento, Long> {

    List<RecomendacaoInvestimento> findAllByUsuarioIdOrderByDataCriacaoDesc(Long usuarioId);

    List<RecomendacaoInvestimento> findAllByUsuarioIdAndTipoOrderByDataCriacaoDesc(
            Long usuarioId, String tipo);

    boolean existsByUsuarioIdAndTituloAndDataCriacaoGreaterThanEqual(
            Long usuarioId, String titulo, LocalDateTime desde);
}
