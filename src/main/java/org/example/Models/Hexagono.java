package org.example.Models;

/** O raio é a distância do centro até um vértice, igual ao lado do hexágono regular. */
public class Hexagono extends Poligono {
    public Hexagono(Ponto centro, double raio) {
        criarVerticesRegulares(centro, raio, 6, 0);
    }

    private Hexagono() { }

    public static Hexagono deVertices(java.util.List<Ponto> vertices) {
        if (vertices == null || vertices.size() != 6) throw new IllegalArgumentException("O hexágono precisa de seis vértices.");
        Hexagono forma = new Hexagono();
        vertices.forEach(forma::adicionarPonto);
        forma.validar();
        return forma;
    }

    @Override public Hexagono clonar() { return copiarPara(new Hexagono()); }
}
