package com.gestaofinanceira.transacao.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "transacao")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTransacao tipo;

    @Column(length = 150)
    private String estabelecimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Recorrencia recorrencia = Recorrencia.NENHUMA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTransacao status = StatusTransacao.ATIVA;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    @Column(name = "categoria_id")
    private Long categoriaId;

    @Column(name = "importacao_id")
    private Long importacaoId;

    protected Transacao() {
    }

    public Transacao(String descricao, BigDecimal valor, LocalDate data,
                        TipoTransacao tipo, String estabelecimento,
                        Recorrencia recorrencia, Long usuarioId, Long categoriaId) {
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.tipo = tipo;
        this.estabelecimento = estabelecimento;
        this.recorrencia = recorrencia;
        this.usuarioId = usuarioId;
        this.categoriaId = categoriaId;
    }

    public void atualizarDados(String descricao, BigDecimal valor, LocalDate data,
                                TipoTransacao tipo, String estabelecimento,
                                Recorrencia recorrencia, Long categoriaId) {
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.tipo = tipo;
        this.estabelecimento = estabelecimento;
        this.recorrencia = recorrencia;
        this.categoriaId = categoriaId;
    }

    public void excluir() {
        this.status = StatusTransacao.EXCLUIDA;
    }

    public void vincularImportacao(Long importacaoId) {
        this.importacaoId = importacaoId;
    }

    public Long getImportacaoId() { return importacaoId; }

    public Long getId() { return id; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
    public LocalDate getData() { return data; }
    public TipoTransacao getTipo() { return tipo; }
    public String getEstabelecimento() { return estabelecimento; }
    public Recorrencia getRecorrencia() { return recorrencia; }
    public StatusTransacao getStatus() { return status; }
    public Long getUsuarioId() { return usuarioId; }
    public Long getCategoriaId() { return categoriaId; }
}