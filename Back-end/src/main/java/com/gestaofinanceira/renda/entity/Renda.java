package com.gestaofinanceira.renda.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Fonte de renda do usuario. Tabela: renda_usuario. */
@Entity
@Table(name = "renda_usuario")
public class Renda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoRenda tipo;

    @Column(name = "valor_referencia", precision = 10, scale = 2)
    private BigDecimal valorReferencia;

    @Column(name = "recorrente", nullable = false)
    private boolean recorrente = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodicidade", length = 20)
    private Periodicidade periodicidade;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    protected Renda() {
    }

    public Renda(String nome, TipoRenda tipo, BigDecimal valorReferencia,
                 boolean recorrente, Periodicidade periodicidade, Long usuarioId) {
        this.nome = nome;
        this.tipo = tipo;
        this.valorReferencia = valorReferencia;
        this.recorrente = recorrente;
        this.periodicidade = periodicidade;
        this.usuarioId = usuarioId;
    }

    public void atualizarDados(String nome, TipoRenda tipo, BigDecimal valorReferencia,
                               boolean recorrente, Periodicidade periodicidade) {
        this.nome = nome;
        this.tipo = tipo;
        this.valorReferencia = valorReferencia;
        this.recorrente = recorrente;
        this.periodicidade = periodicidade;
    }

    /** Exclusao logica (RN005). */
    public void desativar() {
        this.ativo = false;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public TipoRenda getTipo() { return tipo; }
    public BigDecimal getValorReferencia() { return valorReferencia; }
    public boolean isRecorrente() { return recorrente; }
    public Periodicidade getPeriodicidade() { return periodicidade; }
    public boolean isAtivo() { return ativo; }
    public Long getUsuarioId() { return usuarioId; }
}
