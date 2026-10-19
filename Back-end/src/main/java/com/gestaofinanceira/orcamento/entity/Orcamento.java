package com.gestaofinanceira.orcamento.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orcamento")
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private Long categoriaId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorLimite;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false)
    private Boolean ativa = true;

    @Column(nullable = false)
    private Boolean excluida = false;

    protected Orcamento() {
    }

    public Orcamento(Long usuarioId, Long categoriaId,
                     BigDecimal valorLimite, Integer mes, Integer ano) {
        this.usuarioId = usuarioId;
        this.categoriaId = categoriaId;
        this.valorLimite = valorLimite;
        this.mes = mes;
        this.ano = ano;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public BigDecimal getValorLimite() {
        return valorLimite;
    }

    public Integer getMes() {
        return mes;
    }

    public Integer getAno() {
        return ano;
    }

    public void atualizar(BigDecimal valorLimite, Integer mes, Integer ano) {
        this.valorLimite = valorLimite;
        this.mes = mes;
        this.ano = ano;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public Boolean getExcluida() {
        return excluida;
    }

public void excluirLogicamente() {
    this.ativa = false;
    this.excluida = true;
}
}
