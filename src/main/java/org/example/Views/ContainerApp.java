package org.example.Views;

import java.util.List;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.Controller.EditorContext;
import org.example.Controller.ModoFerramenta;
import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

/** Composição da interface e renderização; eventos são delegados à ferramenta ativa. */
public class ContainerApp extends Application {
    private final EditorContext contexto = new EditorContext();
    private final Canvas areaDeDesenho = new Canvas(800, 600);
    private BarraLateralView barraLateral;
    private PainelStatusView painelStatus;
    private BarraMenuView barraMenu;

    @Override
    public void start(Stage stage) {
        StackPane painelDesenho = new StackPane(areaDeDesenho);
        painelDesenho.setMinSize(0, 0);
        painelDesenho.setStyle("-fx-background-color: white;");
        areaDeDesenho.widthProperty().bind(painelDesenho.widthProperty());
        areaDeDesenho.heightProperty().bind(painelDesenho.heightProperty());
        areaDeDesenho.widthProperty().addListener((obs, antes, depois) -> atualizarTamanhoCanvas());
        areaDeDesenho.heightProperty().addListener((obs, antes, depois) -> atualizarTamanhoCanvas());
        barraLateral = new BarraLateralView(contexto);
        painelStatus = new PainelStatusView(contexto);
        barraMenu = new BarraMenuView(contexto, stage, areaDeDesenho);
        ScrollPane lateralRolavel = new ScrollPane(barraLateral);
        lateralRolavel.setFitToWidth(true);
        lateralRolavel.setPrefWidth(290);
        lateralRolavel.setMinWidth(250);
        lateralRolavel.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        BorderPane raiz = new BorderPane();
        raiz.setLeft(lateralRolavel);
        raiz.setCenter(painelDesenho);
        raiz.setBottom(painelStatus);
        raiz.setTop(barraMenu);
        BorderPane.setMargin(painelDesenho, new Insets(1));
        contexto.setAoRedesenhar(this::redesenharCanvas);
        contexto.setAoAtualizarStatus(() -> {
            painelStatus.atualizar();
            barraLateral.atualizarDesenhoEmAndamento();
            barraMenu.atualizar();
        });
        contexto.setAoAtualizarInterface(() -> {
            barraLateral.atualizar();
            barraMenu.atualizar();
        });
        contexto.setAoErro(mensagem -> {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.initOwner(stage);
            alerta.setTitle("Verifique o desenho");
            alerta.setHeaderText("Não foi possível concluir a operação");
            alerta.setContentText(mensagem);
            alerta.showAndWait();
        });
        inicializarEventos();
        stage.setTitle("Editor Vetorial");
        stage.setScene(new Scene(raiz, 1180, 800));
        stage.setMinWidth(800);
        stage.setMinHeight(560);
        stage.show();
        contexto.atualizarInterface();
        areaDeDesenho.requestFocus();
    }

    private void atualizarTamanhoCanvas() {
        contexto.atualizarTamanhoCanvas(areaDeDesenho.getWidth(), areaDeDesenho.getHeight());
        redesenharCanvas();
    }

    public void inicializarEventos() {
        areaDeDesenho.setFocusTraversable(true);
        areaDeDesenho.setOnMouseMoved(evento -> contexto.atualizarMouse(evento.getX(), evento.getY()));
        areaDeDesenho.setOnMousePressed(evento -> {
            areaDeDesenho.requestFocus();
            atualizarMouse(evento);
            contexto.getFerramentaAtual().aoPressionarMouse(evento, contexto);
        });
        areaDeDesenho.setOnMouseDragged(evento -> {
            atualizarMouse(evento);
            contexto.getFerramentaAtual().aoArrastarMouse(evento, contexto);
        });
        areaDeDesenho.setOnMouseReleased(evento -> {
            atualizarMouse(evento);
            contexto.getFerramentaAtual().aoSoltarMouse(evento, contexto);
        });
        areaDeDesenho.setOnMouseClicked(evento -> contexto.getFerramentaAtual().aoClicar(evento, contexto));
        areaDeDesenho.setOnKeyPressed(evento -> {
            if (evento.getCode() == KeyCode.ENTER) contexto.concluirDesenho();
            else if (evento.getCode() == KeyCode.ESCAPE) contexto.cancelarDesenho();
            else if (evento.getCode() == KeyCode.DELETE) contexto.excluirSelecionados();
            else return;
            evento.consume();
        });
    }

    private void atualizarMouse(MouseEvent evento) {
        contexto.atualizarMouse(evento.getX(), evento.getY());
    }

