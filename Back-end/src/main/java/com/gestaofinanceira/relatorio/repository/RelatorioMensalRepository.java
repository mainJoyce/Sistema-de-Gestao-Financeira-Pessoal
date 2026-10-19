package com.gestaofinanceira.relatorio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestaofinanceira.relatorio.entity.RelatorioMensal;

public interface RelatorioMensalRepository extends JpaRepository<RelatorioMensal, Long> {

    @Query("""
            SELECT r FROM RelatorioMensal r
            WHERE r.usuarioId = :usuarioId
            ORDER BY r.mesReferencia DESC, r.dataGeracao DESC, r.id DESC
            """)
    List<RelatorioMensal> listar(@Param("usuarioId") Long usuarioId);

    @Query("""
            SELECT r FROM RelatorioMensal r
            WHERE r.id = :id AND r.usuarioId = :usuarioId
            """)
    Optional<RelatorioMensal> buscarPorIdDoUsuario(@Param("id") Long id,
                                                    @Param("usuarioId") Long usuarioId);
}