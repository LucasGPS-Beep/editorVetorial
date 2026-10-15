package org.example.Models;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.UnaryOperator;

/** Estilo, encapsulamento e transformações compartilhados pelas formas. */
public abstract class FormaBase implements FormaGeometrica {
    protected final List<Ponto> pontos = new ArrayList<>();
    private String corPreenchimentoHex = "#D3D3D3";
    private String corBordaHex = "#000000";
    private double espessuraBorda = 1;
    private String groupId;

    /** Devolve uma lista imutável de cópias para proteger a geometria interna. */
    @Override
    public List<Ponto> getPontos() {
        return pontos.stream().map(Ponto::clonar).toList();
    }

    @Override public int getQuantidadePontos() { return pontos.size(); }
    @Override public String getCorPreenchimentoHex() { return corPreenchimentoHex; }
    @Override public String getCorBordaHex() { return corBordaHex; }
    @Override public double getEspessuraBorda() { return espessuraBorda; }
    @Override public String getGroupId() { return groupId; }

    @Override
    public void setCorPreenchimentoHex(String hex) {
        corPreenchimentoHex = validarCor(hex);
    }

    @Override
    public void setCorBordaHex(String hex) {
        corBordaHex = validarCor(hex);
    }

    private static String validarCor(String hex) {
        if (hex == null || !hex.matches("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?")) {
            throw new IllegalArgumentException("Use uma cor #RRGGBB ou #RRGGBBAA.");
        }
        return hex.toUpperCase(Locale.ROOT);
    }

    @Override
    public void setEspessuraBorda(double espessura) {
        Ponto.exigirFinito(espessura, "Espessura");
        if (espessura < 0) throw new IllegalArgumentException("A espessura não pode ser negativa.");
        espessuraBorda = espessura;
    }

    /** null representa uma forma sem grupo. */
    @Override
    public void setGroupId(String groupId) {
        if (groupId != null && groupId.isBlank()) {
            throw new IllegalArgumentException("O identificador do grupo não pode estar vazio.");
        }
        this.groupId = groupId;
    }

    @Override
    public void transladar(double dx, double dy) {
        Ponto.exigirFinito(dx, "dx");
        Ponto.exigirFinito(dy, "dy");
        transformar(p -> new Ponto(p.getX() + dx, p.getY() + dy));
    }

    @Override
    public void escalar(double fator) {
        exigirPositivo(fator, "Fator de escala");
        transformarNoCentro(fator, 0, 0, fator);
    }

    /** Ângulo em radianos, usando a convenção matemática das coordenadas. */
    @Override
    public void rotacionar(double anguloRadianos) {
        Ponto.exigirFinito(anguloRadianos, "Ângulo");
        double cos = Math.cos(anguloRadianos);
        double sin = Math.sin(anguloRadianos);
        transformarNoCentro(cos, -sin, sin, cos);
    }

    /** Cisalhamento simultâneo em x e y em torno do centroide. */
    @Override
    public void cisalhar(double shx, double shy) {
        Ponto.exigirFinito(shx, "shx");
        Ponto.exigirFinito(shy, "shy");
        double determinante = Ponto.exigirFinito(1 - shx * shy, "Determinante");
        if (determinante == 0) {
            throw new IllegalArgumentException("O cisalhamento não pode achatar a forma em uma linha.");
        }
        transformarNoCentro(1, shx, shy, 1);
    }

    private void transformarNoCentro(double a, double b, double c, double d) {
        if (pontos.isEmpty()) return;
        Ponto centro = getCentroide();
        transformar(p -> {
            double x = p.getX() - centro.getX();
            double y = p.getY() - centro.getY();
            return new Ponto(centro.getX() + a * x + b * y,
                    centro.getY() + c * x + d * y);
        });
    }

    /** Só substitui os pontos depois que todas as coordenadas forem validadas. */
    private void transformar(UnaryOperator<Ponto> operacao) {
        List<Ponto> novos = pontos.stream().map(operacao).toList();
        pontos.clear();
        pontos.addAll(novos);
    }

    protected final <T extends FormaBase> T copiarPara(T destino) {
        Objects.requireNonNull(destino);
        destino.pontos.clear();
        pontos.forEach(p -> destino.pontos.add(p.clonar()));
        destino.setCorPreenchimentoHex(corPreenchimentoHex);
        destino.setCorBordaHex(corBordaHex);
        destino.setEspessuraBorda(espessuraBorda);
        destino.setGroupId(groupId);
        return destino;
    }

    /** Restaura apenas coordenadas, mantendo a identidade usada pela seleção e camadas. */
    public final void restaurarGeometria(FormaGeometrica origem) {
        Objects.requireNonNull(origem);
        if (getClass() != origem.getClass()) throw new IllegalArgumentException("Tipos geométricos diferentes.");
        List<Ponto> copia = origem.getPontos();
        pontos.clear();
        copia.forEach(p -> pontos.add(p.clonar()));
    }

    protected static double exigirPositivo(double valor, String nome) {
        Ponto.exigirFinito(valor, nome);
        if (valor <= 0) throw new IllegalArgumentException(nome + " deve ser maior que zero.");
        return valor;
    }
}
