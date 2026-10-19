package com.gestaofinanceira.historico.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestaofinanceira.historico.entity.HistoricoTransacao;

public interface HistoricoTransacaoRepository extends JpaRepository<HistoricoTransacao, Long> {

    @Query("""
            SELECT h FROM HistoricoTransacao h
            WHERE h.usuarioId = :usuarioId
                AND (:transacaoId IS NULL OR h.transacaoId = :transacaoId)
            ORDER BY h.dataAcao DESC, h.id DESC
            """)
    List<HistoricoTransacao> listar(@Param("usuarioId") Long usuarioId,
                                    @Param("transacaoId") Long transacaoId);

    @Query("""
            SELECT h FROM HistoricoTransacao h
            WHERE h.id = :id AND h.usuarioId = :usuarioId
            """)
    Optional<HistoricoTransacao> buscarPorIdDoUsuario(@Param("id") Long id,
                                                    @Param("usuarioId") Long usuarioId);
}