package com.gestaofinanceira.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(
            RecursoNaoEncontradoException ex, HttpServletRequest request) {
        return montarResposta(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return montarResposta(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarJsonInvalido(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return montarResposta(HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido ou malformado", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tratarTipoInvalido(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return montarResposta(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + ex.getName() + "'", request);
    }

        @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResposta> tratarRegraDeNegocio(
            RegraDeNegocioException ex, HttpServletRequest request) {
        return montarResposta(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request);
    }

        @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResposta> tratarConflito(
            ConflitoException ex, HttpServletRequest request) {
        return montarResposta(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

        @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResposta> tratarCredenciais(
            CredenciaisInvalidasException ex, HttpServletRequest request) {
        return montarResposta(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroGenerico(
            Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {}", request.getRequestURI(), ex);
        return montarResposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno do servidor", request);
    }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResposta> tratarMetodoNaoSuportado(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return montarResposta(HttpStatus.METHOD_NOT_ALLOWED,
                "Método HTTP não suportado para este endpoint", request);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErroResposta> tratarParteAusente(
            MissingServletRequestPartException ex, HttpServletRequest request) {
        return montarResposta(HttpStatus.BAD_REQUEST,
                "Envie o arquivo no campo 'arquivo'", request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErroResposta> tratarArquivoGrande(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return montarResposta(HttpStatus.valueOf(413),
                "Arquivo maior que o limite permitido (1 MB)", request);
    }

    private ResponseEntity<ErroResposta> montarResposta(
            HttpStatus status, String mensagem, HttpServletRequest request) {
        ErroResposta corpo = new ErroResposta(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(corpo);
    }
}