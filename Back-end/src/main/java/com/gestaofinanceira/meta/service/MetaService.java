package com.gestaofinanceira.meta.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.meta.entity.Meta;
import com.gestaofinanceira.meta.repository.MetaRepository;

@Service
public class MetaService {

    private final MetaRepository metaRepository;

    public MetaService(MetaRepository metaRepository) {
        this.metaRepository = metaRepository;
    }

    public List<Meta> listarPorUsuario(Long usuarioId) {
        return metaRepository.findByUsuarioId(usuarioId);
    }

    public Meta buscarPorId(Long id, Long usuarioId) {
        return metaRepository.findById(id)
                .filter(meta -> usuarioId.equals(meta.getUsuarioId()))
                .orElseThrow(() ->
                        new RuntimeException("Meta não encontrada"));
    }

    @Transactional
    public Meta criar(Meta meta, Long usuarioId) {
        meta.setUsuarioId(usuarioId);
        return metaRepository.save(meta);
    }

    @Transactional
    public Meta atualizar(Long id, Meta dados, Long usuarioId) {
        Meta meta = buscarPorId(id, usuarioId);

        meta.setNome(dados.getNome());
        meta.setValorObjetivo(dados.getValorObjetivo());
        meta.setDataDesejada(dados.getDataDesejada());

        return metaRepository.save(meta);
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        Meta meta = buscarPorId(id, usuarioId);
        metaRepository.delete(meta);
    }
}