package com.gestaofinanceira.transacao.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Sugere o NOME de uma categoria padrao a partir do texto (estabelecimento/descricao).
 * A ordem importa: a primeira categoria que casar vence.
 * Exemplos do documento: McDonald's -> Alimentacao, Netflix -> Assinaturas,
 * Posto Shell -> Transporte, Farmacia Sao Joao -> Saude.
 */
public final class ClassificadorPalavraChave {

    private static final Map<String, List<Pattern>> REGRAS = new LinkedHashMap<>();

    static {
        regra("Assinaturas", "netflix", "spotify", "disney", "hbo", "amazon prime",
                "prime video", "youtube premium", "globoplay", "deezer", "apple music");
        regra("Compras", "mercado livre", "amazon", "magalu", "magazine luiza",
                "americanas", "shopee", "shein", "aliexpress", "casas bahia", "renner");
        regra("Alimentação", "mcdonald", "burger", "pizza", "restaurante", "lanchonete",
                "padaria", "supermercado", "mercado", "atacadao", "assai", "carrefour",
                "ifood", "rappi", "acai", "sorveteria", "cafeteria", "churrascaria",
                "hortifruti", "acougue");
        regra("Transporte", "uber", "99pop", "cabify", "posto", "shell", "ipiranga",
                "combustivel", "gasolina", "estacionamento", "pedagio", "metro",
                "onibus", "passagem", "taxi", "oficina", "mecanica");
        regra("Saúde", "farmacia", "drogaria", "drogasil", "pacheco", "hospital",
                "clinica", "laboratorio", "dentista", "unimed", "hapvida", "plano de saude");
        regra("Educação", "escola", "colegio", "faculdade", "universidade", "curso",
                "udemy", "alura", "livraria");
        regra("Moradia", "aluguel", "condominio", "imobiliaria", "material de construcao");
        regra("Contas e Serviços", "energia", "neoenergia", "celpe", "enel", "compesa",
                "saneamento", "internet", "telefon", "vivo", "claro");
        regra("Impostos", "ipva", "iptu", "imposto", "darf", "receita federal", "irpf");
        regra("Lazer", "cinema", "teatro", "show", "ingresso", "steam", "playstation",
                "xbox", "boteco", "choperia", "parque", "viagem", "hotel", "airbnb");
        regra("Investimentos", "tesouro", "cdb", "corretora", "fundo de investimento",
                "poupanca");
    }

    private ClassificadorPalavraChave() {
    }

    private static void regra(String categoria, String... palavras) {
        List<Pattern> padroes = new ArrayList<>();
        for (String palavra : palavras) {
            // o texto comeca na palavra (evita "posto" casar com "composto")
            padroes.add(Pattern.compile("(?<![a-z0-9])" + Pattern.quote(palavra)));
        }
        REGRAS.put(categoria, padroes);
    }

    /** Devolve o nome da categoria padrao sugerida, ou vazio se nada casar. */
    public static Optional<String> categoriaPara(String texto) {
        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }
        String normalizado = normalizar(texto);
        for (Map.Entry<String, List<Pattern>> regra : REGRAS.entrySet()) {
            for (Pattern padrao : regra.getValue()) {
                if (padrao.matcher(normalizado).find()) {
                    return Optional.of(regra.getKey());
                }
            }
        }
        return Optional.empty();
    }

    public static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).trim();
    }
}
