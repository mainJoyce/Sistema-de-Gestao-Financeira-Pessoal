package com.gestaofinanceira.renda.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestaofinanceira.renda.entity.Renda;

public interface RendaRepository extends JpaRepository<Renda, Long> {

    List<Renda> findAllByUsuarioIdAndAtivoTrueOrderByNome(Long usuarioId);

    Optional<Renda> findByIdAndUsuarioIdAndAtivoTrue(Long id, Long usuarioId);
}
