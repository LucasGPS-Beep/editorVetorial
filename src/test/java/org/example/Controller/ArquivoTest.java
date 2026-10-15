package org.example.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.example.Models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

class ArquivoTest {
    @Test
    void abreExemploDistribuidoComQuatroTiposEGrupo() throws IOException {
        DocumentoVetorial exemplo = new ArquivoController().carregar(Path.of("exemplos/demonstracao.json"));
        var formas = exemplo.formas();
        assertEquals(4, formas.size());
        assertInstanceOf(Quadrado.class, formas.get(0));
        assertInstanceOf(Circulo.class, formas.get(1));
        assertInstanceOf(Hexagono.class, formas.get(2));
        assertInstanceOf(Poligono.class, formas.get(3));
        assertEquals("grupo-demo", formas.get(0).getGroupId());
        assertEquals("grupo-demo", formas.get(1).getGroupId());
    }

    @TempDir Path pasta;
    private final ArquivoController arquivos = new ArquivoController();

    @Test
    void roundTripPreservaTiposGeometriaEstilosGruposEOrdem() throws IOException {
        List<FormaGeometrica> formas = List.of(new Quadrado(new Ponto(100, 100), 30),
                new Hexagono(new Ponto(250, 100), 40), new Circulo(new Ponto(400, 100), 50),
                new Poligono(List.of(new Ponto(10, 200), new Ponto(100, 250), new Ponto(50, 350))));
        for (FormaGeometrica f : formas) {
            f.cisalhar(0.5, 0.25);
            f.rotacionar(0.3);
            f.setCorPreenchimentoHex("#11223380");
            f.setCorBordaHex("#AABBCC");
            f.setEspessuraBorda(2.5);
        }
        formas.get(0).setGroupId("grupo-teste");
        formas.get(2).setGroupId("grupo-teste");
        ((Poligono)formas.get(0)).editarVertice(0, new Ponto(130, 140));
        DocumentoVetorial original = new DocumentoVetorial(1200, 800, true, formas);
        Path arquivo = pasta.resolve("desenho com espaço.json");
        arquivos.salvar(arquivo, original);
        DocumentoVetorial lido = arquivos.carregar(arquivo);
        assertEquals(1200, lido.larguraCanvas());
        assertEquals(800, lido.alturaCanvas());
        assertTrue(lido.gradeMagnetica());
        assertTrue(HistoricoController.iguais(original.formas(), lido.formas()));
        String json = Files.readString(arquivo);
        assertTrue(json.contains("\"versao\": 1"));
        assertFalse(json.contains("org.example"));
        try (var lista = Files.list(pasta)) { assertEquals(1, lista.count()); }
    }

    @Test
    void documentoVazioETrocaDeArquivoExistenteFuncionam() throws IOException {
        Path arquivo = pasta.resolve("vazio.json");
        Files.writeString(arquivo, "conteúdo anterior");
        arquivos.salvar(arquivo, new DocumentoVetorial(800, 600, false, List.of()));
        assertTrue(arquivos.carregar(arquivo).formas().isEmpty());
        assertFalse(arquivos.carregar(arquivo).gradeMagnetica());
    }

    @Test
    void documentoESuasCopiasNaoCompartilhamGeometria() {
        Circulo forma = new Circulo(new Ponto(100, 100), 20);
        DocumentoVetorial documento = new DocumentoVetorial(800, 600, false, List.of(forma));
        forma.transladar(100, 0);
        documento.formas().get(0).transladar(500, 0);
        assertEquals(100, documento.formas().get(0).getCentroide().getX());
    }

    static Stream<String> arquivosInvalidos() {
        String forma = """
                {"tipo":"Poligono","pontos":[{"x":0,"y":0},{"x":10,"y":0},{"x":0,"y":10}],
                "preenchimento":"#FFFFFF","borda":"#000000","espessuraBorda":1}
                """;
        return Stream.of("{", "null", "{}", documento(forma) + " {}",
                documento(forma).replace("\"versao\":1", "\"versao\":2"),
                documento(forma).replace("\"largura\":800", "\"largura\":0"),
                documento(forma).replace("\"gradeMagnetica\":false", "\"outraCoisa\":false"),
                documento(forma.replace("Poligono", "ClasseDesconhecida")),
                documento(forma.replace("Poligono", "Quadrado")),
                documento(forma.replace("Poligono", "Hexagono")),
                documento(forma.replace("\"y\":10", "\"y\":0")),
                documento(forma.replace("\"x\":10", "\"x\":null")),
                documento(forma.replace("\"x\":10", "\"x\":1e999")),
                documento(forma.replace("#FFFFFF", "cor inválida")),
                documento(forma.replace("\"espessuraBorda\":1", "\"espessuraBorda\":-1")),
                documento(forma.replace("\"espessuraBorda\":1", "\"espessuraBorda\":null")),
                documento("null"),
                documento("{\"tipo\":\"Circulo\",\"pontos\":[{\"x\":0,\"y\":0},{\"x\":0,\"y\":0},{\"x\":0,\"y\":0}],\"preenchimento\":\"#FFFFFF\",\"borda\":\"#000000\",\"espessuraBorda\":1}"));
    }

    private static String documento(String forma) {
        return "{\"versao\":1,\"canvas\":{\"largura\":800,\"altura\":600,\"gradeMagnetica\":false},\"formas\":[" + forma + "]}";
    }

    @ParameterizedTest
    @MethodSource("arquivosInvalidos")
    void rejeitaArquivoInvalidoSemAlterarDocumentoAtual(String json) throws IOException {
        Path arquivo = pasta.resolve("invalido.json");
        Files.writeString(arquivo, json);
        EditorContext c = new EditorContext();
        c.adicionarForma(new Circulo(new Ponto(100, 100), 20));
        List<FormaGeometrica> antes = HistoricoController.copiar(c.getFormas());
        assertThrows(IOException.class, () -> c.carregarDocumento(arquivos.carregar(arquivo)));
        assertTrue(HistoricoController.iguais(antes, c.getFormas()));
        assertTrue(c.temAlteracoesNaoSalvas());
    }

    @Test
    void falhaDeEscritaNaoMarcaSalvoNemDeixaTemporarios() throws IOException {
        EditorContext c = new EditorContext();
        c.adicionarForma(new Circulo(new Ponto(100, 100), 20));
        Path destino = Files.createDirectory(pasta.resolve("diretorio.json"));
        Files.writeString(destino.resolve("preservar.txt"), "preservar");
        assertThrows(IOException.class, () -> arquivos.salvar(destino, c.exportarDocumento()));
        assertTrue(c.temAlteracoesNaoSalvas());
        assertEquals("preservar", Files.readString(destino.resolve("preservar.txt")));
        try (var lista = Files.list(pasta)) { assertEquals(1, lista.count()); }
    }
}
