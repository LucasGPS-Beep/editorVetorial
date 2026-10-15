package org.example.Models;

import java.util.Objects;

/** Coordenada 2D mutável. Não aceita NaN ou infinito. */
public final class Ponto {
    private double x;
    private double y;

    public Ponto(double x, double y) {
        this.x = exigirFinito(x, "x");
        this.y = exigirFinito(y, "y");
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = exigirFinito(x, "x"); }
    public void setY(double y) { this.y = exigirFinito(y, "y"); }

    public void mover(double dx, double dy) {
        double novoX = exigirFinito(x + exigirFinito(dx, "dx"), "x resultante");
        double novoY = exigirFinito(y + exigirFinito(dy, "dy"), "y resultante");
        x = novoX;
        y = novoY;
    }

    public double distancia(Ponto outro) {
        Objects.requireNonNull(outro, "O outro ponto é obrigatório.");
        return Math.hypot(x - outro.x, y - outro.y);
    }

    public Ponto clonar() { return new Ponto(x, y); }

    static double exigirFinito(double valor, String nome) {
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException(nome + " deve ser finito.");
        }
        return valor;
    }
}
