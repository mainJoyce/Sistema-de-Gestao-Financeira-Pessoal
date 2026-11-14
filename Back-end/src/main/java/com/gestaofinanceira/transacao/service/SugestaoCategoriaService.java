package com.gestaofinanceira.transacao.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.categoria.dto.CategoriaResponse;
import com.gestaofinanceira.categoria.service.CategoriaService;
import com.gestaofinanceira.transacao.dto.SugestaoCategoriaResponse;
import com.gestaofinanceira.transacao.entity.TipoTransacao;
import com.gestaofinanceira.transacao.repository.TransacaoRepository;

/** Sugestao automatica de categoria (documento, secao 3.2). */
@Service
public class SugestaoCategoriaService {

    private static final String CATEGORIA_PADRAO = "Outros";

    private final TransacaoRepository transacaoRepository;
    private final CategoriaService categoriaService;

    public SugestaoCategoriaService(TransacaoRepository transacaoRepository,
                                    CategoriaService categoriaService) {
        this.transacaoRepository = transacaoRepository;
        this.categoriaService = categoriaService;
    }

    @Transactional(readOnly = true)
    public Optional<SugestaoCategoriaResponse> sugerir(Long usuarioId, TipoTransacao tipo,
                                                       String estabelecimento, String descricao) {
        List<CategoriaResponse> disponiveis = categoriaService.listar(usuarioId);

        // 1) o que o proprio usuario ja fez com esse estabelecimento
        if (estabelecimento != null && !estabelecimento.isBlank()) {
            List<Long> usadas = transacaoRepository.categoriasMaisUsadas(
                    usuarioId, tipo, estabelecimento.trim().toLowerCase(), PageRequest.of(0, 1));
            for (Long categoriaId : usadas) {
                Optional<CategoriaResponse> categoria = disponiveis.stream()
                        .filter(c -> c.id().equals(categoriaId))
                        .findFirst();
                if (categoria.isPresent()) {
                    return Optional.of(resposta(categoria.get(), "HISTORICO"));
                }
            }
        }

        // 2) reconhecimento pelo nome (so para despesas)
        if (tipo == TipoTransacao.DESPESA) {
            String texto = (estabelecimento == null ? "" : estabelecimento) + " "
                    + (descricao == null ? "" : descricao);
            Optional<String> nome = ClassificadorPalavraChave.categoriaPara(texto);
            if (nome.isPresent()) {
                Optional<CategoriaResponse> categoria = padraoPorNome(disponiveis, nome.get());
                if (categoria.isPresent()) {
                    return Optional.of(resposta(categoria.get(), "PALAVRA_CHAVE"));
                }
            }
            // 3) toda despesa precisa ter categoria (secao 3.2): cai em "Outros"
            return padraoPorNome(disponiveis, CATEGORIA_PADRAO)
                    .map(c -> resposta(c, "PADRAO"));
        }
        return Optional.empty();
    }

    private Optional<CategoriaResponse> padraoPorNome(List<CategoriaResponse> categorias, String nome) {
        return categorias.stream()
                .filter(c -> c.padrao() && c.nome().equalsIgnoreCase(nome))
                .findFirst();
    }

    private SugestaoCategoriaResponse resposta(CategoriaResponse c, String origem) {
        return new SugestaoCategoriaResponse(c.id(), c.nome(), origem);
    }
}
