package org.example.Controller;

import java.util.Map;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

public class FerramentaTransformar implements Ferramenta {
    private final ModoFerramenta modo;
    private Map<FormaGeometrica, FormaGeometrica> estadoInicial;
    private Ponto inicio;
    private Ponto centro;
    private String ultimoErro;

    public FerramentaTransformar(ModoFerramenta modo) {
        if (modo != ModoFerramenta.ESCALAR && modo != ModoFerramenta.ROTACIONAR && modo != ModoFerramenta.CISALHAR) {
            throw new IllegalArgumentException("Modo de transformação inválido.");
        }
        this.modo = modo;
    }

    @Override
    public void aoPressionarMouse(MouseEvent evento, EditorContext contexto) {
        if (evento.getButton() != MouseButton.PRIMARY) return;
        FormaGeometrica alvo = contexto.encontrarForma(evento.getX(), evento.getY());
        if (alvo != null && !contexto.getSelecionados().contains(alvo)) contexto.selecionar(alvo, false);
        if (contexto.getSelecionados().isEmpty()) return;
        centro = contexto.getCentroSelecao();
        inicio = new Ponto(evento.getX(), evento.getY());
        // O centro não define direção nem uma distância útil para escala/rotação.
        if (modo != ModoFerramenta.CISALHAR && inicio.distancia(centro) < 5) return;
        estadoInicial = contexto.capturarSelecao();
        contexto.iniciarEdicao();
        ultimoErro = null;
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoArrastarMouse(MouseEvent evento, EditorContext contexto) {
        if (estadoInicial != null && evento.isPrimaryButtonDown()) aplicar(evento, contexto);
    }

    private void aplicar(MouseEvent evento, EditorContext contexto) {
        Ponto atual = new Ponto(evento.getX(), evento.getY());
        double a;
        double b = 0;
        switch (modo) {
            case ESCALAR -> a = atual.distancia(centro) / inicio.distancia(centro);
            case ROTACIONAR -> {
                double delta = Math.atan2(atual.getY() - centro.getY(), atual.getX() - centro.getX())
                        - Math.atan2(inicio.getY() - centro.getY(), inicio.getX() - centro.getX());
                a = Math.atan2(Math.sin(delta), Math.cos(delta));
            }
            case CISALHAR -> {
                a = (atual.getX() - inicio.getX()) / 150;
                b = (atual.getY() - inicio.getY()) / 150;
            }
            default -> throw new IllegalStateException();
        }
        try {
            contexto.transformarDesde(estadoInicial, modo, a, b);
            ultimoErro = null;
        } catch (IllegalArgumentException erro) {
            // Mantém a última prévia válida e permite corrigir o gesto sem abrir alertas a cada pixel.
            ultimoErro = erro.getMessage();
        }
    }

    @Override
    public void aoSoltarMouse(MouseEvent evento, EditorContext contexto) {
        if (estadoInicial == null || evento.getButton() != MouseButton.PRIMARY) return;
        aplicar(evento, contexto);
        estadoInicial = null;
        contexto.confirmarEdicao();
        contexto.requestStatsUpdate();
        if (ultimoErro != null) contexto.informarErro(ultimoErro);
    }

    @Override public boolean temEdicaoEmAndamento() { return estadoInicial != null; }

    @Override
    public void cancelar(EditorContext contexto) {
        if (estadoInicial != null) contexto.cancelarEdicao();
        estadoInicial = null;
        ultimoErro = null;
        Ferramenta.super.cancelar(contexto);
    }
}
