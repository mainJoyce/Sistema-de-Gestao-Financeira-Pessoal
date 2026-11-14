package com.gestaofinanceira.meta.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestaofinanceira.meta.entity.Meta;

public interface MetaRepository extends JpaRepository<Meta, Long> {

    List<Meta> findAllByUsuarioIdOrderByDataDesejadaAscIdAsc(Long usuarioId);

    Optional<Meta> findByIdAndUsuarioId(Long id, Long usuarioId);
}
