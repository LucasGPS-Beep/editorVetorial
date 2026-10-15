package org.example.Controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.Poligono;
import org.example.Models.Ponto;

public class FerramentaDesenhar implements Ferramenta {
    private Poligono poligono;

    @Override
    public void aoClicar(MouseEvent evento, EditorContext contexto) {
        if (evento.getButton() == MouseButton.SECONDARY) {
            concluir(contexto);
            return;
        }
        if (evento.getButton() != MouseButton.PRIMARY) return;
        if (evento.getClickCount() >= 2) {
            concluir(contexto);
            return;
        }
        if (poligono == null) {
            poligono = new Poligono();
            contexto.aplicarEstilo(poligono);
        }
        Ponto ponto = contexto.pontoDoMouse(evento.getX(), evento.getY());
        if (poligono.getQuantidadePontos() == 0
                || poligono.getPontos().get(poligono.getQuantidadePontos() - 1).distancia(ponto) > 0) {
            poligono.adicionarPonto(ponto);
        }
        contexto.setFormaEmAndamento(poligono);
    }

    @Override
    public void concluir(EditorContext contexto) {
        if (poligono == null) return;
        try {
            poligono.validar();
        } catch (IllegalArgumentException erro) {
            contexto.informarErro(erro.getMessage());
            return;
        }
        Poligono concluido = poligono;
        poligono = null;
        contexto.adicionarForma(concluido);
    }

    @Override
    public void cancelar(EditorContext contexto) {
        poligono = null;
        Ferramenta.super.cancelar(contexto);
    }
}
