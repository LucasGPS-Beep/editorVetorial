package org.example.Models;

/** O raio é a distância do centro até um vértice (metade da diagonal). */
public class Quadrado extends Poligono {
    public Quadrado(Ponto centro, double raio) {
        criarVerticesRegulares(centro, raio, 4, Math.PI / 4);
    }

    private Quadrado() { }

    public static Quadrado deVertices(java.util.List<Ponto> vertices) {
        if (vertices == null || vertices.size() != 4) throw new IllegalArgumentException("O quadrado precisa de quatro vértices.");
        Quadrado forma = new Quadrado();
        vertices.forEach(forma::adicionarPonto);
        forma.validar();
        return forma;
    }

    @Override public Quadrado clonar() { return copiarPara(new Quadrado()); }
}
