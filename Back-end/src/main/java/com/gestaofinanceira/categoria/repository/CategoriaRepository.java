package com.gestaofinanceira.categoria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestaofinanceira.categoria.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

        @Query("""
                SELECT c FROM Categoria c
                WHERE c.ativo = true
                AND (c.usuarioId = :usuarioId OR c.usuarioId IS NULL)
                ORDER BY c.nome
                """)
        List<Categoria> listarDisponiveis(@Param("usuarioId") Long usuarioId);

        @Query("""
                SELECT c FROM Categoria c
                WHERE c.id = :id
                AND c.ativo = true
                AND (c.usuarioId = :usuarioId OR c.usuarioId IS NULL)
                """)
        Optional<Categoria> buscarDisponivelPorId(@Param("id") Long id,
        @Param("usuarioId") Long usuarioId);
}