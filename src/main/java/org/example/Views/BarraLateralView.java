package org.example.Views;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.example.Controller.EditorContext;
import org.example.Controller.ModoFerramenta;
import org.example.Models.FormaGeometrica;

public class BarraLateralView extends VBox {
    private final EditorContext contexto;
    private final Map<ModoFerramenta, ToggleButton> ferramentas = new EnumMap<>(ModoFerramenta.class);
    private final ColorPicker preenchimento = new ColorPicker();
    private final ColorPicker borda = new ColorPicker();
    private final Slider espessura = new Slider(0, 20, 2);
    private final Label valorEspessura = new Label();
    private final CheckBox grade = new CheckBox("Encaixar na grade (20 px)");
    private final ListView<FormaGeometrica> camadas = new ListView<>();
    private final Button concluir = new Button("Concluir polígono · Enter");
    private final Button cancelar = new Button("Cancelar operação · Esc");
    private final Button excluir = new Button("Excluir selecionadas");
    private final Button frente = new Button("Trazer para frente");
    private final Button tras = new Button("Enviar para trás");
    private final Button agrupar = new Button("Agrupar selecionadas");
    private final Button desagrupar = new Button("Desagrupar selecionadas");
    private final VBox transformacoes = new VBox(8);
    private boolean sincronizando;

    public BarraLateralView(EditorContext contexto) {
        this.contexto = contexto;
        setSpacing(10);
        setPadding(new Insets(18));
        setPrefWidth(270);
        setStyle("-fx-background-color: #f1f3f8;");
        Label titulo = new Label("Editor Vetorial");
        titulo.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: #24304a;");
        Label subtitulo = new Label("Crie, selecione e organize suas formas.");
        subtitulo.setWrapText(true);
        getChildren().addAll(titulo, subtitulo, secao("FERRAMENTAS"));
        construirSecaoFerramentas();
        construirSecaoTransformacoes();
        getChildren().addAll(secao("ESTILO"), new Label("Preenchimento"), preenchimento,
                new Label("Borda"), borda, valorEspessura, espessura, grade,
                secao("CAMADAS · FRENTE NO TOPO"), camadas, frente, tras, agrupar, desagrupar, excluir);
        preenchimento.setMaxWidth(Double.MAX_VALUE);
        borda.setMaxWidth(Double.MAX_VALUE);
        preenchimento.setOnAction(evento -> aplicarEstilo());
        borda.setOnAction(evento -> aplicarEstilo());
        espessura.setShowTickLabels(true);
        espessura.setMajorTickUnit(5);
        espessura.valueProperty().addListener((obs, anterior, atual) -> aplicarEstilo());
        espessura.valueChangingProperty().addListener((obs, antes, arrastando) -> {
            if (!sincronizando) {
                if (arrastando) contexto.iniciarEdicao();
                else contexto.confirmarEdicao();
            }
        });
        grade.setOnAction(evento -> contexto.setGridSnappingHabilitado(grade.isSelected()));
        configurarCamadas();
        configurarBotao(frente, contexto::trazerParaFrente);
        configurarBotao(tras, contexto::enviarParaTras);
        configurarBotao(excluir, contexto::excluirSelecionados);
        configurarBotao(agrupar, () -> executar(contexto::agruparSelecionados));
        configurarBotao(desagrupar, contexto::desagruparSelecionados);
        atualizar();
    }

    private void construirSecaoTransformacoes() {
        TextField fator = campo("1,2", "Fator de escala");
        TextField angulo = campo("15", "Ângulo em graus");
        TextField shx = campo("0,25", "Cisalhamento horizontal");
        TextField shy = campo("0", "Cisalhamento vertical");
        GridPane valores = new GridPane();
        valores.setHgap(8);
        valores.setVgap(8);
        valores.addRow(0, new Label("Fator"), fator);
        valores.addRow(1, new Label("Graus"), angulo);
        valores.addRow(2, new Label("shx"), shx);
        valores.addRow(3, new Label("shy"), shy);
        Button escala = new Button("Aplicar escala");
        Button rotacao = new Button("Aplicar rotação");
        Button cisalhamento = new Button("Aplicar cisalhamento");
        configurarBotao(escala, () -> executar(() -> contexto.transformarSelecionados(
                ModoFerramenta.ESCALAR, numero(fator), 0)));
        configurarBotao(rotacao, () -> executar(() -> contexto.transformarSelecionados(
                ModoFerramenta.ROTACIONAR, Math.toRadians(numero(angulo)), 0)));
        configurarBotao(cisalhamento, () -> executar(() -> contexto.transformarSelecionados(
                ModoFerramenta.CISALHAR, numero(shx), numero(shy))));
        transformacoes.getChildren().addAll(valores, escala, rotacao, cisalhamento);
        TitledPane painel = new TitledPane("Transformações por valor", transformacoes);
        painel.setExpanded(false);
        getChildren().add(painel);
    }

    private TextField campo(String valor, String descricao) {
        TextField campo = new TextField(valor);
        campo.setPrefColumnCount(7);
        campo.setAccessibleText(descricao);
        campo.setTooltip(new Tooltip(descricao));
        return campo;
    }

    private double numero(TextField campo) {
        try {
            double valor = Double.parseDouble(campo.getText().trim().replace(',', '.'));
            if (!Double.isFinite(valor)) throw new NumberFormatException();
            return valor;
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("Informe um número finito em: " + campo.getAccessibleText() + ".");
        }
    }

    private void executar(Runnable acao) {
        try { acao.run(); }
        catch (IllegalArgumentException erro) { contexto.informarErro(erro.getMessage()); }
    }

    private Label secao(String texto) {
        Label label = new Label(texto);
        label.setPadding(new Insets(12, 0, 2, 0));
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #596780;");
        return label;
    }

