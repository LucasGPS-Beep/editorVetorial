package org.example.Views;

import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.Controller.EditorContext;
import org.example.Models.FormaGeometrica;

public class PainelStatusView extends VBox {
    private final EditorContext contexto;
    private final Label instrucao = new Label();
    private final Label dados = new Label();

    public PainelStatusView(EditorContext contexto) {
        this.contexto = contexto;
        setSpacing(4);
        setPadding(new Insets(10, 16, 10, 16));
        setStyle("-fx-background-color: #e9edf5;");
        instrucao.setWrapText(true);
        dados.setWrapText(true);
        getChildren().addAll(instrucao, dados);
        atualizar();
    }

    public void atualizar() {
        instrucao.setText(contexto.getModo().getInstrucao());
        List<FormaGeometrica> selecao = contexto.getSelecionados();
        FormaGeometrica previa = contexto.getFormaEmAndamento();
        List<FormaGeometrica> medidas = previa == null ? selecao : List.of(previa);
        double area = medidas.stream().mapToDouble(FormaGeometrica::calcularArea).sum();
        double perimetro = medidas.stream().mapToDouble(FormaGeometrica::calcularPerimetro).sum();
        String alvo = previa == null ? selecao.size() + " selecionada(s)" : "Desenho em andamento";
        dados.setText(String.format("X: %.0f  Y: %.0f   |   %d forma(s)   |   %s   |   Área: %.2f px²   Perímetro: %.2f px",
                contexto.getMouseX(), contexto.getMouseY(), contexto.getFormas().size(), alvo, area, perimetro));
    }
}
