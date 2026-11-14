package com.gestaofinanceira.score.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestaofinanceira.score.entity.ScoreFinanceiro;

public interface ScoreFinanceiroRepository extends JpaRepository<ScoreFinanceiro, Long> {

    Optional<ScoreFinanceiro> findFirstByUsuarioIdOrderByDataCalculoDescIdDesc(Long usuarioId);

    List<ScoreFinanceiro> findAllByUsuarioIdOrderByDataCalculoDescIdDesc(Long usuarioId);
}
