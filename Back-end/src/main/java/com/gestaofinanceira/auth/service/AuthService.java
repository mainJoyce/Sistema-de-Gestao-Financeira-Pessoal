package com.gestaofinanceira.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gestaofinanceira.auth.dto.LoginRequest;
import com.gestaofinanceira.auth.dto.LoginResponse;
import com.gestaofinanceira.config.JwtService;
import com.gestaofinanceira.exception.CredenciaisInvalidasException;
import com.gestaofinanceira.usuario.entity.Usuario;
import com.gestaofinanceira.usuario.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                    PasswordEncoder passwordEncoder,
                    JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException();
        }

        String token = jwtService.gerarToken(usuario.getId(), usuario.getEmail());
        return new LoginResponse(token, "Bearer", jwtService.getExpiracaoSegundos());
    }
}