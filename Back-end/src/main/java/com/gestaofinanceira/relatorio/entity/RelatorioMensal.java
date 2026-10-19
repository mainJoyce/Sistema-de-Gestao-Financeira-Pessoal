package com.gestaofinanceira.relatorio.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "relatorio_mensal")
public class RelatorioMensal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mes_referencia", nullable = false, updatable = false)
    private LocalDate mesReferencia;

    @Column(name = "total_recebido", nullable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal totalRecebido;

    @Column(name = "total_gasto", nullable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal totalGasto;

    @Column(nullable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal saldo;

    @Column(name = "variacao_mes_anterior", updatable = false, precision = 10, scale = 2)
    private BigDecimal variacaoMesAnterior;

    @Column(name = "percentual_variacao", updatable = false, precision = 5, scale = 2)
    private BigDecimal percentualVariacao;

    @Column(name = "data_geracao", nullable = false, updatable = false)
    private LocalDateTime dataGeracao;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    protected RelatorioMensal() {
    }

    public RelatorioMensal(LocalDate mesReferencia, BigDecimal totalRecebido,
                            BigDecimal totalGasto, BigDecimal saldo,
                            BigDecimal variacaoMesAnterior, BigDecimal percentualVariacao,
                            Long usuarioId) {
        this.mesReferencia = mesReferencia;
        this.totalRecebido = totalRecebido;
        this.totalGasto = totalGasto;
        this.saldo = saldo;
        this.variacaoMesAnterior = variacaoMesAnterior;
        this.percentualVariacao = percentualVariacao;
        this.usuarioId = usuarioId;
    }

    @PrePersist
    void aoCriar() {
        this.dataGeracao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public LocalDate getMesReferencia() { return mesReferencia; }
    public BigDecimal getTotalRecebido() { return totalRecebido; }
    public BigDecimal getTotalGasto() { return totalGasto; }
    public BigDecimal getSaldo() { return saldo; }
    public BigDecimal getVariacaoMesAnterior() { return variacaoMesAnterior; }
    public BigDecimal getPercentualVariacao() { return percentualVariacao; }
    public LocalDateTime getDataGeracao() { return dataGeracao; }
    public Long getUsuarioId() { return usuarioId; }
}