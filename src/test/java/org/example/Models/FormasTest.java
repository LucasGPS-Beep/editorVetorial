package org.example.Models;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

class FormasTest {
    @Test
    void rejeitaCirculoComMedidasNaoFinitas() {
        assertThrows(IllegalArgumentException.class, () -> new Circulo(new Ponto(0, 0), 1e200));
    }

    private static final double EPS = 1e-8;

    static Stream<FormaGeometrica> formas() {
        return Stream.of(new Quadrado(new Ponto(5, 8), 3),
                new Hexagono(new Ponto(5, 8), 3), new Circulo(new Ponto(5, 8), 3),
                new Poligono(List.of(new Ponto(0, 0), new Ponto(4, 0), new Ponto(0, 2))));
    }

    @Test
    void quadradoCalculaVerticesAreaEPerimetroPeloRaioCircunscrito() {
        Quadrado forma = new Quadrado(new Ponto(5, 8), 3);
        assertEquals(4, forma.getQuantidadePontos());
        assertEquals(18, forma.calcularArea(), EPS);
        assertEquals(12 * Math.sqrt(2), forma.calcularPerimetro(), EPS);
        assertEquals(5, forma.getCentroide().getX(), EPS);
        assertEquals(8, forma.getCentroide().getY(), EPS);
        forma.getPontos().forEach(p -> assertEquals(3, p.distancia(new Ponto(5, 8)), EPS));
    }

    @Test
    void hexagonoCalculaVerticesAreaEPerimetro() {
        Hexagono forma = new Hexagono(new Ponto(0, 0), 2);
        assertEquals(6, forma.getQuantidadePontos());
        assertEquals(6 * Math.sqrt(3), forma.calcularArea(), EPS);
        assertEquals(12, forma.calcularPerimetro(), EPS);
        assertTrue(forma.contemPonto(0, 0));
        assertFalse(forma.contemPonto(2, 2));
    }

    @Test
    void circuloCalculaMedidasESelecionaInteriorEBorda() {
        Circulo forma = new Circulo(new Ponto(3, 4), 2);
        assertEquals(4 * Math.PI, forma.calcularArea(), EPS);
        assertEquals(4 * Math.PI, forma.calcularPerimetro(), EPS);
        assertTrue(forma.contemPonto(3, 4));
        assertTrue(forma.contemPonto(5, 4));
        assertFalse(forma.contemPonto(5.01, 4));
        assertThrows(UnsupportedOperationException.class, () -> forma.adicionarPonto(new Ponto(0, 0)));
    }

    @Test
    void circuloSuportaEscalaRotacaoETranslacao() {
        Circulo forma = new Circulo(new Ponto(3, 4), 2);
        forma.escalar(3);
        forma.rotacionar(Math.PI / 3);
        forma.transladar(-3, -4);
        assertEquals(36 * Math.PI, forma.calcularArea(), EPS);
        assertEquals(12 * Math.PI, forma.calcularPerimetro(), EPS);
        assertEquals(0, forma.getCentroide().getX(), EPS);
        assertEquals(0, forma.getCentroide().getY(), EPS);
        assertTrue(forma.contemPonto(6, 0));
        assertFalse(forma.contemPonto(6.1, 0));
    }

    @Test
    void circuloCisalhadoPreservaCurvaAreaESelecao() {
        Circulo forma = new Circulo(new Ponto(0, 0), 4);
        forma.cisalhar(0.5, 0);
        assertEquals(16 * Math.PI, forma.calcularArea(), EPS);
        assertTrue(forma.calcularPerimetro() > 8 * Math.PI);
        assertTrue(forma.contemPonto(2, 4));
        assertTrue(forma.contemPonto(0, 0));
        assertFalse(forma.contemPonto(3, 4));
        forma.cisalhar(-0.5, 0);
        assertEquals(8 * Math.PI, forma.calcularPerimetro(), EPS);
        assertTrue(forma.contemPonto(0, 4));
    }

    @ParameterizedTest
    @MethodSource("formas")
    void clonarPreservaTipoEstiloEGeometriaSemCompartilharPontos(FormaGeometrica original) {
        original.setCorPreenchimentoHex("#aabbcc80");
        original.setCorBordaHex("#123456");
        original.setEspessuraBorda(3.5);
        original.setGroupId("grupo-1");
        original.rotacionar(0.4);
        original.cisalhar(0.3, 0.1);
        FormaGeometrica copia = original.clonar();
        double area = original.calcularArea();
        Ponto centro = original.getCentroide();
        assertNotSame(original, copia);
        assertEquals(original.getClass(), copia.getClass());
        assertEquals(area, copia.calcularArea(), EPS);
        assertEquals(original.calcularPerimetro(), copia.calcularPerimetro(), EPS);
        assertEquals("#AABBCC80", copia.getCorPreenchimentoHex());
        assertEquals("#123456", copia.getCorBordaHex());
        assertEquals(3.5, copia.getEspessuraBorda());
        assertEquals("grupo-1", copia.getGroupId());
        copia.transladar(100, 100);
        copia.escalar(2);
        copia.setGroupId(null);
        copia.setCorPreenchimentoHex("#000000");
        assertEquals(area, original.calcularArea(), EPS);
        assertEquals(centro.getX(), original.getCentroide().getX(), EPS);
        assertEquals(centro.getY(), original.getCentroide().getY(), EPS);
        assertEquals("grupo-1", original.getGroupId());
        assertEquals("#AABBCC80", original.getCorPreenchimentoHex());
        assertEquals(4 * area, copia.calcularArea(), EPS);
    }

    @ParameterizedTest
    @MethodSource("formas")
    void validaEstilosEEntradas(FormaGeometrica forma) {
        assertThrows(IllegalArgumentException.class, () -> forma.setCorBordaHex("vermelho"));
        assertThrows(IllegalArgumentException.class, () -> forma.setCorPreenchimentoHex(null));
        assertThrows(IllegalArgumentException.class, () -> forma.setEspessuraBorda(-1));
        assertThrows(IllegalArgumentException.class, () -> forma.setEspessuraBorda(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> forma.setGroupId(" "));
        assertThrows(IllegalArgumentException.class, () -> forma.contemPonto(Double.NaN, 0));
        forma.setEspessuraBorda(0);
        assertEquals(0, forma.getEspessuraBorda());
    }

    @Test
    void rejeitaRaiosInvalidos() {
        for (double raio : new double[] {0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> new Circulo(new Ponto(0, 0), raio));
            assertThrows(IllegalArgumentException.class, () -> new Quadrado(new Ponto(0, 0), raio));
            assertThrows(IllegalArgumentException.class, () -> new Hexagono(new Ponto(0, 0), raio));
        }
    }
}
