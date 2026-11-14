package com.gestaofinanceira.recomendacao.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Sugestao educativa gerada pelo sistema. Tabela: recomendacao_investimento. */
@Entity
@Table(name = "recomendacao_investimento")
public class RecomendacaoInvestimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo", nullable = false, length = 50, updatable = false)
    private String tipo;

    @Column(name = "titulo", nullable = false, length = 150, updatable = false)
    private String titulo;

    @Column(name = "descricao", nullable = false, length = 500, updatable = false)
    private String descricao;

    @Column(name = "valor_sugerido", precision = 10, scale = 2, updatable = false)
    private BigDecimal valorSugerido;

    @Column(name = "justificativa", length = 500, updatable = false)
    private String justificativa;

    @Column(name = "versao_regra", nullable = false, length = 20, updatable = false)
    private String versaoRegra;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    protected RecomendacaoInvestimento() {
    }

    public RecomendacaoInvestimento(Long usuarioId, String tipo, String titulo,
                                    String descricao, BigDecimal valorSugerido,
                                    String justificativa, String versaoRegra) {
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.titulo = titulo;
        this.descricao = descricao;
        this.valorSugerido = valorSugerido;
        this.justificativa = justificativa;
        this.versaoRegra = versaoRegra;
    }

    @PrePersist
    void aoCriar() {
        this.dataCriacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValorSugerido() { return valorSugerido; }
    public String getJustificativa() { return justificativa; }
    public String getVersaoRegra() { return versaoRegra; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public Long getUsuarioId() { return usuarioId; }
}