    private void construirSecaoFerramentas() {
        ToggleGroup grupo = new ToggleGroup();
        GridPane gradeFerramentas = new GridPane();
        gradeFerramentas.setHgap(6);
        gradeFerramentas.setVgap(6);
        for (int i = 0; i < 2; i++) {
            var coluna = new javafx.scene.layout.ColumnConstraints();
            coluna.setPercentWidth(50);
            gradeFerramentas.getColumnConstraints().add(coluna);
        }
        int indice = 0;
        for (ModoFerramenta modo : ModoFerramenta.values()) {
            ToggleButton botao = new ToggleButton(modo.getTitulo());
            botao.setToggleGroup(grupo);
            botao.setMaxWidth(Double.MAX_VALUE);
            botao.setMinHeight(36);
            botao.setWrapText(true);
            botao.setTooltip(new Tooltip(modo.getInstrucao()));
            botao.setOnAction(evento -> contexto.setModo(modo));
            ferramentas.put(modo, botao);
            gradeFerramentas.add(botao, indice % 2, indice / 2);
            indice++;
        }
        getChildren().add(gradeFerramentas);
        configurarBotao(concluir, contexto::concluirDesenho);
        configurarBotao(cancelar, contexto::cancelarDesenho);
        getChildren().addAll(concluir, cancelar);
    }

    private void configurarBotao(Button botao, Runnable acao) {
        botao.setMaxWidth(Double.MAX_VALUE);
        botao.setOnAction(evento -> acao.run());
    }

    private void configurarCamadas() {
        camadas.setPrefHeight(160);
        camadas.setMinHeight(100);
        camadas.setPlaceholder(new Label("Nenhuma forma criada"));
        camadas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        camadas.setCellFactory(lista -> {
            ListCell<FormaGeometrica> celula = new ListCell<>() {
                @Override
                protected void updateItem(FormaGeometrica forma, boolean vazia) {
                    super.updateItem(forma, vazia);
                    setText(vazia || forma == null ? null
                            : (contexto.getFormas().indexOf(forma) + 1) + " · " + nomeForma(forma)
                            + (forma.getGroupId() == null ? "" : " · grupo"));
                }
            };
            celula.setOnMouseClicked(evento -> {
                if (!celula.isEmpty()) {
                    FormaGeometrica forma = celula.getItem();
                    contexto.setModo(ModoFerramenta.SELECIONAR);
                    contexto.selecionar(forma, evento.isShiftDown());
                    evento.consume();
                }
            });
            return celula;
        });
        camadas.setOnKeyPressed(evento -> {
            if (evento.getCode() == KeyCode.ENTER && camadas.getSelectionModel().getSelectedItem() != null) {
                FormaGeometrica forma = camadas.getSelectionModel().getSelectedItem();
                contexto.setModo(ModoFerramenta.SELECIONAR);
                contexto.selecionar(forma, false);
                evento.consume();
            }
        });
    }

    public static String nomeForma(FormaGeometrica forma) {
        return switch (forma.getClass().getSimpleName()) {
            case "Circulo" -> "Círculo";
            case "Poligono" -> "Polígono";
            case "Hexagono" -> "Hexágono";
            default -> forma.getClass().getSimpleName();
        };
    }

    private void aplicarEstilo() {
        if (!sincronizando) {
            contexto.definirEstilo(hex(preenchimento.getValue()), hex(borda.getValue()), espessura.getValue());
        }
    }

    private static String hex(Color cor) {
        return String.format("#%02X%02X%02X%02X", Math.round(cor.getRed() * 255),
                Math.round(cor.getGreen() * 255), Math.round(cor.getBlue() * 255),
                Math.round(cor.getOpacity() * 255));
    }

    public void atualizar() {
        sincronizando = true;
        try {
            ferramentas.get(contexto.getModo()).setSelected(true);
            preenchimento.setValue(Color.web(contexto.getCorPreenchimentoHex()));
            borda.setValue(Color.web(contexto.getCorBordaHex()));
            espessura.setMax(Math.max(20, contexto.getEspessuraBorda()));
            espessura.setValue(contexto.getEspessuraBorda());
            valorEspessura.setText(String.format("Espessura: %.1f px", contexto.getEspessuraBorda()));
            grade.setSelected(contexto.isGridSnappingHabilitado());
            List<FormaGeometrica> ordem = new java.util.ArrayList<>(contexto.getFormas());
            java.util.Collections.reverse(ordem);
            if (!camadas.getItems().equals(ordem)) camadas.getItems().setAll(ordem);
            camadas.getSelectionModel().clearSelection();
            for (int i = 0; i < ordem.size(); i++) {
                if (contexto.getSelecionados().contains(ordem.get(i))) camadas.getSelectionModel().select(i);
            }
            camadas.refresh();
            boolean semSelecao = contexto.getSelecionados().isEmpty();
            excluir.setDisable(semSelecao);
            frente.setDisable(semSelecao);
            tras.setDisable(semSelecao);
            agrupar.setDisable(contexto.getSelecionados().size() < 2);
            desagrupar.setDisable(contexto.getSelecionados().stream().noneMatch(f -> f.getGroupId() != null));
            transformacoes.setDisable(semSelecao);
            atualizarDesenhoEmAndamento();
        } finally {
            sincronizando = false;
        }
    }

    public void atualizarDesenhoEmAndamento() {
        boolean desenhando = contexto.getFormaEmAndamento() != null;
        concluir.setDisable(!desenhando || contexto.getModo() != ModoFerramenta.POLIGONO);
        cancelar.setDisable(!desenhando && !contexto.getFerramentaAtual().temEdicaoEmAndamento());
    }
}
