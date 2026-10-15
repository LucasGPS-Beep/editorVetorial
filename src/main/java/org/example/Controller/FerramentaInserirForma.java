package org.example.Controller;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.*;

/** Inserção por arrasto: o primeiro ponto é o centro e a distância define o raio. */
public class FerramentaInserirForma implements Ferramenta {
    private final ModoFerramenta modo;
    private Ponto centroInsercao;

    public FerramentaInserirForma(ModoFerramenta modo) {
        if (modo != ModoFerramenta.CIRCULO && modo != ModoFerramenta.QUADRADO
                && modo != ModoFerramenta.HEXAGONO) {
            throw new IllegalArgumentException("Escolha uma forma regular para inserir.");
        }
        this.modo = modo;
    }

    @Override
    public void aoPressionarMouse(MouseEvent evento, EditorContext contexto) {
        if (evento.getButton() != MouseButton.PRIMARY) return;
        centroInsercao = contexto.pontoDoMouse(evento.getX(), evento.getY());
        contexto.setFormaEmAndamento(null);
    }

    @Override
    public void aoArrastarMouse(MouseEvent evento, EditorContext contexto) {
        if (centroInsercao == null || !evento.isPrimaryButtonDown()) return;
        atualizarPrevia(evento, contexto);
    }

    private void atualizarPrevia(MouseEvent evento, EditorContext contexto) {
        double raio = centroInsercao.distancia(contexto.pontoDoMouse(evento.getX(), evento.getY()));
        if (raio == 0) {
            contexto.setFormaEmAndamento(null);
            return;
        }
        FormaGeometrica forma = switch (modo) {
            case CIRCULO -> new Circulo(centroInsercao, raio);
            case QUADRADO -> new Quadrado(centroInsercao, raio);
            case HEXAGONO -> new Hexagono(centroInsercao, raio);
            default -> throw new IllegalStateException("Forma de inserção inválida.");
        };
        contexto.aplicarEstilo(forma);
        contexto.setFormaEmAndamento(forma);
    }

    @Override
    public void aoSoltarMouse(MouseEvent evento, EditorContext contexto) {
        if (centroInsercao == null || evento.getButton() != MouseButton.PRIMARY) return;
        atualizarPrevia(evento, contexto);
        centroInsercao = null;
        if (contexto.getFormaEmAndamento() != null) {
            contexto.adicionarForma(contexto.getFormaEmAndamento());
        }
    }

    @Override
    public void cancelar(EditorContext contexto) {
        centroInsercao = null;
        Ferramenta.super.cancelar(contexto);
    }
}
