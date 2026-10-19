package com.gestaofinanceira.alerta.repository;

import com.gestaofinanceira.alerta.entity.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByUsuarioIdOrderByDataCriacaoDesc(Long usuarioId);

    Optional<Alerta> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Alerta> findByUsuarioIdAndLidoFalseOrderByDataCriacaoDesc(Long usuarioId);
}