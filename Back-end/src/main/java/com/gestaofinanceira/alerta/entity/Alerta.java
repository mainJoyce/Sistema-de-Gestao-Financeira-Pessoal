package com.gestaofinanceira.alerta.entity;

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

/** Aviso gerado pelo sistema para o usuario. Tabela: alerta. */
@Entity
@Table(name = "alerta")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30, updatable = false)
    private TipoAlerta tipo;

    @Column(name = "titulo", nullable = false, length = 150, updatable = false)
    private String titulo;

    @Column(name = "mensagem", nullable = false, length = 500, updatable = false)
    private String mensagem;

    @Column(name = "lida", nullable = false)
    private boolean lida = false;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_leitura")
    private LocalDateTime dataLeitura;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    protected Alerta() {
    }

    public Alerta(TipoAlerta tipo, String titulo, String mensagem, Long usuarioId) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.usuarioId = usuarioId;
    }

    @PrePersist
    void aoCriar() {
        this.dataCriacao = LocalDateTime.now();
    }

    public void marcarComoLida() {
        if (!this.lida) {
            this.lida = true;
            this.dataLeitura = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public TipoAlerta getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getMensagem() { return mensagem; }
    public boolean isLida() { return lida; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataLeitura() { return dataLeitura; }
    public Long getUsuarioId() { return usuarioId; }
}
