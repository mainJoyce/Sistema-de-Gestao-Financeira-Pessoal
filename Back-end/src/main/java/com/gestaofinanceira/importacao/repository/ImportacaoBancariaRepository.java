package com.gestaofinanceira.importacao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestaofinanceira.importacao.entity.ImportacaoBancaria;

public interface ImportacaoBancariaRepository extends JpaRepository<ImportacaoBancaria, Long> {

    @Query("""
            SELECT i FROM ImportacaoBancaria i
            WHERE i.usuarioId = :usuarioId
            ORDER BY i.dataCriacao DESC, i.id DESC
            """)
    List<ImportacaoBancaria> listar(@Param("usuarioId") Long usuarioId);

    @Query("""
            SELECT i FROM ImportacaoBancaria i
            WHERE i.id = :id AND i.usuarioId = :usuarioId
            """)
    Optional<ImportacaoBancaria> buscarPorIdDoUsuario(@Param("id") Long id,
                                                    @Param("usuarioId") Long usuarioId);
}