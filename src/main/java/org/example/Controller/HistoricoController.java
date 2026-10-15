package org.example.Controller;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import org.example.Models.FormaGeometrica;

/** Memento: cada estado contém cópias profundas e independentes das formas. */
public final class HistoricoController {
    private final Deque<List<FormaGeometrica>> undoStack = new ArrayDeque<>();
    private final Deque<List<FormaGeometrica>> redoStack = new ArrayDeque<>();

    public static List<FormaGeometrica> copiar(List<FormaGeometrica> formas) {
        return formas.stream().map(FormaGeometrica::clonar).toList();
    }

    public void registrarEstado(List<FormaGeometrica> formas) {
        undoStack.push(copiar(formas));
        redoStack.clear();
    }

    public List<FormaGeometrica> desfazer(List<FormaGeometrica> atual) {
        if (undoStack.isEmpty()) return copiar(atual);
        redoStack.push(copiar(atual));
        return copiar(undoStack.pop());
    }

    public List<FormaGeometrica> refazer(List<FormaGeometrica> atual) {
        if (redoStack.isEmpty()) return copiar(atual);
        undoStack.push(copiar(atual));
        return copiar(redoStack.pop());
    }

    public boolean podeDesfazer() { return !undoStack.isEmpty(); }
    public boolean podeRefazer() { return !redoStack.isEmpty(); }
    public void limpar() { undoStack.clear(); redoStack.clear(); }

    /** Compara conteúdo e ordem, sem depender da identidade das instâncias. */
    public static boolean iguais(List<FormaGeometrica> a, List<FormaGeometrica> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            FormaGeometrica x = a.get(i), y = b.get(i);
            if (x.getClass() != y.getClass()
                    || !Objects.equals(x.getGroupId(), y.getGroupId())
                    || !x.getCorPreenchimentoHex().equals(y.getCorPreenchimentoHex())
                    || !x.getCorBordaHex().equals(y.getCorBordaHex())
                    || Double.compare(x.getEspessuraBorda(), y.getEspessuraBorda()) != 0) return false;
            var px = x.getPontos();
            var py = y.getPontos();
            if (px.size() != py.size()) return false;
            for (int j = 0; j < px.size(); j++) {
                if (Double.compare(px.get(j).getX(), py.get(j).getX()) != 0
                        || Double.compare(px.get(j).getY(), py.get(j).getY()) != 0) return false;
            }
        }
        return true;
    }
}
