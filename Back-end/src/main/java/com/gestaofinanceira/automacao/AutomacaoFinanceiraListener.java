package com.gestaofinanceira.automacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.gestaofinanceira.alerta.service.AlertaService;
import com.gestaofinanceira.score.service.ScoreService;
import com.gestaofinanceira.transacao.event.TransacaoAlteradaEvent;

/**
 * Depois que uma transacao e gravada com sucesso, recalcula o score (RN008) e cria os alertas
 * preventivos (US15). Uma falha aqui nunca desfaz nem derruba a operacao do usuario: so e registrada no log.
 */
@Component
public class AutomacaoFinanceiraListener {

    private static final Logger log = LoggerFactory.getLogger(AutomacaoFinanceiraListener.class);

    private final AlertaService alertaService;
    private final ScoreService scoreService;

    public AutomacaoFinanceiraListener(AlertaService alertaService, ScoreService scoreService) {
        this.alertaService = alertaService;
        this.scoreService = scoreService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoAlterarTransacoes(TransacaoAlteradaEvent evento) {
        try {
            alertaService.gerarAlertas(evento.usuarioId());
        } catch (Exception e) {
            log.warn("Não foi possível gerar alertas do usuário {}", evento.usuarioId(), e);
        }
        try {
            scoreService.recalcular(evento.usuarioId());
        } catch (Exception e) {
            // inclui o caso esperado de usuario sem renda cadastrada
            log.info("Score não recalculado para o usuário {}: {}", evento.usuarioId(), e.getMessage());
        }
    }
}
