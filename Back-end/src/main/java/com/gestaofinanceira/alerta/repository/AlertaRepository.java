package com.gestaofinanceira.alerta.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestaofinanceira.alerta.entity.Alerta;
import com.gestaofinanceira.alerta.entity.TipoAlerta;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findAllByUsuarioIdOrderByDataCriacaoDescIdDesc(Long usuarioId);

    List<Alerta> findAllByUsuarioIdAndLidaFalseOrderByDataCriacaoDescIdDesc(Long usuarioId);

    Optional<Alerta> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByUsuarioIdAndTipoAndTituloAndDataCriacaoGreaterThanEqual(
            Long usuarioId, TipoAlerta tipo, String titulo, LocalDateTime desde);
}
