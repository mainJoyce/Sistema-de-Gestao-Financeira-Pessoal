package com.gestaofinanceira.usuario.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestaofinanceira.exception.ConflitoException;
import com.gestaofinanceira.exception.RecursoNaoEncontradoException;
import com.gestaofinanceira.usuario.dto.UsuarioCadastroRequest;
import com.gestaofinanceira.usuario.dto.UsuarioResponse;
import com.gestaofinanceira.usuario.entity.Usuario;
import com.gestaofinanceira.usuario.repository.UsuarioRepository;
import com.gestaofinanceira.usuario.dto.UsuarioAtualizacaoRequest;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse cadastrar(UsuarioCadastroRequest request) {
        String email = request.email().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflitoException("E-mail já cadastrado");
        }
        if (usuarioRepository.existsByCpf(request.cpf())) {
            throw new ConflitoException("CPF já cadastrado");
        }

        Usuario usuario = new Usuario(
                request.nomeCompleto().trim(),
                request.cpf(),
                request.dataNascimento(),
                email,
                request.telefone().trim(),
                request.rendaMensal(),
                request.dependentes(),
                passwordEncoder.encode(request.senha()));

        return paraResponse(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado"));
        return paraResponse(usuario);
    }

        @Transactional
    public UsuarioResponse atualizar(Long usuarioId, UsuarioAtualizacaoRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        String email = request.email().trim().toLowerCase();
        if (!email.equals(usuario.getEmail()) && usuarioRepository.existsByEmail(email)) {
            throw new ConflitoException("E-mail já cadastrado");
        }

        usuario.setNomeCompleto(request.nomeCompleto().trim());
        usuario.setEmail(email);
        usuario.setTelefone(request.telefone().trim());
        usuario.setRendaMensal(request.rendaMensal());
        usuario.setDependentes(request.dependentes());
        return paraResponse(usuarioRepository.save(usuario));
    }

    private UsuarioResponse paraResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getNomeCompleto(), u.getDataNascimento(),
                u.getEmail(), u.getTelefone(), u.getRendaMensal(),
                u.getDependentes());
    }
}
