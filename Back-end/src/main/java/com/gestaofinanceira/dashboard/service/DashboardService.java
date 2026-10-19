package com.gestaofinanceira.dashboard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.categoria.dto.CategoriaResponse;
import com.gestaofinanceira.categoria.service.CategoriaService;
import com.gestaofinanceira.dashboard.dto.DashboardResponse;
import com.gestaofinanceira.dashboard.dto.GastoCategoriaResponse;
import com.gestaofinanceira.transacao.entity.TipoTransacao;
import com.gestaofinanceira.transacao.repository.TransacaoRepository;

@Service
public class DashboardService {

    private final TransacaoRepository transacaoRepository;
    private final CategoriaService categoriaService;

    public DashboardService(TransacaoRepository transacaoRepository,
                            CategoriaService categoriaService) {
        this.transacaoRepository = transacaoRepository;
        this.categoriaService = categoriaService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse resumo(Long usuarioId, YearMonth mes) {
        YearMonth mesConsulta = (mes != null) ? mes : YearMonth.now();
        LocalDate inicio = mesConsulta.atDay(1);
        LocalDate fim = mesConsulta.atEndOfMonth();

        BigDecimal receita = transacaoRepository
                .somarPorTipo(usuarioId, TipoTransacao.RECEITA, inicio, fim);
        BigDecimal despesa = transacaoRepository
                .somarPorTipo(usuarioId, TipoTransacao.DESPESA, inicio, fim);
        BigDecimal saldo = receita.subtract(despesa);

        BigDecimal comprometimento = percentual(despesa, receita);

        Map<Long, String> nomes = new HashMap<>();
        for (CategoriaResponse c : categoriaService.listar(usuarioId)) {
            nomes.put(c.id(), c.nome());
        }

        List<GastoCategoriaResponse> porCategoria = new ArrayList<>();
        for (Object[] linha : transacaoRepository
                .somarDespesasPorCategoria(usuarioId, inicio, fim)) {
            Long categoriaId = (Long) linha[0];
            BigDecimal valor = (BigDecimal) linha[1];
            String nome = (categoriaId == null)
                    ? "Sem categoria"
                    : nomes.getOrDefault(categoriaId, "Categoria removida");
            porCategoria.add(new GastoCategoriaResponse(
                    categoriaId, nome, valor, percentual(valor, despesa)));
        }

        return new DashboardResponse(
                mesConsulta, receita, despesa, saldo, comprometimento, porCategoria);
    }

    private BigDecimal percentual(BigDecimal parte, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return parte.multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP);
    }
}