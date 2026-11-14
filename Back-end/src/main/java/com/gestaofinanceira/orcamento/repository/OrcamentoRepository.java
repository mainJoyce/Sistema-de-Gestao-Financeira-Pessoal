package com.gestaofinanceira.orcamento.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestaofinanceira.orcamento.entity.Orcamento;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    List<Orcamento> findAllByUsuarioIdOrderByMesReferenciaDescIdDesc(Long usuarioId);

    List<Orcamento> findAllByUsuarioIdAndMesReferencia(Long usuarioId, LocalDate mesReferencia);

    Optional<Orcamento> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByUsuarioIdAndCategoriaIdAndMesReferencia(
            Long usuarioId, Long categoriaId, LocalDate mesReferencia);
}
