package org.example.Models;

import java.util.List;

/** Documento completo; a seleção e o histórico são estados de sessão e não são persistidos. */
public record DocumentoVetorial(double larguraCanvas, double alturaCanvas,
        boolean gradeMagnetica, List<FormaGeometrica> formas) {
    public DocumentoVetorial {
        if (!Double.isFinite(larguraCanvas) || !Double.isFinite(alturaCanvas)
                || larguraCanvas <= 0 || alturaCanvas <= 0) {
            throw new IllegalArgumentException("O canvas precisa de dimensões finitas e positivas.");
        }
        formas = formas.stream().map(f -> {
            if (!(f instanceof Poligono) && !(f instanceof Circulo)) {
                throw new IllegalArgumentException("Tipo de forma não suportado no documento.");
            }
            if (f instanceof Poligono p) p.validar();
            if (!(f.calcularArea() > 0) || !Double.isFinite(f.calcularArea())
                    || !Double.isFinite(f.calcularPerimetro())) {
                throw new IllegalArgumentException("O documento contém uma forma degenerada ou grande demais.");
            }
            return f.clonar();
        }).toList();
    }

    @Override public List<FormaGeometrica> formas() { return formas.stream().map(FormaGeometrica::clonar).toList(); }
}
