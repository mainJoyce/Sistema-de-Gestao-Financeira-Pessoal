package com.gestaofinanceira.meta.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Meta financeira do usuario. Tabela: meta_financeira. */
@Entity
@Table(name = "meta_financeira")
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 50)
    private String nome;

    @Column(name = "valor_objetivo", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorObjetivo;

    @Column(name = "data_desejada", nullable = false)
    private LocalDate dataDesejada;

    @Column(name = "valor_acumulado", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorAcumulado = BigDecimal.ZERO;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    protected Meta() {
    }

    public Meta(String nome, BigDecimal valorObjetivo, LocalDate dataDesejada, Long usuarioId) {
        this.nome = nome;
        this.valorObjetivo = valorObjetivo;
        this.dataDesejada = dataDesejada;
        this.usuarioId = usuarioId;
    }

    public void atualizarDados(String nome, BigDecimal valorObjetivo, LocalDate dataDesejada) {
        this.nome = nome;
        this.valorObjetivo = valorObjetivo;
        this.dataDesejada = dataDesejada;
    }

    public void registrarAporte(BigDecimal valor) {
        this.valorAcumulado = this.valorAcumulado.add(valor);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public BigDecimal getValorObjetivo() { return valorObjetivo; }
    public LocalDate getDataDesejada() { return dataDesejada; }
    public BigDecimal getValorAcumulado() { return valorAcumulado; }
    public Long getUsuarioId() { return usuarioId; }
}
