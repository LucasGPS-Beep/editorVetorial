package org.example.Controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javafx.event.EventType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;

class FerramentasTest {
    private MouseEvent evento(EventType<MouseEvent> tipo, double x, double y, boolean shift, int cliques) {
        return new MouseEvent(tipo, x, y, x, y, MouseButton.PRIMARY, cliques,
                shift, false, false, false,
                tipo == MouseEvent.MOUSE_PRESSED || tipo == MouseEvent.MOUSE_DRAGGED,
                false, false, false, false, true, null);
    }

    private void pressionar(EditorContext c, double x, double y, boolean shift) {
        c.getFerramentaAtual().aoPressionarMouse(evento(MouseEvent.MOUSE_PRESSED, x, y, shift, 1), c);
    }

    private void arrastar(EditorContext c, double x, double y) {
        c.getFerramentaAtual().aoArrastarMouse(evento(MouseEvent.MOUSE_DRAGGED, x, y, false, 1), c);
    }

    private void soltar(EditorContext c, double x, double y) {
        c.getFerramentaAtual().aoSoltarMouse(evento(MouseEvent.MOUSE_RELEASED, x, y, false, 1), c);
    }

    private void clicar(EditorContext c, double x, double y, int cliques) {
        c.getFerramentaAtual().aoClicar(evento(MouseEvent.MOUSE_CLICKED, x, y, false, cliques), c);
    }

    @ParameterizedTest
    @EnumSource(value = ModoFerramenta.class, names = {"CIRCULO", "QUADRADO", "HEXAGONO"})
    void insereFormaPorArrastoComPreviaSemGravarAntesDeSoltar(ModoFerramenta modo) {
        EditorContext c = new EditorContext();
        c.definirEstilo("#112233", "#445566", 3);
        c.setModo(modo);
        pressionar(c, 100, 100, false);
        arrastar(c, 140, 100);
        assertTrue(c.getFormas().isEmpty());
        assertNotNull(c.getFormaEmAndamento());
        soltar(c, 150, 100);
        assertEquals(1, c.getFormas().size());
        FormaGeometrica forma = c.getFormas().get(0);
        assertEquals(100, forma.getCentroide().getX(), 1e-8);
        assertEquals(100, forma.getCentroide().getY(), 1e-8);
        double area = switch (modo) {
            case CIRCULO -> Math.PI * 2500;
            case QUADRADO -> 5000;
            case HEXAGONO -> 3 * Math.sqrt(3) * 2500 / 2;
            default -> throw new AssertionError();
        };
        assertEquals(area, forma.calcularArea(), 1e-8);
        assertEquals("#112233", forma.getCorPreenchimentoHex());
        assertEquals("#445566", forma.getCorBordaHex());
        assertEquals(3, forma.getEspessuraBorda());
        assertEquals(List.of(forma), c.getSelecionados());
        assertNull(c.getFormaEmAndamento());
    }

    @Test
    void cliqueSemArrastoECancelamentoNaoCriamForma() {
        EditorContext c = new EditorContext();
        c.setModo(ModoFerramenta.CIRCULO);
        pressionar(c, 10, 10, false);
        soltar(c, 10, 10);
        assertTrue(c.getFormas().isEmpty());
        pressionar(c, 10, 10, false);
        arrastar(c, 60, 10);
        c.cancelarDesenho();
        soltar(c, 60, 10);
        assertTrue(c.getFormas().isEmpty());
        assertNull(c.getFormaEmAndamento());
    }

    @Test
    void poligonoInvalidoMantemRascunhoParaCorrecao() {
        EditorContext c = new EditorContext();
        AtomicReference<String> erro = new AtomicReference<>();
        c.setAoErro(erro::set);
        c.setModo(ModoFerramenta.POLIGONO);
        clicar(c, 10, 10, 1);
        clicar(c, 110, 10, 1);
        c.concluirDesenho();
        assertNotNull(erro.get());
        assertTrue(c.getFormas().isEmpty());
        assertEquals(2, c.getFormaEmAndamento().getQuantidadePontos());
        clicar(c, 10, 110, 1);
        c.concluirDesenho();
        assertEquals(1, c.getFormas().size());
        assertEquals(5000, c.getFormas().get(0).calcularArea(), 1e-8);
        assertNull(c.getFormaEmAndamento());
    }

    @Test
    void duploCliqueConcluiSemRepetirUltimoVertice() {
        EditorContext c = new EditorContext();
        c.setModo(ModoFerramenta.POLIGONO);
        clicar(c, 10, 10, 1);
        clicar(c, 110, 10, 1);
        clicar(c, 10, 110, 1);
        clicar(c, 10, 110, 2);
        assertEquals(3, c.getFormas().get(0).getQuantidadePontos());
    }

