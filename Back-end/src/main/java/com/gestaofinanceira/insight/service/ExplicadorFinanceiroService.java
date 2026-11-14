package com.gestaofinanceira.insight.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.dashboard.dto.DashboardResponse;
import com.gestaofinanceira.dashboard.dto.GastoCategoriaResponse;
import com.gestaofinanceira.dashboard.service.DashboardService;
import com.gestaofinanceira.exception.RegraDeNegocioException;
import com.gestaofinanceira.insight.dto.ExplicacaoResponse;
import com.gestaofinanceira.insight.ia.ClienteIa;

/**
 * Explica o mes em linguagem simples. Os numeros vem do backend. A IA (opcional) so reescreve
 * o texto de forma mais amigavel; se estiver indisponivel ou responder algo fora do esperado,
 * o texto gerado por regras e usado (fluxo de contingencia descrito na entrega parcial).
 */
@Service
public class ExplicadorFinanceiroService {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final String AVISO_IA =
            "Texto gerado por inteligência artificial a partir dos totais do mês. "
                    + "Não é recomendação de investimento.";
    private static final String AVISO_REGRAS =
            "Texto gerado automaticamente a partir dos totais do mês.";

    private final DashboardService dashboardService;
    private final ClienteIa clienteIa;

    public ExplicadorFinanceiroService(DashboardService dashboardService, ClienteIa clienteIa) {
        this.dashboardService = dashboardService;
        this.clienteIa = clienteIa;
    }

    @Transactional(readOnly = true)
    public ExplicacaoResponse explicar(Long usuarioId, YearMonth mes) {
        YearMonth alvo = (mes != null) ? mes : YearMonth.now();
        DashboardResponse resumo = dashboardService.resumo(usuarioId, alvo);

        if (resumo.receitaTotal().signum() == 0 && resumo.despesaTotal().signum() == 0) {
            throw new RegraDeNegocioException(
                    "Não há transações no mês informado para gerar a explicação");
        }

        String base = textoPorRegras(alvo, resumo);

        Optional<String> ia = clienteIa.gerarTexto(montarPrompt(base));
        if (ia.isPresent() && respostaAceitavel(ia.get())) {
            return new ExplicacaoResponse(ia.get(), true, AVISO_IA);
        }
        return new ExplicacaoResponse(base, false, AVISO_REGRAS);
    }

    private String textoPorRegras(YearMonth mes, DashboardResponse r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Em ").append(mes).append(", você recebeu ").append(moeda(r.receitaTotal()))
                .append(" e gastou ").append(moeda(r.despesaTotal())).append(". ");
        if (r.saldo().signum() >= 0) {
            sb.append("Seu saldo ficou positivo em ").append(moeda(r.saldo())).append(". ");
        } else {
            sb.append("Seu saldo ficou negativo em ").append(moeda(r.saldo().abs())).append(". ");
        }
        if (r.receitaTotal().signum() > 0) {
            sb.append("Isso significa que ").append(r.percentualComprometimento())
                    .append("% da sua renda foi gasta. ");
        }
        if (!r.gastosPorCategoria().isEmpty()) {
            GastoCategoriaResponse maior = r.gastosPorCategoria().get(0);
            sb.append("Sua maior despesa foi com ").append(maior.categoria()).append(": ")
                    .append(moeda(maior.valor())).append(" (").append(maior.percentual())
                    .append("% dos gastos).");
        }
        return sb.toString().trim();
    }

    private String montarPrompt(String fatos) {
        return "Você é um assistente de educação financeira. Reescreva o texto abaixo em português do Brasil, "
                + "com linguagem muito simples, em no máximo 5 frases curtas, para uma pessoa sem conhecimento "
                + "de finanças. Use apenas os números do texto, não invente valores, não prometa resultados "
                + "e não recomende investimentos específicos.\n\nTexto: " + fatos;
    }

    /** Barreira do backend: descarta respostas vazias, longas demais ou com promessas. */
    private boolean respostaAceitavel(String texto) {
        String minusculo = texto.toLowerCase(PT_BR);
        return !texto.isBlank()
                && texto.length() <= 1200
                && !minusculo.contains("garant")
                && !minusculo.contains("lucro certo")
                && !minusculo.contains("sem risco");
    }

    private String moeda(BigDecimal valor) {
        return String.format(PT_BR, "R$ %,.2f", valor);
    }
}
