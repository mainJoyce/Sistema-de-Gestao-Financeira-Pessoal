package com.gestaofinanceira.importacao.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gestaofinanceira.exception.RecursoNaoEncontradoException;
import com.gestaofinanceira.exception.RegraDeNegocioException;
import com.gestaofinanceira.importacao.dto.ImportacaoResponse;
import com.gestaofinanceira.importacao.entity.ImportacaoBancaria;
import com.gestaofinanceira.importacao.entity.StatusImportacao;
import com.gestaofinanceira.importacao.entity.TipoOrigem;
import com.gestaofinanceira.importacao.repository.ImportacaoBancariaRepository;
import com.gestaofinanceira.transacao.entity.TipoTransacao;
import com.gestaofinanceira.transacao.service.TransacaoService;

@Service
public class ImportacaoBancariaService {

    private static final int MAX_LINHAS = 1000;
    private static final int MAX_ERROS_LISTADOS = 20;
    private static final String CABECALHO = "data;descricao;valor;tipo";

    private final ImportacaoBancariaRepository importacaoRepository;
    private final TransacaoService transacaoService;

    public ImportacaoBancariaService(ImportacaoBancariaRepository importacaoRepository,
                                     TransacaoService transacaoService) {
        this.importacaoRepository = importacaoRepository;
        this.transacaoService = transacaoService;
    }

    @Transactional
    public ImportacaoResponse importar(MultipartFile arquivo, Long usuarioId) {
        validarArquivo(arquivo);
        List<String> linhas = lerLinhas(arquivo);
        validarConteudo(linhas);

        ImportacaoBancaria importacao = importacaoRepository.save(
                new ImportacaoBancaria(usuarioId, nomeSeguro(arquivo), TipoOrigem.CSV));

        int importadas = 0;
        int duplicadas = 0;
        int comErro = 0;
        List<String> erros = new ArrayList<>();

        for (int i = 1; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            if (linha.isBlank()) {
                continue;
            }
            try {
                LinhaCsv l = interpretar(linha);
                if (transacaoService.existeDuplicada(
                        usuarioId, l.data(), l.valor(), l.tipo(), l.descricao())) {
                    duplicadas++;
                } else {
                    transacaoService.criarImportada(usuarioId, importacao.getId(),
                            l.descricao(), l.valor(), l.data(), l.tipo(), l.estabelecimento());
                    importadas++;
                }
            } catch (IllegalArgumentException e) {
                comErro++;
                if (erros.size() < MAX_ERROS_LISTADOS) {
                    erros.add("Linha " + (i + 1) + ": " + e.getMessage());
                }
            }
        }

        boolean aproveitou = importadas > 0 || duplicadas > 0;
        importacao.concluir(aproveitou ? StatusImportacao.CONCLUIDA : StatusImportacao.FALHOU);
        importacaoRepository.save(importacao);

        return paraResponse(importacao, importadas, duplicadas, comErro, erros);
    }

    @Transactional(readOnly = true)
    public List<ImportacaoResponse> listar(Long usuarioId) {
        return importacaoRepository.listar(usuarioId).stream()
                .map(i -> paraResponse(i, null, null, null, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public ImportacaoResponse buscarPorId(Long id, Long usuarioId) {
        return importacaoRepository.buscarPorIdDoUsuario(id, usuarioId)
                .map(i -> paraResponse(i, null, null, null, null))
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Importação com id " + id + " não encontrada"));
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraDeNegocioException("O arquivo está vazio");
        }
        String nome = arquivo.getOriginalFilename();
        if (nome == null || !nome.toLowerCase().endsWith(".csv")) {
            throw new RegraDeNegocioException("Formato não suportado: envie um arquivo .csv");
        }
    }

    private List<String> lerLinhas(MultipartFile arquivo) {
        try (BufferedReader leitor = new BufferedReader(
                new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> linhas = new ArrayList<>(leitor.lines().toList());
            if (!linhas.isEmpty()) {
                linhas.set(0, linhas.get(0).replace("\uFEFF", ""));
            }
            return linhas;
        } catch (IOException e) {
            throw new RegraDeNegocioException("Não foi possível ler o arquivo");
        }
    }

    private void validarConteudo(List<String> linhas) {
        if (linhas.isEmpty()
                || !linhas.get(0).trim().toLowerCase().startsWith(CABECALHO)) {
            throw new RegraDeNegocioException(
                    "Cabeçalho inválido. A primeira linha deve ser: "
                    + "data;descricao;valor;tipo;estabelecimento");
        }
        if (linhas.size() - 1 > MAX_LINHAS) {
            throw new RegraDeNegocioException(
                    "O arquivo excede o limite de " + MAX_LINHAS + " linhas");
        }
    }

    private String nomeSeguro(MultipartFile arquivo) {
        String nome = arquivo.getOriginalFilename();
        int corte = Math.max(nome.lastIndexOf('/'), nome.lastIndexOf('\\'));
        String limpo = nome.substring(corte + 1);
        return limpo.length() > 255 ? limpo.substring(0, 255) : limpo;
    }

    private LinhaCsv interpretar(String linha) {
        String[] c = linha.split(";", -1);
        if (c.length < 4) {
            throw new IllegalArgumentException("esperadas ao menos 4 colunas separadas por ';'");
        }

        LocalDate data;
        try {
            data = LocalDate.parse(c[0].trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("data inválida (use AAAA-MM-DD)");
        }

        String descricao = c[1].trim();
        if (descricao.isEmpty() || descricao.length() > 255) {
            throw new IllegalArgumentException("descrição obrigatória (máximo 255 caracteres)");
        }

        BigDecimal valor;
        try {
            valor = new BigDecimal(c[2].trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("valor inválido");
        }
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException("o valor deve ser maior que zero");
        }
        if (valor.scale() > 2 || valor.precision() - valor.scale() > 8) {
            throw new IllegalArgumentException("valor fora do formato (máx. 8 inteiros e 2 decimais)");
        }

        TipoTransacao tipo;
        try {
            tipo = TipoTransacao.valueOf(c[3].trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("tipo inválido (use RECEITA ou DESPESA)");
        }

        String estabelecimento = (c.length > 4 && !c[4].isBlank()) ? c[4].trim() : null;
        if (estabelecimento != null && estabelecimento.length() > 150) {
            throw new IllegalArgumentException("estabelecimento com mais de 150 caracteres");
        }

        return new LinhaCsv(data, descricao, valor, tipo, estabelecimento);
    }

    private ImportacaoResponse paraResponse(ImportacaoBancaria i, Integer importadas,
                                            Integer duplicadas, Integer comErro,
                                            List<String> erros) {
        return new ImportacaoResponse(
                i.getId(), i.getStatus(), i.getTipoOrigem(), i.getNomeArquivoOrigem(),
                i.getDataCriacao(), i.getDataConclusao(),
                importadas, duplicadas, comErro, erros);
    }

    private record LinhaCsv(LocalDate data, String descricao, BigDecimal valor,
                            TipoTransacao tipo, String estabelecimento) {
    }
}