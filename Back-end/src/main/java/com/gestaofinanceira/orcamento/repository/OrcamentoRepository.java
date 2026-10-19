package com.gestaofinanceira.orcamento.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestaofinanceira.orcamento.entity.Orcamento;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    List<Orcamento> findAllByUsuarioIdAndExcluidaFalse(Long usuarioId);

    Optional<Orcamento> findByIdAndUsuarioIdAndExcluidaFalse(Long id, Long usuarioId);

    boolean existsByUsuarioIdAndCategoriaIdAndMesAndAnoAndExcluidaFalse(
            Long usuarioId, Long categoriaId, Integer mes, Integer ano);
}