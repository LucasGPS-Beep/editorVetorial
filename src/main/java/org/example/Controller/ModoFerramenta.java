package org.example.Controller;

public enum ModoFerramenta {
    SELECIONAR("Selecionar / mover", "Clique para selecionar; arraste uma forma para mover ou o fundo para selecionar por caixa. Shift adiciona à seleção."),
    POLIGONO("Polígono", "Clique para adicionar vértices. Enter ou duplo clique conclui; Esc cancela."),
    CIRCULO("Círculo", "Arraste do centro até a borda para criar um círculo."),
    QUADRADO("Quadrado", "Arraste do centro até um vértice para criar um quadrado."),
    HEXAGONO("Hexágono", "Arraste do centro até um vértice para criar um hexágono."),
    ESCALAR("Escalar", "Selecione as formas e arraste afastando-se do centro para ampliar. Esc reverte o arrasto."),
    ROTACIONAR("Rotacionar", "Selecione as formas e arraste em torno do centro para rotacionar. Esc reverte o arrasto."),
    CISALHAR("Cisalhar", "Selecione as formas e arraste horizontal ou verticalmente para cisalhar. Esc reverte o arrasto."),
    EDITAR_VERTICE("Editar vértice", "Arraste um vértice destacado de um polígono. Esc reverte a edição; círculos não têm vértices.");

    private final String titulo;
    private final String instrucao;

    ModoFerramenta(String titulo, String instrucao) {
        this.titulo = titulo;
        this.instrucao = instrucao;
    }

    public String getTitulo() { return titulo; }
    public String getInstrucao() { return instrucao; }
}
