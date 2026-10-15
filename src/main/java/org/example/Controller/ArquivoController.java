package org.example.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import org.example.Models.*;

/** JSON versionado com dados explícitos; não carrega nomes arbitrários de classes. */
public final class ArquivoController {
    private final Gson gson = new GsonBuilder().setStrictness(Strictness.STRICT).setPrettyPrinting().create();
    private record Coordenada(Double x, Double y) { }
    private record FormaDados(String tipo, List<Coordenada> pontos, String preenchimento,
            String borda, Double espessuraBorda, String groupId) { }
    private record CanvasDados(Double largura, Double altura, Boolean gradeMagnetica) { }
    private record ArquivoDados(Integer versao, CanvasDados canvas, List<FormaDados> formas) { }

    public void salvar(Path destino, DocumentoVetorial documento) throws IOException {
        List<FormaDados> formas = documento.formas().stream().map(f -> new FormaDados(
                f.getClass().getSimpleName(), f.getPontos().stream().map(p -> new Coordenada(p.getX(), p.getY())).toList(),
                f.getCorPreenchimentoHex(), f.getCorBordaHex(), f.getEspessuraBorda(), f.getGroupId())).toList();
        String json = gson.toJson(new ArquivoDados(1, new CanvasDados(documento.larguraCanvas(),
                documento.alturaCanvas(), documento.gradeMagnetica()), formas));
        Path absoluto = destino.toAbsolutePath();
        Path temporario = Files.createTempFile(absoluto.getParent(), ".editor-vetorial-", ".tmp");
        try {
            Files.writeString(temporario, json, StandardCharsets.UTF_8);
            try {
                Files.move(temporario, absoluto, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException erro) {
                Files.move(temporario, absoluto, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporario);
        }
    }

    public DocumentoVetorial carregar(Path origem) throws IOException {
        try (var leitor = Files.newBufferedReader(origem, StandardCharsets.UTF_8)) {
            ArquivoDados dados = gson.fromJson(leitor, ArquivoDados.class);
            exigir(dados != null && dados.versao != null && dados.versao == 1, "Versão de arquivo ausente ou incompatível.");
            exigir(dados.canvas != null && dados.formas != null, "Canvas ou lista de formas ausente.");
            CanvasDados canvas = dados.canvas;
            exigir(canvas.largura != null && canvas.altura != null && canvas.gradeMagnetica != null,
                    "Configurações do canvas incompletas.");
            List<FormaGeometrica> formas = dados.formas.stream().map(this::reconstruir).toList();
            return new DocumentoVetorial(canvas.largura, canvas.altura, canvas.gradeMagnetica, formas);
        } catch (RuntimeException erro) {
            throw new IOException("Arquivo JSON inválido: " + erro.getMessage(), erro);
        }
    }

    private FormaGeometrica reconstruir(FormaDados dados) {
        exigir(dados != null && dados.tipo != null && dados.pontos != null
                && dados.espessuraBorda != null, "Dados de uma forma estão incompletos.");
        List<Ponto> pontos = dados.pontos.stream().map(p -> {
            exigir(p != null && p.x != null && p.y != null, "Coordenada incompleta.");
            return new Ponto(p.x, p.y);
        }).toList();
        FormaGeometrica forma = switch (dados.tipo) {
            case "Poligono" -> new Poligono(pontos);
            case "Quadrado" -> Quadrado.deVertices(pontos);
            case "Hexagono" -> Hexagono.deVertices(pontos);
            case "Circulo" -> Circulo.dePontosControle(pontos);
            default -> throw new IllegalArgumentException("Tipo de forma desconhecido: " + dados.tipo);
        };
        forma.setCorPreenchimentoHex(dados.preenchimento);
        forma.setCorBordaHex(dados.borda);
        forma.setEspessuraBorda(dados.espessuraBorda);
        forma.setGroupId(dados.groupId);
        return forma;
    }

    private void exigir(boolean condicao, String mensagem) {
        if (!condicao) throw new IllegalArgumentException(mensagem);
    }
}
