package com.gestaofinanceira.importacao.entity;

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
@Table(name = "importacao_bancaria")
public class ImportacaoBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusImportacao status;

    @Column(name = "nome_arquivo_origem", nullable = false, length = 255, updatable = false)
    private String nomeArquivoOrigem;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_origem", nullable = false, length = 25, updatable = false)
    private TipoOrigem tipoOrigem;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    protected ImportacaoBancaria() {
    }

    public ImportacaoBancaria(Long usuarioId, String nomeArquivoOrigem, TipoOrigem tipoOrigem) {
        this.usuarioId = usuarioId;
        this.nomeArquivoOrigem = nomeArquivoOrigem;
        this.tipoOrigem = tipoOrigem;
        this.status = StatusImportacao.FALHOU; // só vira CONCLUIDA se terminar bem
    }

    @PrePersist
    void aoCriar() {
        this.dataCriacao = LocalDateTime.now();
    }

    public void concluir(StatusImportacao statusFinal) {
        this.status = statusFinal;
        this.dataConclusao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public StatusImportacao getStatus() { return status; }
    public String getNomeArquivoOrigem() { return nomeArquivoOrigem; }
    public TipoOrigem getTipoOrigem() { return tipoOrigem; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataConclusao() { return dataConclusao; }
}