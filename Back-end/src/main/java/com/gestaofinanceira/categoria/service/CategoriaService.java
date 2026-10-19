package com.gestaofinanceira.categoria.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.categoria.dto.CategoriaRequest;
import com.gestaofinanceira.categoria.dto.CategoriaResponse;
import com.gestaofinanceira.categoria.entity.Categoria;
import com.gestaofinanceira.categoria.repository.CategoriaRepository;
import com.gestaofinanceira.exception.RecursoNaoEncontradoException;
import com.gestaofinanceira.exception.RegraDeNegocioException;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar(Long usuarioId) {
        return categoriaRepository.listarDisponiveis(usuarioId).stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long id, Long usuarioId) {
        return paraResponse(buscarOuFalhar(id, usuarioId));
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request, Long usuarioId) {
        Categoria categoria = new Categoria(
                request.nome(), request.icone(), request.tipo(), usuarioId);
        return paraResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request, Long usuarioId) {
        Categoria categoria = buscarOuFalhar(id, usuarioId);
        exigirQueNaoSejaPadrao(categoria, "alterada");
        categoria.setNome(request.nome());
        categoria.setIcone(request.icone());
        categoria.setTipo(request.tipo());
        return paraResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        Categoria categoria = buscarOuFalhar(id, usuarioId);
        exigirQueNaoSejaPadrao(categoria, "excluída");
        categoria.setAtivo(false);
        categoriaRepository.save(categoria);
    }

    private Categoria buscarOuFalhar(Long id, Long usuarioId) {
        return categoriaRepository.buscarDisponivelPorId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria com id " + id + " não encontrada"));
    }

    private void exigirQueNaoSejaPadrao(Categoria categoria, String acao) {
        if (categoria.isPadrao()) {
            throw new RegraDeNegocioException(
                    "Categorias padrão não podem ser " + acao);
        }
    }

    private CategoriaResponse paraResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getIcone(),
                categoria.getTipo(),
                categoria.isPadrao());
    }
}