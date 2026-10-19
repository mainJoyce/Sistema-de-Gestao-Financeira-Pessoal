package com.gestaofinanceira.historico.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "historico_transacao")
public class HistoricoTransacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transacao_id", nullable = false, updatable = false)
    private Long transacaoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, updatable = false)
    private AcaoHistorico acao;

    @Column(name = "descricao_anterior", length = 255, updatable = false)
    private String descricaoAnterior;

    @Column(name = "valor_anterior", precision = 10, scale = 2, updatable = false)
    private BigDecimal valorAnterior;

    @Column(name = "data_anterior", updatable = false)
    private LocalDate dataAnterior;

    @Column(name = "categoria_anterior_id", updatable = false)
    private Long categoriaAnteriorId;

    @Column(name = "data_acao", nullable = false, updatable = false)
    private LocalDateTime dataAcao;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    protected HistoricoTransacao() {
    }

    public HistoricoTransacao(Long transacaoId, AcaoHistorico acao,
                                String descricaoAnterior, BigDecimal valorAnterior,
                                LocalDate dataAnterior, Long categoriaAnteriorId,
                                Long usuarioId) {
        this.transacaoId = transacaoId;
        this.acao = acao;
        this.descricaoAnterior = descricaoAnterior;
        this.valorAnterior = valorAnterior;
        this.dataAnterior = dataAnterior;
        this.categoriaAnteriorId = categoriaAnteriorId;
        this.usuarioId = usuarioId;
    }

    @PrePersist
    void aoCriar() {
        this.dataAcao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getTransacaoId() { return transacaoId; }
    public AcaoHistorico getAcao() { return acao; }
    public String getDescricaoAnterior() { return descricaoAnterior; }
    public BigDecimal getValorAnterior() { return valorAnterior; }
    public LocalDate getDataAnterior() { return dataAnterior; }
    public Long getCategoriaAnteriorId() { return categoriaAnteriorId; }
    public LocalDateTime getDataAcao() { return dataAcao; }
    public Long getUsuarioId() { return usuarioId; }
}