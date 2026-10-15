package org.example.Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PoligonoTest {
    @Test
    void rejeitaGeometriaComMedidasNaoFinitas() {
        assertThrows(IllegalArgumentException.class, () -> new Poligono(List.of(
                new Ponto(0, 0), new Ponto(1e200, 0), new Ponto(0, 1e200))));
    }

    private static final double EPS = 1e-8;

    private Poligono triangulo() {
        return new Poligono(List.of(new Ponto(0, 0), new Ponto(6, 0), new Ponto(0, 3)));
    }

    @Test
    void calculaAreaPerimetroECentroideDoTriangulo() {
        Poligono forma = triangulo();
        assertEquals(9, forma.calcularArea(), EPS);
        assertEquals(9 + Math.sqrt(45), forma.calcularPerimetro(), EPS);
        assertPonto(2, 1, forma.getCentroide());
    }

    @Test
    void mantemAreaECentroideAoInverterOrdemDosVertices() {
        List<Ponto> vertices = new ArrayList<>(triangulo().getPontos());
        Collections.reverse(vertices);
        Poligono invertido = new Poligono(vertices);
        assertEquals(9, invertido.calcularArea(), EPS);
        assertPonto(2, 1, invertido.getCentroide());
    }

    @Test
    void calculaCentroideGeometricoEmVezDaMediaDosVertices() {
        Poligono trapezio = new Poligono(List.of(
                new Ponto(0, 0), new Ponto(4, 0), new Ponto(2, 2), new Ponto(0, 2)));
        assertEquals(6, trapezio.calcularArea(), EPS);
        assertPonto(14.0 / 9, 8.0 / 9, trapezio.getCentroide());
    }

    @Test
    void detectaInteriorBordasEConcavidade() {
        Poligono forma = new Poligono(List.of(new Ponto(0, 0), new Ponto(4, 0),
                new Ponto(4, 1), new Ponto(1, 1), new Ponto(1, 4), new Ponto(0, 4)));
        assertEquals(7, forma.calcularArea(), EPS);
        assertEquals(16, forma.calcularPerimetro(), EPS);
        assertTrue(forma.contemPonto(0.5, 3));
        assertTrue(forma.contemPonto(3, 0.5));
        assertTrue(forma.contemPonto(0, 0));
        assertTrue(forma.contemPonto(1, 3));
        assertFalse(forma.contemPonto(2, 2));
        assertFalse(forma.contemPonto(5, 0));
    }

    @Test
    void permiteConstrucaoIncrementalMasExigeTresPontosEAreaAoConcluir() {
        Poligono forma = new Poligono();
        assertEquals(0, forma.calcularArea());
        assertEquals(0, forma.calcularPerimetro());
        assertFalse(forma.contemPonto(0, 0));
        assertThrows(IllegalStateException.class, forma::getCentroide);
        forma.adicionarPonto(new Ponto(0, 0));
        forma.adicionarPonto(new Ponto(2, 0));
        assertThrows(IllegalArgumentException.class, forma::validar);
        assertPonto(1, 0, forma.getCentroide());
        forma.adicionarPonto(new Ponto(4, 0));
        assertThrows(IllegalArgumentException.class, forma::validar);
        forma.adicionarPonto(new Ponto(0, 2));
        assertDoesNotThrow(forma::validar);
    }

    @Test
    void protegePontosDeEntradaESaida() {
        Ponto entrada = new Ponto(0, 0);
        Poligono forma = new Poligono(List.of(entrada, new Ponto(6, 0), new Ponto(0, 3)));
        entrada.mover(50, 50);
        forma.getPontos().get(0).mover(90, 90);
        assertThrows(UnsupportedOperationException.class, () -> forma.getPontos().clear());
        assertPonto(0, 0, forma.getPontos().get(0));
        assertEquals(9, forma.calcularArea(), EPS);
    }

    @Test
    void editaVerticeEReverteEdicaoQueZeraArea() {
        Poligono forma = triangulo();
        forma.editarVertice(2, new Ponto(0, 6));
        assertEquals(18, forma.calcularArea(), EPS);
        assertThrows(IllegalArgumentException.class, () -> forma.editarVertice(2, new Ponto(3, 0)));
        assertPonto(0, 6, forma.getPontos().get(2));
        assertEquals(18, forma.calcularArea(), EPS);
    }

    @Test
    void transladarPreservaDimensoesEMoveCentroide() {
        Poligono forma = triangulo();
        forma.transladar(10, -4);
        assertEquals(9, forma.calcularArea(), EPS);
        assertPonto(12, -3, forma.getCentroide());
        assertTrue(forma.contemPonto(11, -3));
        assertFalse(forma.contemPonto(1, 1));
    }

    @Test
    void escalaEmTornoDoCentroide() {
        Poligono forma = triangulo();
        double perimetro = forma.calcularPerimetro();
        forma.escalar(2);
        assertEquals(36, forma.calcularArea(), EPS);
        assertEquals(2 * perimetro, forma.calcularPerimetro(), EPS);
        assertPonto(2, 1, forma.getCentroide());
        assertPonto(-2, -1, forma.getPontos().get(0));
    }

    @Test
    void rotacionaEmRadianosPreservandoCentroEArea() {
        Poligono forma = triangulo();
        forma.rotacionar(Math.PI / 2);
        assertPonto(3, -1, forma.getPontos().get(0));
        assertPonto(2, 1, forma.getCentroide());
        assertEquals(9, forma.calcularArea(), EPS);
    }

    @Test
    void cisalhamentoSegueMatrizESeuDeterminante() {
        Poligono forma = triangulo();
        forma.cisalhar(2, 0.25);
        assertEquals(4.5, forma.calcularArea(), EPS);
        assertPonto(-2, -0.5, forma.getPontos().get(0));
        assertPonto(2, 1, forma.getCentroide());
    }

    @Test
    void rejeitaTransformacoesInvalidasSemAlterarForma() {
        Poligono forma = triangulo();
        assertThrows(IllegalArgumentException.class, () -> forma.escalar(0));
        assertThrows(IllegalArgumentException.class, () -> forma.escalar(-2));
        assertThrows(IllegalArgumentException.class, () -> forma.rotacionar(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> forma.cisalhar(2, 0.5));
        assertThrows(IllegalArgumentException.class, () -> forma.transladar(0, Double.POSITIVE_INFINITY));
        assertPonto(0, 0, forma.getPontos().get(0));
        assertEquals(9, forma.calcularArea(), EPS);
    }

    private void assertPonto(double x, double y, Ponto ponto) {
        assertEquals(x, ponto.getX(), EPS);
        assertEquals(y, ponto.getY(), EPS);
    }
}
