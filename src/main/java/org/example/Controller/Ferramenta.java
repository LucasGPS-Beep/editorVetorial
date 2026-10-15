package org.example.Controller;

import javafx.scene.input.MouseEvent;

/** Estado de interação: cada ferramenta recebe os mesmos eventos do canvas. */
public interface Ferramenta {
    default void aoPressionarMouse(MouseEvent evento, EditorContext contexto) { }
    default void aoArrastarMouse(MouseEvent evento, EditorContext contexto) { }
    default void aoSoltarMouse(MouseEvent evento, EditorContext contexto) { }
    default void aoClicar(MouseEvent evento, EditorContext contexto) { }
    default void concluir(EditorContext contexto) { }
    default boolean temEdicaoEmAndamento() { return false; }
    default void cancelar(EditorContext contexto) { contexto.setFormaEmAndamento(null); }
}
