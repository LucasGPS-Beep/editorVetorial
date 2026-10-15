package org.example.Models;

/** Retângulo alinhado aos eixos, inclusive para círculos transformados em elipses. */
public record Limites(double minX, double minY, double maxX, double maxY) {
    public static Limites entre(Ponto a, Ponto b) {
        return new Limites(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()),
                Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()));
    }

    public static Limites de(FormaGeometrica forma) {
        var pontos = forma.getPontos();
        if (pontos.isEmpty()) throw new IllegalArgumentException("A forma não tem pontos.");
        if (forma instanceof Circulo) {
            Ponto c = pontos.get(0);
            double rx = Math.hypot(pontos.get(1).getX() - c.getX(), pontos.get(2).getX() - c.getX());
            double ry = Math.hypot(pontos.get(1).getY() - c.getY(), pontos.get(2).getY() - c.getY());
            return new Limites(c.getX() - rx, c.getY() - ry, c.getX() + rx, c.getY() + ry);
        }
        return new Limites(pontos.stream().mapToDouble(Ponto::getX).min().orElseThrow(),
                pontos.stream().mapToDouble(Ponto::getY).min().orElseThrow(),
                pontos.stream().mapToDouble(Ponto::getX).max().orElseThrow(),
                pontos.stream().mapToDouble(Ponto::getY).max().orElseThrow());
    }

    public boolean contem(Limites outro) {
        return outro.minX >= minX && outro.maxX <= maxX && outro.minY >= minY && outro.maxY <= maxY;
    }
}
