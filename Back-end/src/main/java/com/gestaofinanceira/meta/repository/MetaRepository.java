package com.gestaofinanceira.meta.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestaofinanceira.meta.entity.Meta;

public interface MetaRepository extends JpaRepository<Meta, Long> {

    List<Meta> findByUsuarioId(Long usuarioId);

}