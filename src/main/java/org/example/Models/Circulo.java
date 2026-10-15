package org.example.Models;

import java.util.Objects;

/**
 * Curva definida pelo centro e dois vetores de raio, armazenados como extremidades.
 * Essa representação também preserva a elipse resultante de um cisalhamento.
 */
public class Circulo extends FormaBase {
    public Circulo(Ponto centro, double raio) {
        Objects.requireNonNull(centro, "O centro é obrigatório.");
        exigirPositivo(raio, "Raio");
        pontos.add(centro.clonar());
        pontos.add(new Ponto(centro.getX() + raio, centro.getY()));
        pontos.add(new Ponto(centro.getX(), centro.getY() + raio));
        if (determinante() == 0) {
            throw new IllegalArgumentException("O raio é pequeno demais para as coordenadas do centro.");
        }
        if (!Double.isFinite(calcularArea()) || !Double.isFinite(calcularPerimetro())) {
            throw new IllegalArgumentException("O raio produz medidas grandes demais.");
        }
    }

    private Circulo() { }

    /** Reconstrói inclusive uma elipse cisalhada, preservando seus três controles. */
    public static Circulo dePontosControle(java.util.List<Ponto> controles) {
        if (controles == null || controles.size() != 3) {
            throw new IllegalArgumentException("O círculo precisa de três pontos de controle.");
        }
        Circulo forma = new Circulo();
        controles.forEach(p -> forma.pontos.add(Objects.requireNonNull(p).clonar()));
        if (!(forma.calcularArea() > 0) || !Double.isFinite(forma.calcularArea())
                || !Double.isFinite(forma.calcularPerimetro())) {
            throw new IllegalArgumentException("Controles do círculo são degenerados ou grandes demais.");
        }
        return forma;
    }

    @Override
    public void adicionarPonto(Ponto ponto) {
        throw new UnsupportedOperationException("O círculo é definido pelo centro e raio, sem adicionar vértices.");
    }

    @Override public Ponto getCentroide() { return pontos.get(0).clonar(); }

    private double ux() { return pontos.get(1).getX() - pontos.get(0).getX(); }
    private double uy() { return pontos.get(1).getY() - pontos.get(0).getY(); }
    private double vx() { return pontos.get(2).getX() - pontos.get(0).getX(); }
    private double vy() { return pontos.get(2).getY() - pontos.get(0).getY(); }
    private double determinante() { return ux() * vy() - uy() * vx(); }

    @Override public double calcularArea() { return Math.PI * Math.abs(determinante()); }

    /** Exato para círculos; aproximação de Ramanujan II para elipses cisalhadas. */
    @Override
    public double calcularPerimetro() {
        double aa = ux() * ux() + uy() * uy();
        double bb = vx() * vx() + vy() * vy();
        double ab = ux() * vx() + uy() * vy();
        double maior = Math.sqrt((aa + bb + Math.hypot(aa - bb, 2 * ab)) / 2);
        double menor = Math.abs(determinante()) / maior;
        double h = Math.pow((maior - menor) / (maior + menor), 2);
        return Math.PI * (maior + menor) * (1 + 3 * h / (10 + Math.sqrt(4 - 3 * h)));
    }

    @Override
    public boolean contemPonto(double px, double py) {
        Ponto.exigirFinito(px, "px");
        Ponto.exigirFinito(py, "py");
        double dx = px - pontos.get(0).getX();
        double dy = py - pontos.get(0).getY();
        double det = determinante();
        double a = (dx * vy() - dy * vx()) / det;
        double b = (ux() * dy - uy() * dx) / det;
        return a * a + b * b <= 1 + 1e-12;
    }

    @Override public Circulo clonar() { return copiarPara(new Circulo()); }
}
