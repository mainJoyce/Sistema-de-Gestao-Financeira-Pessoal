package com.gestaofinanceira.insight.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestaofinanceira.insight.entity.Insight;

public interface InsightRepository extends JpaRepository<Insight, Long> {

    List<Insight> findAllByUsuarioIdOrderByDataCriacaoDesc(Long usuarioId);

    List<Insight> findAllByUsuarioIdAndTipoOrderByDataCriacaoDesc(
            Long usuarioId, String tipo);

    boolean existsByUsuarioIdAndTipoAndDadosReferencia(
            Long usuarioId, String tipo, String dadosReferencia);
}