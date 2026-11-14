package com.gestaofinanceira.orcamento.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Limite de gasto de uma categoria em um mes. Tabela: orcamento. */
@Entity
@Table(name = "orcamento")
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "valor_limite", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorLimite;

    /** Sempre o dia 1 do mes de referencia. */
    @Column(name = "mes_referencia", nullable = false)
    private LocalDate mesReferencia;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    @Column(name = "categoria_id", nullable = false, updatable = false)
    private Long categoriaId;

    protected Orcamento() {
    }

    public Orcamento(Long usuarioId, Long categoriaId,
                     BigDecimal valorLimite, LocalDate mesReferencia) {
        this.usuarioId = usuarioId;
        this.categoriaId = categoriaId;
        this.valorLimite = valorLimite;
        this.mesReferencia = mesReferencia;
    }

    public void atualizar(BigDecimal valorLimite, LocalDate mesReferencia) {
        this.valorLimite = valorLimite;
        this.mesReferencia = mesReferencia;
    }

    public Long getId() { return id; }
    public BigDecimal getValorLimite() { return valorLimite; }
    public LocalDate getMesReferencia() { return mesReferencia; }
    public Long getUsuarioId() { return usuarioId; }
    public Long getCategoriaId() { return categoriaId; }
    public int getMes() { return mesReferencia.getMonthValue(); }
    public int getAno() { return mesReferencia.getYear(); }
}
