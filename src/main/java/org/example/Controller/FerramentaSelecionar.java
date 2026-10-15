package org.example.Controller;

import java.util.List;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.Limites;
import org.example.Models.Ponto;

public class FerramentaSelecionar implements Ferramenta {
    private Ponto ultimoPonto;
    private Ponto inicioCaixa;
    private List<FormaGeometrica> selecaoAnterior = List.of();
    private boolean adicionarCaixa;

    @Override
    public void aoPressionarMouse(MouseEvent evento, EditorContext contexto) {
        if (evento.getButton() != MouseButton.PRIMARY) return;
        ultimoPonto = null;
        inicioCaixa = null;
        FormaGeometrica forma = contexto.encontrarForma(evento.getX(), evento.getY());
        if (forma == null) {
            selecaoAnterior = contexto.getSelecionados();
            adicionarCaixa = evento.isShiftDown();
            inicioCaixa = new Ponto(evento.getX(), evento.getY());
            if (!evento.isShiftDown()) contexto.selecionar(null, false);
            return;
        }
        if (evento.isShiftDown() || !contexto.getSelecionados().contains(forma)) {
            contexto.selecionar(forma, evento.isShiftDown());
        }
        if (contexto.getSelecionados().contains(forma)) {
            ultimoPonto = contexto.pontoDoMouse(evento.getX(), evento.getY());
            contexto.iniciarEdicao();
        }
    }

    @Override
    public void aoArrastarMouse(MouseEvent evento, EditorContext contexto) {
        if (!evento.isPrimaryButtonDown()) return;
        if (inicioCaixa != null) {
            contexto.setCaixaSelecao(Limites.entre(inicioCaixa, new Ponto(evento.getX(), evento.getY())));
        } else if (ultimoPonto != null) {
            Ponto atual = contexto.pontoDoMouse(evento.getX(), evento.getY());
            contexto.moverSelecionados(atual.getX() - ultimoPonto.getX(), atual.getY() - ultimoPonto.getY());
            ultimoPonto = atual;
        }
    }

    @Override
    public void aoSoltarMouse(MouseEvent evento, EditorContext contexto) {
        if (evento.getButton() != MouseButton.PRIMARY) return;
        if (inicioCaixa != null && contexto.getCaixaSelecao() != null) {
            Limites caixa = Limites.entre(inicioCaixa, new Ponto(evento.getX(), evento.getY()));
            contexto.selecionarCaixa(caixa, adicionarCaixa ? selecaoAnterior : List.of());
        }
        if (ultimoPonto != null) {
            Ponto finalMouse = contexto.pontoDoMouse(evento.getX(), evento.getY());
            contexto.moverSelecionados(finalMouse.getX() - ultimoPonto.getX(), finalMouse.getY() - ultimoPonto.getY());
            contexto.confirmarEdicao();
        }
        inicioCaixa = null;
        ultimoPonto = null;
        contexto.setCaixaSelecao(null);
    }

    @Override
    public boolean temEdicaoEmAndamento() { return inicioCaixa != null || ultimoPonto != null; }

    @Override
    public void cancelar(EditorContext contexto) {
        if (inicioCaixa != null) contexto.selecionarLista(selecaoAnterior);
        if (ultimoPonto != null) contexto.cancelarEdicao();
        inicioCaixa = null;
        ultimoPonto = null;
        contexto.setCaixaSelecao(null);
        Ferramenta.super.cancelar(contexto);
    }
}
