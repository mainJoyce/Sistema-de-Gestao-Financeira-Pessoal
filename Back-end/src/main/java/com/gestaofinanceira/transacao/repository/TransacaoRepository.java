package com.gestaofinanceira.transacao.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestaofinanceira.transacao.entity.Recorrencia;
import com.gestaofinanceira.transacao.entity.TipoTransacao;
import com.gestaofinanceira.transacao.entity.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    @Query("""
            SELECT t FROM Transacao t
            WHERE t.usuarioId = :usuarioId
                AND t.status = com.gestaofinanceira.transacao.entity.StatusTransacao.ATIVA
                AND (:dataInicial IS NULL OR t.data >= :dataInicial)
                AND (:dataFinal IS NULL OR t.data <= :dataFinal)
                AND (:categoriaId IS NULL OR t.categoriaId = :categoriaId)
                AND (:tipo IS NULL OR t.tipo = :tipo)
                AND (:recorrencia IS NULL OR t.recorrencia = :recorrencia)
                AND (:estabelecimento IS NULL OR LOWER(t.estabelecimento) LIKE :estabelecimento)
            ORDER BY t.data DESC, t.id DESC
            """)
    List<Transacao> filtrar(@Param("usuarioId") Long usuarioId,
                            @Param("dataInicial") LocalDate dataInicial,
                            @Param("dataFinal") LocalDate dataFinal,
                            @Param("categoriaId") Long categoriaId,
                            @Param("tipo") TipoTransacao tipo,
                            @Param("recorrencia") Recorrencia recorrencia,
                            @Param("estabelecimento") String estabelecimento);

    @Query("""
            SELECT t FROM Transacao t
            WHERE t.id = :id
                AND t.usuarioId = :usuarioId
                AND t.status = com.gestaofinanceira.transacao.entity.StatusTransacao.ATIVA
            """)
    Optional<Transacao> buscarAtivaPorId(@Param("id") Long id,
                                        @Param("usuarioId") Long usuarioId);

            @Query("""
            SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t
            WHERE t.usuarioId = :usuarioId
                AND t.status = com.gestaofinanceira.transacao.entity.StatusTransacao.ATIVA
                AND t.tipo = :tipo
                AND t.data >= :inicio AND t.data <= :fim
            """)
    BigDecimal somarPorTipo(@Param("usuarioId") Long usuarioId,
                            @Param("tipo") TipoTransacao tipo,
                            @Param("inicio") LocalDate inicio,
                            @Param("fim") LocalDate fim);

    @Query("""
            SELECT t.categoriaId, SUM(t.valor) FROM Transacao t
            WHERE t.usuarioId = :usuarioId
                AND t.status = com.gestaofinanceira.transacao.entity.StatusTransacao.ATIVA
                AND t.tipo = com.gestaofinanceira.transacao.entity.TipoTransacao.DESPESA
                AND t.data >= :inicio AND t.data <= :fim
            GROUP BY t.categoriaId
            ORDER BY SUM(t.valor) DESC
            """)
    List<Object[]> somarDespesasPorCategoria(@Param("usuarioId") Long usuarioId,
                                            @Param("inicio") LocalDate inicio,
                                            @Param("fim") LocalDate fim);

                                                @Query("""
            SELECT COUNT(t) FROM Transacao t
            WHERE t.usuarioId = :usuarioId
                AND t.status = com.gestaofinanceira.transacao.entity.StatusTransacao.ATIVA
                AND t.data = :data
                AND t.valor = :valor
                AND t.tipo = :tipo
                AND LOWER(t.descricao) = :descricao
            """)
    long contarIguais(@Param("usuarioId") Long usuarioId,
                        @Param("data") LocalDate data,
                        @Param("valor") BigDecimal valor,
                        @Param("tipo") TipoTransacao tipo,
                        @Param("descricao") String descricao);

                        
                        
    @Query("""
        SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t
        WHERE t.usuarioId = :usuarioId
            AND t.categoriaId = :categoriaId
            AND t.status = com.gestaofinanceira.transacao.entity.StatusTransacao.ATIVA
            AND t.tipo = com.gestaofinanceira.transacao.entity.TipoTransacao.DESPESA
            AND t.data >= :inicio AND t.data <= :fim
        """)
BigDecimal somarDespesasPorCategoriaEPeriodo(
        @Param("usuarioId") Long usuarioId,
        @Param("categoriaId") Long categoriaId,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim);

}