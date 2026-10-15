package org.example.Models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PontoTest {
    @Test
    void calculaDistanciaEMoveSemAlterarClone() {
        Ponto ponto = new Ponto(1, 2);
        Ponto copia = ponto.clonar();
        assertEquals(5, ponto.distancia(new Ponto(4, 6)), 1e-9);
        ponto.mover(3, -2);
        assertEquals(4, ponto.getX());
        assertEquals(0, ponto.getY());
        assertEquals(1, copia.getX());
        assertEquals(2, copia.getY());
    }

    @Test
    void rejeitaCoordenadasInvalidasSemMoverParcialmente() {
        assertThrows(IllegalArgumentException.class, () -> new Ponto(Double.NaN, 0));
        assertThrows(IllegalArgumentException.class, () -> new Ponto(0, Double.POSITIVE_INFINITY));
        Ponto ponto = new Ponto(1, Double.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> ponto.mover(5, Double.MAX_VALUE));
        assertEquals(1, ponto.getX());
        assertEquals(Double.MAX_VALUE, ponto.getY());
    }
}
