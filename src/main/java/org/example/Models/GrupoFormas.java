package org.example.Models;

import java.util.List;

/** Composite temporário: mantém referências às formas de um mesmo grupo lógico. */
public final class GrupoFormas implements FormaGeometrica {
    private final List<FormaGeometrica> membros;

    public GrupoFormas(List<FormaGeometrica> membros) {
        if (membros.isEmpty()) throw new IllegalArgumentException("Um grupo precisa de formas.");
        this.membros = List.copyOf(membros);
    }

    @Override public List<Ponto> getPontos() { return membros.stream().flatMap(f -> f.getPontos().stream()).toList(); }
    @Override public int getQuantidadePontos() { return membros.stream().mapToInt(FormaGeometrica::getQuantidadePontos).sum(); }
    @Override public void adicionarPonto(Ponto ponto) { throw new UnsupportedOperationException("Adicione pontos às formas do grupo."); }
    @Override public String getCorPreenchimentoHex() { return membros.get(0).getCorPreenchimentoHex(); }
    @Override public String getCorBordaHex() { return membros.get(0).getCorBordaHex(); }
    @Override public double getEspessuraBorda() { return membros.get(0).getEspessuraBorda(); }
    @Override public String getGroupId() { return membros.get(0).getGroupId(); }
    @Override public void setCorPreenchimentoHex(String cor) { membros.forEach(f -> f.setCorPreenchimentoHex(cor)); }
    @Override public void setCorBordaHex(String cor) { membros.forEach(f -> f.setCorBordaHex(cor)); }
    @Override public void setEspessuraBorda(double espessura) { membros.forEach(f -> f.setEspessuraBorda(espessura)); }
    @Override public void setGroupId(String id) { membros.forEach(f -> f.setGroupId(id)); }
    @Override public double calcularArea() { return membros.stream().mapToDouble(FormaGeometrica::calcularArea).sum(); }
    @Override public double calcularPerimetro() { return membros.stream().mapToDouble(FormaGeometrica::calcularPerimetro).sum(); }
    @Override public boolean contemPonto(double x, double y) { return membros.stream().anyMatch(f -> f.contemPonto(x, y)); }

    /** Centro ponderado pelas áreas individuais, contando sobreposições separadamente. */
    @Override
    public Ponto getCentroide() {
        double area = calcularArea();
        if (!(area > 0) || !Double.isFinite(area)) throw new IllegalArgumentException("O grupo precisa ter área finita e positiva.");
        double x = 0;
        double y = 0;
        for (FormaGeometrica f : membros) {
            double peso = f.calcularArea() / area;
            Ponto centro = f.getCentroide();
            x += centro.getX() * peso;
            y += centro.getY() * peso;
        }
        return new Ponto(x, y);
    }

    @Override public void transladar(double dx, double dy) { membros.forEach(f -> f.transladar(dx, dy)); }

    @Override
    public void escalar(double fator) {
        Ponto centro = getCentroide();
        for (FormaGeometrica f : membros) {
            Ponto antes = f.getCentroide();
            f.escalar(fator);
            f.transladar((antes.getX() - centro.getX()) * (fator - 1),
                    (antes.getY() - centro.getY()) * (fator - 1));
        }
    }

    @Override
    public void rotacionar(double angulo) {
        Ponto centro = getCentroide();
        double cos = Math.cos(angulo), sin = Math.sin(angulo);
        for (FormaGeometrica f : membros) {
            Ponto antes = f.getCentroide();
            double x = antes.getX() - centro.getX(), y = antes.getY() - centro.getY();
            f.rotacionar(angulo);
            f.transladar(x * cos - y * sin - x, x * sin + y * cos - y);
        }
    }

    @Override
    public void cisalhar(double shx, double shy) {
        Ponto centro = getCentroide();
        for (FormaGeometrica f : membros) {
            Ponto antes = f.getCentroide();
            f.cisalhar(shx, shy);
            f.transladar(shx * (antes.getY() - centro.getY()), shy * (antes.getX() - centro.getX()));
        }
    }

    @Override public GrupoFormas clonar() { return new GrupoFormas(membros.stream().map(FormaGeometrica::clonar).toList()); }
}