    @Test
    void trocaDeFerramentaDescartaRascunho() {
        EditorContext c = new EditorContext();
        c.setModo(ModoFerramenta.POLIGONO);
        clicar(c, 10, 10, 1);
        c.setModo(ModoFerramenta.CIRCULO);
        assertNull(c.getFormaEmAndamento());
        assertTrue(c.getFormas().isEmpty());
        assertInstanceOf(FerramentaInserirForma.class, c.getFerramentaAtual());
        c.setModo(ModoFerramenta.POLIGONO);
        clicar(c, 30, 30, 1);
        assertEquals(1, c.getFormaEmAndamento().getQuantidadePontos());
    }

    @Test
    void snapAfetaCriacaoECoordenadasMasPodeSerDesligado() {
        EditorContext c = new EditorContext();
        c.setGridSnappingHabilitado(true);
        c.atualizarMouse(23, 38);
        assertEquals(20, c.getMouseX());
        assertEquals(40, c.getMouseY());
        c.setModo(ModoFerramenta.CIRCULO);
        pressionar(c, 23, 38, false);
        soltar(c, 63, 38);
        assertEquals(20, c.getFormas().get(0).getCentroide().getX());
        assertEquals(40, c.getFormas().get(0).getCentroide().getY());
        assertEquals(1600 * Math.PI, c.getFormas().get(0).calcularArea(), 1e-8);
        c.setGridSnappingHabilitado(false);
        assertEquals(23, c.aplicarSnap(23));
    }

    @Test
    void selecionaFormaDaFrenteEMoveSomenteSelecao() {
        EditorContext c = new EditorContext();
        Circulo fundo = new Circulo(new Ponto(100, 100), 60);
        Circulo frente = new Circulo(new Ponto(100, 100), 40);
        c.adicionarForma(fundo);
        c.adicionarForma(frente);
        c.selecionar(null, false);
        pressionar(c, 100, 100, false);
        arrastar(c, 120, 130);
        soltar(c, 120, 130);
        assertEquals(List.of(frente), c.getSelecionados());
        assertEquals(120, frente.getCentroide().getX());
        assertEquals(130, frente.getCentroide().getY());
        assertEquals(100, fundo.getCentroide().getX());
        pressionar(c, 400, 400, false);
        assertTrue(c.getSelecionados().isEmpty());
    }

    @Test
    void shiftSelecionaMultiplasEMovimentaConjunto() {
        EditorContext c = new EditorContext();
        Circulo a = new Circulo(new Ponto(100, 100), 20);
        Circulo b = new Circulo(new Ponto(200, 100), 20);
        c.adicionarForma(a);
        c.adicionarForma(b);
        pressionar(c, 100, 100, false);
        pressionar(c, 200, 100, true);
        assertEquals(2, c.getSelecionados().size());
        arrastar(c, 210, 120);
        soltar(c, 210, 120);
        assertEquals(110, a.getCentroide().getX());
        assertEquals(210, b.getCentroide().getX());
        assertEquals(120, a.getCentroide().getY());
        pressionar(c, 210, 120, true);
        assertEquals(List.of(a), c.getSelecionados());
    }

    @Test
    void camadasMudamPinturaEAlvoDoClique() {
        EditorContext c = new EditorContext();
        Circulo a = new Circulo(new Ponto(100, 100), 40);
        Circulo b = new Circulo(new Ponto(100, 100), 40);
        c.adicionarForma(a);
        c.adicionarForma(b);
        assertSame(b, c.encontrarForma(100, 100));
        c.enviarParaTras();
        assertSame(a, c.encontrarForma(100, 100));
        c.trazerParaFrente();
        assertSame(b, c.encontrarForma(100, 100));
        c.excluirSelecionados();
        assertEquals(List.of(a), c.getFormas());
        assertTrue(c.getSelecionados().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> c.getFormas().clear());
    }

    @Test
    void estiloDaSelecaoAtualizaControlesESomenteFormasSelecionadas() {
        EditorContext c = new EditorContext();
        Circulo a = new Circulo(new Ponto(100, 100), 40);
        Circulo b = new Circulo(new Ponto(200, 100), 40);
        a.setCorBordaHex("#123456");
        c.adicionarForma(a);
        c.adicionarForma(b);
        c.selecionar(a, false);
        assertEquals("#123456", c.getCorBordaHex());
        c.definirEstilo("#ABCDEF", "#654321", 4);
        assertEquals("#ABCDEF", a.getCorPreenchimentoHex());
        assertEquals("#D3D3D3", b.getCorPreenchimentoHex());
        assertThrows(IllegalArgumentException.class, () -> c.definirEstilo("#000000", "inválida", 2));
        assertEquals("#ABCDEF", a.getCorPreenchimentoHex());
        assertEquals("#ABCDEF", c.getCorPreenchimentoHex());
    }
}
