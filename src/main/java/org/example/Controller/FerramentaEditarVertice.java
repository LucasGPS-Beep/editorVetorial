package org.example.Controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.Poligono;
import org.example.Models.Ponto;

public class FerramentaEditarVertice implements Ferramenta {
    private Poligono forma;
    private Poligono estadoInicial;
    private int indice;
    private String ultimoErro;

    @Override
    public void aoPressionarMouse(MouseEvent evento, EditorContext contexto) {
        if (evento.getButton() != MouseButton.PRIMARY) return;
        double menor = 9;
        Poligono candidata = null;
        int vertice = -1;
        var formas = contexto.getFormas();
        Ponto cursor = new Ponto(evento.getX(), evento.getY());
        for (int i = formas.size() - 1; i >= 0; i--) {
            if (formas.get(i) instanceof Poligono poligono) {
                var pontos = poligono.getPontos();
                for (int j = 0; j < pontos.size(); j++) {
                    double distancia = cursor.distancia(pontos.get(j));
                    if (distancia < menor) {
                        menor = distancia;
                        candidata = poligono;
                        vertice = j;
                    }
                }
            }
        }
        if (candidata == null) {
            contexto.selecionar(contexto.encontrarForma(evento.getX(), evento.getY()), false);
            return;
        }
        if (!contexto.getSelecionados().contains(candidata)) contexto.selecionar(candidata, false);
        forma = candidata;
        indice = vertice;
        estadoInicial = candidata.clonar();
        contexto.iniciarEdicao();
        ultimoErro = null;
        contexto.requestStatsUpdate();
    }

    private void editar(MouseEvent evento, EditorContext contexto) {
        try {
            forma.editarVertice(indice, contexto.pontoDoMouse(evento.getX(), evento.getY()));
            ultimoErro = null;
            contexto.atualizarInterface();
        } catch (IllegalArgumentException erro) {
            ultimoErro = erro.getMessage();
        }
    }

    @Override
    public void aoArrastarMouse(MouseEvent evento, EditorContext contexto) {
        if (forma != null && evento.isPrimaryButtonDown()) editar(evento, contexto);
    }

    @Override
    public void aoSoltarMouse(MouseEvent evento, EditorContext contexto) {
        if (forma == null || evento.getButton() != MouseButton.PRIMARY) return;
        editar(evento, contexto);
        forma = null;
        estadoInicial = null;
        contexto.confirmarEdicao();
        contexto.requestStatsUpdate();
        if (ultimoErro != null) contexto.informarErro(ultimoErro);
    }

    @Override public boolean temEdicaoEmAndamento() { return forma != null; }

    @Override
    public void cancelar(EditorContext contexto) {
        if (forma != null) contexto.cancelarEdicao();
        forma = null;
        estadoInicial = null;
        ultimoErro = null;
        Ferramenta.super.cancelar(contexto);
        contexto.atualizarInterface();
    }
}