    private void redesenharCanvas() {
        GraphicsContext gc = areaDeDesenho.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, areaDeDesenho.getWidth(), areaDeDesenho.getHeight());
        if (contexto.isGridSnappingHabilitado()) desenharGrade(gc);
        for (FormaGeometrica forma : contexto.getFormas()) {
            desenharForma(forma, contexto.getSelecionados().contains(forma));
        }
        FormaGeometrica previa = contexto.getFormaEmAndamento();
        if (previa != null) {
            gc.save();
            gc.setGlobalAlpha(0.65);
            desenharForma(previa, false);
            gc.restore();
            if (contexto.getModo() == ModoFerramenta.POLIGONO) desenharVerticesEGuia(gc, previa);
        }
        desenharControlesSelecao(gc);
    }

    private void desenharControlesSelecao(GraphicsContext gc) {
        gc.save();
        gc.setStroke(Color.web("#1674E8"));
        gc.setLineWidth(1.5);
        if (contexto.getCaixaSelecao() != null) {
            var caixa = contexto.getCaixaSelecao();
            double largura = caixa.maxX() - caixa.minX();
            double altura = caixa.maxY() - caixa.minY();
            gc.setFill(Color.web("#1674E8", 0.1));
            gc.fillRect(caixa.minX(), caixa.minY(), largura, altura);
            gc.setLineDashes(5, 3);
            gc.strokeRect(caixa.minX(), caixa.minY(), largura, altura);
        }
        gc.setLineDashes();
        if (contexto.getModo() == ModoFerramenta.EDITAR_VERTICE) {
            gc.setFill(Color.WHITE);
            for (FormaGeometrica forma : contexto.getSelecionados()) {
                if (forma instanceof org.example.Models.Poligono) {
                    for (Ponto p : forma.getPontos()) {
                        gc.fillOval(p.getX() - 4, p.getY() - 4, 8, 8);
                        gc.strokeOval(p.getX() - 4, p.getY() - 4, 8, 8);
                    }
                }
            }
        }
        if (!contexto.getSelecionados().isEmpty() && (contexto.getModo() == ModoFerramenta.ESCALAR
                || contexto.getModo() == ModoFerramenta.ROTACIONAR || contexto.getModo() == ModoFerramenta.CISALHAR)) {
            Ponto centro = contexto.getCentroSelecao();
            gc.strokeOval(centro.getX() - 5, centro.getY() - 5, 10, 10);
            gc.strokeLine(centro.getX() - 9, centro.getY(), centro.getX() + 9, centro.getY());
            gc.strokeLine(centro.getX(), centro.getY() - 9, centro.getX(), centro.getY() + 9);
        }
        gc.restore();
    }

    private void desenharGrade(GraphicsContext gc) {
        gc.setStroke(Color.web("#E9EDF5"));
        gc.setLineWidth(1);
        gc.setLineDashes();
        for (double x = 0; x < areaDeDesenho.getWidth(); x += EditorContext.PASSO_GRADE) {
            gc.strokeLine(x + 0.5, 0, x + 0.5, areaDeDesenho.getHeight());
        }
        for (double y = 0; y < areaDeDesenho.getHeight(); y += EditorContext.PASSO_GRADE) {
            gc.strokeLine(0, y + 0.5, areaDeDesenho.getWidth(), y + 0.5);
        }
    }

    private void desenharForma(FormaGeometrica forma, boolean selecionada) {
        GraphicsContext gc = areaDeDesenho.getGraphicsContext2D();
        gc.save();
        construirCaminho(gc, forma);
        if (forma instanceof Circulo || forma.getQuantidadePontos() >= 3) {
            gc.setFill(Color.web(forma.getCorPreenchimentoHex()));
            gc.fill();
        }
        if (forma.getEspessuraBorda() > 0) {
            gc.setStroke(Color.web(forma.getCorBordaHex()));
            gc.setLineWidth(forma.getEspessuraBorda());
            gc.setLineDashes();
            gc.stroke();
        }
        if (selecionada) {
            gc.setStroke(Color.web("#1674E8"));
            gc.setLineWidth(2);
            gc.setLineDashes(6, 4);
            gc.stroke();
        }
        gc.restore();
    }

    /** Elipse afim em oito curvas cúbicas, sem deformar a espessura da borda. */
    private void construirCaminho(GraphicsContext gc, FormaGeometrica forma) {
        List<Ponto> pontos = forma.getPontos();
        gc.beginPath();
        if (pontos.isEmpty()) return;
        if (forma instanceof Circulo) {
            Ponto centro = pontos.get(0);
            double ux = pontos.get(1).getX() - centro.getX();
            double uy = pontos.get(1).getY() - centro.getY();
            double vx = pontos.get(2).getX() - centro.getX();
            double vy = pontos.get(2).getY() - centro.getY();
            double passo = Math.PI / 4;
            double k = 4.0 / 3 * Math.tan(passo / 4);
            gc.moveTo(centro.getX() + ux, centro.getY() + uy);
            for (int i = 0; i < 8; i++) {
                double a = i * passo;
                double b = a + passo;
                double ax = centro.getX() + ux * Math.cos(a) + vx * Math.sin(a);
                double ay = centro.getY() + uy * Math.cos(a) + vy * Math.sin(a);
                double bx = centro.getX() + ux * Math.cos(b) + vx * Math.sin(b);
                double by = centro.getY() + uy * Math.cos(b) + vy * Math.sin(b);
                gc.bezierCurveTo(ax + k * (-ux * Math.sin(a) + vx * Math.cos(a)),
                        ay + k * (-uy * Math.sin(a) + vy * Math.cos(a)),
                        bx - k * (-ux * Math.sin(b) + vx * Math.cos(b)),
                        by - k * (-uy * Math.sin(b) + vy * Math.cos(b)), bx, by);
            }
            gc.closePath();
        } else {
            gc.moveTo(pontos.get(0).getX(), pontos.get(0).getY());
            for (int i = 1; i < pontos.size(); i++) gc.lineTo(pontos.get(i).getX(), pontos.get(i).getY());
            if (pontos.size() >= 3) gc.closePath();
        }
    }

    private void desenharVerticesEGuia(GraphicsContext gc, FormaGeometrica forma) {
        gc.save();
        List<Ponto> pontos = forma.getPontos();
        gc.setFill(Color.web("#1674E8"));
        pontos.forEach(p -> gc.fillOval(p.getX() - 3, p.getY() - 3, 6, 6));
        if (!pontos.isEmpty()) {
            Ponto ultimo = pontos.get(pontos.size() - 1);
            gc.setStroke(Color.web("#1674E8"));
            gc.setLineWidth(1);
            gc.setLineDashes(4, 4);
            gc.strokeLine(ultimo.getX(), ultimo.getY(), contexto.getMouseX(), contexto.getMouseY());
        }
        gc.restore();
    }

    public static void main(String[] args) { launch(args); }
}
