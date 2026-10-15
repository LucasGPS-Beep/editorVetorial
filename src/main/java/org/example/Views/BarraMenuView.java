package org.example.Views;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.Controller.ArquivoController;
import org.example.Controller.EditorContext;
import org.example.Models.DocumentoVetorial;

/** Menus, atalhos e diálogos de arquivo; a persistência fica no controller. */
public class BarraMenuView extends VBox {
    private final EditorContext contexto;
    private final Stage janela;
    private final Canvas canvas;
    private final ArquivoController arquivos = new ArquivoController();
    private final Button desfazer = new Button("Desfazer");
    private final Button refazer = new Button("Refazer");
    private final MenuItem itemDesfazer;
    private final MenuItem itemRefazer;
    private Path arquivoAtual;

    public BarraMenuView(EditorContext contexto, Stage janela, Canvas canvas) {
        this.contexto = contexto;
        this.janela = janela;
        this.canvas = canvas;
        Menu arquivo = new Menu("Arquivo");
        MenuItem novo = item("Novo", KeyCode.N, this::novoArquivo);
        MenuItem abrir = item("Abrir…", KeyCode.O, this::carregarArquivo);
        MenuItem salvar = item("Salvar", KeyCode.S, () -> salvarArquivo(false));
        MenuItem salvarComo = new MenuItem("Salvar como…");
        salvarComo.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        salvarComo.setOnAction(evento -> salvarArquivo(true));
        arquivo.getItems().addAll(novo, abrir, salvar, salvarComo);
        Menu editar = new Menu("Editar");
        itemDesfazer = item("Desfazer", KeyCode.Z, contexto::desfazer);
        itemRefazer = item("Refazer", KeyCode.Y, contexto::refazer);
        MenuItem refazerAlternativo = new MenuItem("Refazer (Ctrl+Shift+Z)");
        refazerAlternativo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        refazerAlternativo.setOnAction(evento -> contexto.refazer());
        editar.getItems().addAll(itemDesfazer, itemRefazer, refazerAlternativo);
        Button botaoNovo = new Button("Novo");
        Button botaoAbrir = new Button("Abrir");
        Button botaoSalvar = new Button("Salvar");
        botaoNovo.setOnAction(evento -> novoArquivo());
        botaoAbrir.setOnAction(evento -> carregarArquivo());
        botaoSalvar.setOnAction(evento -> salvarArquivo(false));
        desfazer.setOnAction(evento -> contexto.desfazer());
        refazer.setOnAction(evento -> contexto.refazer());
        getChildren().addAll(new MenuBar(arquivo, editar),
                new ToolBar(botaoNovo, botaoAbrir, botaoSalvar, new Separator(), desfazer, refazer));
        janela.setOnCloseRequest(evento -> {
            if (!confirmarAlteracoes()) evento.consume();
        });
        atualizar();
    }

    private MenuItem item(String texto, KeyCode tecla, Runnable acao) {
        MenuItem item = new MenuItem(texto);
        item.setAccelerator(new KeyCodeCombination(tecla, KeyCombination.SHORTCUT_DOWN));
        item.setOnAction(evento -> acao.run());
        return item;
    }

    public void atualizar() {
        desfazer.setDisable(!contexto.podeDesfazer());
        refazer.setDisable(!contexto.podeRefazer());
        itemDesfazer.setDisable(!contexto.podeDesfazer());
        itemRefazer.setDisable(!contexto.podeRefazer());
        String nome = arquivoAtual == null ? "Sem título" : arquivoAtual.getFileName().toString();
        janela.setTitle((contexto.temAlteracoesNaoSalvas() ? "* " : "") + nome + " — Editor Vetorial");
    }

    private FileChooser seletor(String titulo) {
        FileChooser seletor = new FileChooser();
        seletor.setTitle(titulo);
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Desenho vetorial JSON", "*.json"));
        if (arquivoAtual != null && arquivoAtual.toAbsolutePath().getParent().toFile().isDirectory()) {
            seletor.setInitialDirectory(arquivoAtual.toAbsolutePath().getParent().toFile());
        }
        return seletor;
    }

    private boolean salvarArquivo(boolean como) {
        try {
            DocumentoVetorial documento = contexto.exportarDocumento();
            Path destino = arquivoAtual;
            if (como || destino == null) {
                FileChooser seletor = seletor("Salvar desenho");
                seletor.setInitialFileName(arquivoAtual == null ? "desenho.json" : arquivoAtual.getFileName().toString());
                File escolhido = seletor.showSaveDialog(janela);
                if (escolhido == null) return false;
                destino = escolhido.toPath();
            }
            arquivos.salvar(destino, documento);
            arquivoAtual = destino.toAbsolutePath();
            contexto.marcarSalvo();
            atualizar();
            return true;
        } catch (IOException | IllegalArgumentException | IllegalStateException erro) {
            mostrarErro("Não foi possível salvar", erro.getMessage());
            return false;
        }
    }

    private void carregarArquivo() {
        File escolhido = seletor("Abrir desenho").showOpenDialog(janela);
        if (escolhido == null) return;
        try {
            // Valida antes de oferecer descarte; relê após eventual salvamento do arquivo atual.
            arquivos.carregar(escolhido.toPath());
            if (!confirmarAlteracoes()) return;
            DocumentoVetorial documento = arquivos.carregar(escolhido.toPath());
            contexto.carregarDocumento(documento);
            arquivoAtual = escolhido.toPath().toAbsolutePath();
            double margemHorizontal = janela.getWidth() - canvas.getWidth();
            double margemVertical = janela.getHeight() - canvas.getHeight();
            janela.setWidth(documento.larguraCanvas() + margemHorizontal);
            janela.setHeight(documento.alturaCanvas() + margemVertical);
            atualizar();
        } catch (IOException | IllegalArgumentException erro) {
            mostrarErro("Não foi possível abrir", erro.getMessage());
        }
    }

    private void novoArquivo() {
        if (!confirmarAlteracoes()) return;
        contexto.carregarDocumento(new DocumentoVetorial(canvas.getWidth(), canvas.getHeight(), false, List.of()));
        arquivoAtual = null;
        atualizar();
    }

    private boolean confirmarAlteracoes() {
        if (!contexto.temAlteracoesNaoSalvas()) return true;
        ButtonType salvar = new ButtonType("Salvar", ButtonBar.ButtonData.YES);
        ButtonType descartar = new ButtonType("Descartar", ButtonBar.ButtonData.NO);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, "Há alterações não salvas neste desenho.", salvar, descartar, cancelar);
        alerta.initOwner(janela);
        alerta.setTitle("Alterações não salvas");
        alerta.setHeaderText("Deseja salvar antes de continuar?");
        ButtonType escolha = alerta.showAndWait().orElse(cancelar);
        return escolha == descartar || (escolha == salvar && salvarArquivo(false));
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.initOwner(janela);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
