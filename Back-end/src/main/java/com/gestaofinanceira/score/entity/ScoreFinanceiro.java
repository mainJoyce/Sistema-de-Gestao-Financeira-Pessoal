package com.gestaofinanceira.score.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Foto do score do usuario em um momento. Tabela: score_financeiro (RN008). */
@Entity
@Table(name = "score_financeiro")
public class ScoreFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pontuacao", nullable = false, updatable = false)
    private int pontuacao;

    @Column(name = "classificacao", nullable = false, length = 30, updatable = false)
    private String classificacao;

    @Column(name = "controle_despesas", updatable = false)
    private Integer controleDespesas;

    @Column(name = "cumprimento_orcamento", updatable = false)
    private Integer cumprimentoOrcamento;

    @Column(name = "regularidade_receitas", updatable = false)
    private Integer regularidadeReceitas;

    @Column(name = "reserva_financeira", updatable = false)
    private Integer reservaFinanceira;

    @Column(name = "evolucao_metas", updatable = false)
    private Integer evolucaoMetas;

    @Column(name = "data_calculo", nullable = false, updatable = false)
    private LocalDateTime dataCalculo;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    protected ScoreFinanceiro() {
    }

    public ScoreFinanceiro(Long usuarioId, int pontuacao, String classificacao,
                           int controleDespesas, int cumprimentoOrcamento,
                           int regularidadeReceitas, int reservaFinanceira, int evolucaoMetas) {
        this.usuarioId = usuarioId;
        this.pontuacao = pontuacao;
        this.classificacao = classificacao;
        this.controleDespesas = controleDespesas;
        this.cumprimentoOrcamento = cumprimentoOrcamento;
        this.regularidadeReceitas = regularidadeReceitas;
        this.reservaFinanceira = reservaFinanceira;
        this.evolucaoMetas = evolucaoMetas;
    }

    @PrePersist
    void aoCriar() {
        this.dataCalculo = LocalDateTime.now();
    }

    public boolean temOsMesmosValoresDe(int pontuacao, int controle, int orcamento,
                                        int receitas, int reserva, int metas) {
        return this.pontuacao == pontuacao
                && controleDespesas != null && controleDespesas == controle
                && cumprimentoOrcamento != null && cumprimentoOrcamento == orcamento
                && regularidadeReceitas != null && regularidadeReceitas == receitas
                && reservaFinanceira != null && reservaFinanceira == reserva
                && evolucaoMetas != null && evolucaoMetas == metas;
    }

    public Long getId() { return id; }
    public int getPontuacao() { return pontuacao; }
    public String getClassificacao() { return classificacao; }
    public Integer getControleDespesas() { return controleDespesas; }
    public Integer getCumprimentoOrcamento() { return cumprimentoOrcamento; }
    public Integer getRegularidadeReceitas() { return regularidadeReceitas; }
    public Integer getReservaFinanceira() { return reservaFinanceira; }
    public Integer getEvolucaoMetas() { return evolucaoMetas; }
    public LocalDateTime getDataCalculo() { return dataCalculo; }
    public Long getUsuarioId() { return usuarioId; }
}
