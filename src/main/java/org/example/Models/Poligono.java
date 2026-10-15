package org.example.Models;

import java.util.List;
import java.util.Objects;

public class Poligono extends FormaBase {
    public Poligono() { }

    /** Constrói um polígono já concluído. */
    public Poligono(List<Ponto> vertices) {
        Objects.requireNonNull(vertices, "Os vértices são obrigatórios.");
        vertices.forEach(this::adicionarPonto);
        validar();
    }

    /** Permite montagem incremental por cliques; validar() deve ser chamado ao concluir. */
    @Override
    public void adicionarPonto(Ponto ponto) {
        pontos.add(Objects.requireNonNull(ponto, "O ponto é obrigatório.").clonar());
    }

    public void validar() {
        if (pontos.size() < 3) {
            throw new IllegalArgumentException("Um polígono concluído precisa de pelo menos três pontos.");
        }
        double area = calcularArea();
        if (!(area > 0) || !Double.isFinite(area) || !Double.isFinite(calcularPerimetro())) {
            throw new IllegalArgumentException("O polígono precisa ter área positiva e medidas finitas.");
        }
    }

    /** Edição explícita de um vértice, sem expor referências internas. */
    public void editarVertice(int indice, Ponto novoPonto) {
        Ponto copia = Objects.requireNonNull(novoPonto, "O ponto é obrigatório.").clonar();
        Ponto anterior = pontos.set(indice, copia);
        try {
            validar();
        } catch (IllegalArgumentException erro) {
            pontos.set(indice, anterior);
            throw erro;
        }
    }

    /** Soma de Gauss relativa ao primeiro vértice, reduzindo cancelamento numérico. */
    private double dobroAreaOrientada() {
        if (pontos.size() < 3) return 0;
        Ponto origem = pontos.get(0);
        double soma = 0;
        for (int i = 1; i < pontos.size() - 1; i++) {
            Ponto a = pontos.get(i);
            Ponto b = pontos.get(i + 1);
            soma += (a.getX() - origem.getX()) * (b.getY() - origem.getY())
                    - (b.getX() - origem.getX()) * (a.getY() - origem.getY());
        }
        return soma;
    }

    @Override public double calcularArea() { return Math.abs(dobroAreaOrientada()) / 2; }

    @Override
    public double calcularPerimetro() {
        if (pontos.size() < 3) return 0;
        double soma = 0;
        for (int i = 0; i < pontos.size(); i++) {
            soma += pontos.get(i).distancia(pontos.get((i + 1) % pontos.size()));
        }
        return soma;
    }

    @Override
    public Ponto getCentroide() {
        if (pontos.isEmpty()) throw new IllegalStateException("Não há pontos para calcular o centroide.");
        double areaDupla = dobroAreaOrientada();
        Ponto origem = pontos.get(0);
        if (areaDupla == 0) {
            // Durante o desenho, usa a média dos pontos disponíveis.
            double x = 0;
            double y = 0;
            for (Ponto ponto : pontos) {
                x += (ponto.getX() - origem.getX()) / pontos.size();
                y += (ponto.getY() - origem.getY()) / pontos.size();
            }
            return new Ponto(origem.getX() + x, origem.getY() + y);
        }
        double x = 0;
        double y = 0;
        for (int i = 0; i < pontos.size(); i++) {
            Ponto a = pontos.get(i);
            Ponto b = pontos.get((i + 1) % pontos.size());
            double ax = a.getX() - origem.getX();
            double ay = a.getY() - origem.getY();
            double bx = b.getX() - origem.getX();
            double by = b.getY() - origem.getY();
            double cruzado = ax * by - bx * ay;
            x += (ax + bx) * cruzado;
            y += (ay + by) * cruzado;
        }
        return new Ponto(origem.getX() + x / (3 * areaDupla),
                origem.getY() + y / (3 * areaDupla));
    }

    /** Ray casting para polígonos simples, incluindo os pontos da borda. */
    @Override
    public boolean contemPonto(double px, double py) {
        Ponto.exigirFinito(px, "px");
        Ponto.exigirFinito(py, "py");
        if (pontos.size() < 3 || calcularArea() == 0) return false;
        boolean dentro = false;
        for (int i = 0, j = pontos.size() - 1; i < pontos.size(); j = i++) {
            Ponto a = pontos.get(j);
            Ponto b = pontos.get(i);
            if (naBorda(px, py, a, b)) return true;
            if ((a.getY() > py) != (b.getY() > py)
                    && px < (b.getX() - a.getX()) * (py - a.getY())
                    / (b.getY() - a.getY()) + a.getX()) {
                dentro = !dentro;
            }
        }
        return dentro;
    }

    private boolean naBorda(double x, double y, Ponto a, Ponto b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double comprimento = Math.hypot(dx, dy);
        double tolerancia = 1e-9;
        if (comprimento == 0) return Math.hypot(x - a.getX(), y - a.getY()) <= tolerancia;
        double distancia = Math.abs((x - a.getX()) * dy - (y - a.getY()) * dx) / comprimento;
        return distancia <= tolerancia
                && x >= Math.min(a.getX(), b.getX()) - tolerancia
                && x <= Math.max(a.getX(), b.getX()) + tolerancia
                && y >= Math.min(a.getY(), b.getY()) - tolerancia
                && y <= Math.max(a.getY(), b.getY()) + tolerancia;
    }

    protected final void criarVerticesRegulares(Ponto centro, double raio, int lados, double anguloInicial) {
        Objects.requireNonNull(centro, "O centro é obrigatório.");
        exigirPositivo(raio, "Raio");
        for (int i = 0; i < lados; i++) {
            double angulo = anguloInicial + 2 * Math.PI * i / lados;
            adicionarPonto(new Ponto(centro.getX() + raio * Math.cos(angulo),
                    centro.getY() + raio * Math.sin(angulo)));
        }
        validar();
    }

    @Override public Poligono clonar() { return copiarPara(new Poligono()); }
}
