package com.gestaofinanceira.insight.ia;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cliente da API do Gemini (Google). So funciona se a variavel de ambiente GEMINI_API_KEY
 * estiver definida; sem ela, o sistema usa o texto gerado por regras (nunca quebra).
 * Falha, timeout ou resposta invalida => Optional vazio, e o backend usa o texto de reserva.
 * O backend envia SOMENTE totais agregados: nunca nome, CPF, e-mail ou descricoes de transacoes.
 */
@Component
public class ClienteIa {

    private static final Logger log = LoggerFactory.getLogger(ClienteIa.class);
    private static final Pattern TEXTO = Pattern.compile("\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    private final String apiKey;
    private final String modelo;
    private final Duration limite;
    private final HttpClient http;

    public ClienteIa(@Value("${ia.gemini.api-key:}") String apiKey,
                     @Value("${ia.gemini.model:gemini-2.0-flash}") String modelo,
                     @Value("${ia.gemini.timeout-segundos:10}") long timeoutSegundos) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.modelo = modelo;
        this.limite = Duration.ofSeconds(timeoutSegundos);
        this.http = HttpClient.newBuilder().connectTimeout(limite).build();
    }

    public boolean habilitado() {
        return !apiKey.isBlank();
    }

    public Optional<String> gerarTexto(String prompt) {
        if (!habilitado()) {
            return Optional.empty();
        }
        try {
            String corpo = "{\"contents\":[{\"parts\":[{\"text\":\"" + escaparJson(prompt) + "\"}]}]}";
            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/"
                            + modelo + ":generateContent"))
                    .timeout(limite)
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(corpo))
                    .build();
            HttpResponse<String> resposta = http.send(requisicao,
                    HttpResponse.BodyHandlers.ofString());
            if (resposta.statusCode() != 200) {
                log.warn("Serviço de IA respondeu com status {}", resposta.statusCode());
                return Optional.empty();
            }
            return extrairTexto(resposta.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Serviço de IA indisponível: {}", e.getMessage());
            return Optional.empty();
        }
    }

    static Optional<String> extrairTexto(String json) {
        Matcher m = TEXTO.matcher(json);
        if (m.find()) {
            String texto = desescaparJson(m.group(1)).trim();
            return texto.isEmpty() ? Optional.empty() : Optional.of(texto);
        }
        return Optional.empty();
    }

    static String escaparJson(String texto) {
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    static String desescaparJson(String texto) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c != '\\' || i + 1 >= texto.length()) {
                sb.append(c);
                continue;
            }
            char proximo = texto.charAt(++i);
            switch (proximo) {
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'u':
                    if (i + 4 < texto.length()) {
                        sb.append((char) Integer.parseInt(texto.substring(i + 1, i + 5), 16));
                        i += 4;
                    }
                    break;
                default: sb.append(proximo);
            }
        }
        return sb.toString();
    }
}
