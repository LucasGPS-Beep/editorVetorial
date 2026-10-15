package org.example.Controller;

import java.util.List;
import javafx.event.EventType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HistoricoTest {
    @Test
    void excluirDuranteArrastoCancelaGestoERegistraExclusaoSeparadamente() {
        EditorContext c = comCirculo();
        mouse(c, MouseEvent.MOUSE_PRESSED, 100, 100);
        mouse(c, MouseEvent.MOUSE_DRAGGED, 150, 100);
        c.excluirSelecionados();
        assertTrue(c.getFormas().isEmpty());
        assertFalse(c.isEdicaoEmAndamento());
        mouse(c, MouseEvent.MOUSE_RELEASED, 180, 100);
        c.desfazer();
        assertEquals(1, c.getFormas().size());
        assertEquals(100, c.getFormas().get(0).getCentroide().getX());
        assertFalse(c.temAlteracoesNaoSalvas());
        c.refazer();
        assertTrue(c.getFormas().isEmpty());
    }

    private EditorContext comCirculo() {
        EditorContext c = new EditorContext();
        c.adicionarForma(new Circulo(new Ponto(100, 100), 20));
        c.marcarSalvo();
        return c;
    }

    private void mouse(EditorContext c, EventType<MouseEvent> tipo, double x, double y) {
        var e = new MouseEvent(tipo, x, y, x, y, MouseButton.PRIMARY, 1,
                false, false, false, false, tipo != MouseEvent.MOUSE_RELEASED,
                false, false, false, false, true, null);
        if (tipo == MouseEvent.MOUSE_PRESSED) c.getFerramentaAtual().aoPressionarMouse(e, c);
        else if (tipo == MouseEvent.MOUSE_DRAGGED) c.getFerramentaAtual().aoArrastarMouse(e, c);
        else c.getFerramentaAtual().aoSoltarMouse(e, c);
    }

    @Test
    void historicoArmazenaCopiasProfundas() {
        HistoricoController h = new HistoricoController();
        Circulo circulo = new Circulo(new Ponto(100, 100), 20);
        h.registrarEstado(List.of(circulo));
        circulo.transladar(50, 0);
        var anterior = h.desfazer(List.of(circulo));
        assertEquals(100, anterior.get(0).getCentroide().getX());
        anterior.get(0).transladar(999, 0);
        var refeito = h.refazer(anterior);
        assertEquals(150, refeito.get(0).getCentroide().getX());
        assertNotSame(circulo, refeito.get(0));
    }

    @Test
    void criacaoPodeSerDesfeitaERefeita() {
        EditorContext c = new EditorContext();
        assertFalse(c.temAlteracoesNaoSalvas());
        assertFalse(c.podeDesfazer());
        c.adicionarForma(new Quadrado(new Ponto(100, 100), 20));
        assertTrue(c.temAlteracoesNaoSalvas());
        c.desfazer();
        assertTrue(c.getFormas().isEmpty());
        assertFalse(c.temAlteracoesNaoSalvas());
        c.refazer();
        assertInstanceOf(Quadrado.class, c.getFormas().get(0));
        assertTrue(c.temAlteracoesNaoSalvas());
    }

    @Test
    void variosEventosDeArrastoGeramUmaUnicaAcaoEVoltarAoSalvoLimpaIndicador() {
        EditorContext c = comCirculo();
        mouse(c, MouseEvent.MOUSE_PRESSED, 100, 100);
        mouse(c, MouseEvent.MOUSE_DRAGGED, 110, 100);
        mouse(c, MouseEvent.MOUSE_DRAGGED, 120, 100);
        mouse(c, MouseEvent.MOUSE_RELEASED, 150, 100);
        assertEquals(150, c.getFormas().get(0).getCentroide().getX());
        assertTrue(c.temAlteracoesNaoSalvas());
        c.desfazer();
        assertEquals(100, c.getFormas().get(0).getCentroide().getX());
        assertFalse(c.temAlteracoesNaoSalvas());
        c.refazer();
        assertEquals(150, c.getFormas().get(0).getCentroide().getX());
        c.desfazer();
        c.desfazer();
        assertTrue(c.getFormas().isEmpty());
    }

    @Test
    void cancelarMovimentoNaoCriaHistoricoNemSujeira() {
        EditorContext c = comCirculo();
        mouse(c, MouseEvent.MOUSE_PRESSED, 100, 100);
        mouse(c, MouseEvent.MOUSE_DRAGGED, 150, 100);
        c.cancelarDesenho();
        assertEquals(100, c.getFormas().get(0).getCentroide().getX());
        assertFalse(c.temAlteracoesNaoSalvas());
        c.desfazer();
        assertTrue(c.getFormas().isEmpty());
    }

    @Test
    void clicarSemMoverNaoCriaAcao() {
        EditorContext c = comCirculo();
        mouse(c, MouseEvent.MOUSE_PRESSED, 100, 100);
        mouse(c, MouseEvent.MOUSE_RELEASED, 100, 100);
        c.trazerParaFrente();
        c.desagruparSelecionados();
        c.desfazer();
        assertTrue(c.getFormas().isEmpty());
    }

    @Test
    void novaAlteracaoLimpaRedoMasSelecaoEOperacaoInvalidaNao() {
        EditorContext c = comCirculo();
        c.moverSelecionados(50, 0);
        c.desfazer();
        c.selecionar(c.getFormas().get(0), false);
        assertThrows(IllegalArgumentException.class, () -> c.transformarSelecionados(ModoFerramenta.ESCALAR, 0, 0));
        assertTrue(c.podeRefazer());
        c.definirEstilo("#FF0000", "#000000", 1);
        assertFalse(c.podeRefazer());
    }

    @Test
    void estilosDeSliderSaoUmaUnicaAcao() {
        EditorContext c = comCirculo();
        c.iniciarEdicao();
        c.definirEstilo("#FF0000", "#123456", 2);
        c.definirEstilo("#FF0000", "#123456", 8);
        c.confirmarEdicao();
        c.desfazer();
        assertEquals("#D3D3D3", c.getFormas().get(0).getCorPreenchimentoHex());
        assertEquals(1, c.getFormas().get(0).getEspessuraBorda());
        assertFalse(c.temAlteracoesNaoSalvas());
        c.refazer();
        assertEquals(8, c.getFormas().get(0).getEspessuraBorda());
    }

    @Test
    void agrupamentoTransformacaoECamadasPodemSerDesfeitos() {
        EditorContext c = comCirculo();
        c.adicionarForma(new Quadrado(new Ponto(300, 100), 20));
        c.selecionarLista(c.getFormas());
        c.agruparSelecionados();
        c.marcarSalvo();
        c.setModo(ModoFerramenta.ESCALAR);
        mouse(c, MouseEvent.MOUSE_PRESSED, 400, 100);
        mouse(c, MouseEvent.MOUSE_DRAGGED, 500, 100);
        mouse(c, MouseEvent.MOUSE_RELEASED, 550, 100);
        c.desfazer();
        assertFalse(c.temAlteracoesNaoSalvas());
        assertNotNull(c.getFormas().get(0).getGroupId());
        c.desfazer();
        assertNull(c.getFormas().get(0).getGroupId());
        c.selecionar(c.getFormas().get(1), false);
        c.enviarParaTras();
        assertInstanceOf(Quadrado.class, c.getFormas().get(0));
        c.desfazer();
        assertInstanceOf(Circulo.class, c.getFormas().get(0));
    }

    @Test
    void edicaoDeVerticePodeSerDesfeitaEmUmaAcao() {
        EditorContext c = new EditorContext();
        c.adicionarForma(new Poligono(List.of(new Ponto(100, 100), new Ponto(200, 100), new Ponto(100, 200))));
        c.marcarSalvo();
        c.setModo(ModoFerramenta.EDITAR_VERTICE);
        mouse(c, MouseEvent.MOUSE_PRESSED, 100, 100);
        mouse(c, MouseEvent.MOUSE_DRAGGED, 80, 80);
        mouse(c, MouseEvent.MOUSE_RELEASED, 60, 60);
        c.desfazer();
        assertEquals(100, c.getFormas().get(0).getPontos().get(0).getX());
        assertFalse(c.temAlteracoesNaoSalvas());
        c.refazer();
        assertEquals(60, c.getFormas().get(0).getPontos().get(0).getX());
    }

    @Test
    void documentoNovoOuCarregadoLimpaHistoricoESalvamentoExigeGestoConcluido() {
        EditorContext c = comCirculo();
        c.iniciarEdicao();
        assertThrows(IllegalStateException.class, c::exportarDocumento);
        c.cancelarEdicao();
        c.carregarDocumento(new DocumentoVetorial(900, 700, true, c.getFormas()));
        assertFalse(c.podeDesfazer());
        assertFalse(c.podeRefazer());
        assertFalse(c.temAlteracoesNaoSalvas());
        assertTrue(c.isGridSnappingHabilitado());
        assertEquals(900, c.exportarDocumento().larguraCanvas());
        c.setGridSnappingHabilitado(false);
        assertTrue(c.temAlteracoesNaoSalvas());
        c.setGridSnappingHabilitado(true);
        assertFalse(c.temAlteracoesNaoSalvas());
        c.selecionar(c.getFormas().get(0), false);
        assertFalse(c.temAlteracoesNaoSalvas());
        c.atualizarTamanhoCanvas(1000, 800);
        assertFalse(c.temAlteracoesNaoSalvas());
    }
}
