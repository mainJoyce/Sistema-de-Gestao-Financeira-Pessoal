package com.gestaofinanceira.insight.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Observacao gerada pelo sistema a partir das transacoes. Tabela: insight. */
@Entity
@Table(name = "insight")
public class Insight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false, length = 150, updatable = false)
    private String titulo;

    @Column(name = "descricao", nullable = false, length = 500, updatable = false)
    private String descricao;

    @Column(name = "tipo", nullable = false, length = 30, updatable = false)
    private String tipo;

    @Column(name = "justificativa", length = 500, updatable = false)
    private String justificativa;

    /** Chave do periodo analisado (ex.: 2026-10). Evita gerar o mesmo insight duas vezes. */
    @Column(name = "dados_referencia", columnDefinition = "TEXT", updatable = false)
    private String dadosReferencia;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    protected Insight() {
    }

    public Insight(Long usuarioId, String tipo, String titulo, String descricao,
                   String justificativa, String dadosReferencia) {
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.titulo = titulo;
        this.descricao = descricao;
        this.justificativa = justificativa;
        this.dadosReferencia = dadosReferencia;
    }

    @PrePersist
    void aoCriar() {
        this.dataCriacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getTipo() { return tipo; }
    public String getJustificativa() { return justificativa; }
    public String getDadosReferencia() { return dadosReferencia; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public Long getUsuarioId() { return usuarioId; }
}
