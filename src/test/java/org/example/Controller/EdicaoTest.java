package org.example.Controller;

import java.util.List;
import javafx.event.EventType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.Models.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EdicaoTest {
    private static final double EPS = 1e-7;

    private MouseEvent mouse(EventType<MouseEvent> tipo, double x, double y) {
        return new MouseEvent(tipo, x, y, x, y, MouseButton.PRIMARY, 1,
                false, false, false, false, tipo != MouseEvent.MOUSE_RELEASED,
                false, false, false, false, true, null);
    }

    private void pressionar(EditorContext c, double x, double y) {
        c.getFerramentaAtual().aoPressionarMouse(mouse(MouseEvent.MOUSE_PRESSED, x, y), c);
    }
    private void arrastar(EditorContext c, double x, double y) {
        c.getFerramentaAtual().aoArrastarMouse(mouse(MouseEvent.MOUSE_DRAGGED, x, y), c);
    }
    private void soltar(EditorContext c, double x, double y) {
        c.getFerramentaAtual().aoSoltarMouse(mouse(MouseEvent.MOUSE_RELEASED, x, y), c);
    }

    private EditorContext par(boolean agrupar) {
        EditorContext c = new EditorContext();
        c.adicionarForma(new Circulo(new Ponto(100, 100), 10));
        c.adicionarForma(new Circulo(new Ponto(300, 100), 10));
        c.selecionarLista(c.getFormas());
        if (agrupar) c.agruparSelecionados();
        return c;
    }

    @Test
    void selecionarEMoverUmMembroIncluiGrupoInteiro() {
        EditorContext c = par(true);
        var a = c.getFormas().get(0);
        var b = c.getFormas().get(1);
        assertEquals(a.getGroupId(), b.getGroupId());
        c.selecionar(a, false);
        assertEquals(2, c.getSelecionados().size());
        c.moverSelecionados(10, 20);
        assertCentro(a, 110, 120);
        assertCentro(b, 310, 120);
        c.selecionar(b, true);
        assertTrue(c.getSelecionados().isEmpty());
    }

    @Test
    void escalaGrupoEmTornoDeCentroComum() {
        EditorContext c = par(true);
        c.transformarSelecionados(ModoFerramenta.ESCALAR, 2, 0);
        assertCentro(c.getFormas().get(0), 0, 100);
        assertCentro(c.getFormas().get(1), 400, 100);
        assertEquals(400 * Math.PI, c.getFormas().get(0).calcularArea(), EPS);
        assertEquals(200, c.getCentroSelecao().getX(), EPS);
    }

    @Test
    void rotacionaGrupoComPosicoesRelativas() {
        EditorContext c = par(true);
        c.transformarSelecionados(ModoFerramenta.ROTACIONAR, Math.PI / 2, 0);
        assertCentro(c.getFormas().get(0), 200, 0);
        assertCentro(c.getFormas().get(1), 200, 200);
        assertEquals(100 * Math.PI, c.getFormas().get(0).calcularArea(), EPS);
    }

    @Test
    void cisalhaGrupoIncluindoDistanciaEntreMembros() {
        EditorContext c = par(true);
        c.transformarSelecionados(ModoFerramenta.CISALHAR, 0.5, 0.25);
        assertCentro(c.getFormas().get(0), 100, 75);
        assertCentro(c.getFormas().get(1), 300, 125);
        assertEquals(100 * Math.PI * 0.875, c.getFormas().get(0).calcularArea(), EPS);
    }

    @Test
    void formasSemGrupoUsamSeusPropriosCentros() {
        EditorContext c = par(false);
        c.transformarSelecionados(ModoFerramenta.ESCALAR, 2, 0);
        assertCentro(c.getFormas().get(0), 100, 100);
        assertCentro(c.getFormas().get(1), 300, 100);
    }

    @Test
    void desagruparPermiteSelecionarMembrosIndividualmente() {
        EditorContext c = par(true);
        c.desagruparSelecionados();
        c.selecionar(c.getFormas().get(0), false);
        assertEquals(1, c.getSelecionados().size());
        assertTrue(c.getFormas().stream().allMatch(f -> f.getGroupId() == null));
        assertThrows(IllegalArgumentException.class, c::agruparSelecionados);
    }

    @Test
    void reagruparNaoDeixaMembrosDeGrupoAnteriorParaTras() {
        EditorContext c = par(true);
        String anterior = c.getFormas().get(0).getGroupId();
        var extra = new Quadrado(new Ponto(500, 100), 20);
        c.adicionarForma(extra);
        c.selecionar(c.getFormas().get(0), true);
        c.agruparSelecionados();
        String novo = extra.getGroupId();
        assertNotEquals(anterior, novo);
        assertTrue(c.getFormas().stream().allMatch(f -> novo.equals(f.getGroupId())));
    }

    @Test
    void transformacaoInvalidaNaoAlteraNenhumMembro() {
        EditorContext c = par(true);
        assertThrows(IllegalArgumentException.class, () -> c.transformarSelecionados(ModoFerramenta.CISALHAR, 2, 0.5));
        assertThrows(IllegalArgumentException.class, () -> c.transformarSelecionados(ModoFerramenta.ESCALAR, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> c.transformarSelecionados(ModoFerramenta.ESCALAR, Double.MAX_VALUE, 0));
        assertCentro(c.getFormas().get(0), 100, 100);
        assertCentro(c.getFormas().get(1), 300, 100);
        assertEquals(100 * Math.PI, c.getFormas().get(0).calcularArea(), EPS);
    }

    @Test
    void caixaSelecionaSomenteFormasCompletamenteContidas() {
        EditorContext c = par(false);
        c.selecionar(null, false);
        pressionar(c, 70, 70);
        arrastar(c, 130, 130);
        assertNotNull(c.getCaixaSelecao());
        soltar(c, 130, 130);
        assertEquals(List.of(c.getFormas().get(0)), c.getSelecionados());
        assertNull(c.getCaixaSelecao());
        c.selecionarCaixa(new Limites(95, 95, 105, 105), List.of());
        assertTrue(c.getSelecionados().isEmpty());
    }

    @Test
    void caixaDeUmMembroSelecionaGrupoTodoECancelamentoRestauraSelecao() {
        EditorContext c = par(true);
        c.selecionar(null, false);
        c.selecionarCaixa(new Limites(80, 80, 120, 120), List.of());
        assertEquals(2, c.getSelecionados().size());
        pressionar(c, 20, 20);
        arrastar(c, 40, 40);
        c.cancelarDesenho();
        assertEquals(2, c.getSelecionados().size());
        assertNull(c.getCaixaSelecao());
    }

    @Test
    void caixaAditivaPreservaShiftDoInicioDoGesto() {
        EditorContext c = par(false);
        c.selecionar(c.getFormas().get(1), false);
        MouseEvent inicio = new MouseEvent(MouseEvent.MOUSE_PRESSED, 70, 70, 70, 70,
                MouseButton.PRIMARY, 1, true, false, false, false, true,
                false, false, false, false, true, null);
        c.getFerramentaAtual().aoPressionarMouse(inicio, c);
        arrastar(c, 130, 130);
        soltar(c, 130, 130);
        assertEquals(2, c.getSelecionados().size());
    }

    @Test
    void caixaUsaLimitesReaisDaElipseCisalhada() {
        EditorContext c = new EditorContext();
        Circulo circulo = new Circulo(new Ponto(100, 100), 10);
        circulo.cisalhar(1, 0);
        c.adicionarForma(circulo);
        c.selecionarCaixa(new Limites(88, 88, 112, 112), List.of());
        assertTrue(c.getSelecionados().isEmpty());
        c.selecionarCaixa(new Limites(80, 80, 120, 120), List.of());
        assertEquals(List.of(circulo), c.getSelecionados());
    }

    @Test
    void arrastoDeEscalaUsaEstadoInicialESuportaEscape() {
        EditorContext c = par(true);
        c.setModo(ModoFerramenta.ESCALAR);
        pressionar(c, 350, 100);
        arrastar(c, 500, 100);
        assertCentro(c.getFormas().get(0), 0, 100);
        arrastar(c, 650, 100);
        assertCentro(c.getFormas().get(0), -100, 100);
        assertTrue(c.getFerramentaAtual().temEdicaoEmAndamento());
        c.cancelarDesenho();
        assertCentro(c.getFormas().get(0), 100, 100);
        assertFalse(c.getFerramentaAtual().temEdicaoEmAndamento());
        soltar(c, 650, 100);
        assertCentro(c.getFormas().get(0), 100, 100);
    }

    @Test
    void arrastoDeRotacaoECisalhamentoAplicamAoSoltar() {
        EditorContext c = par(true);
        c.setModo(ModoFerramenta.ROTACIONAR);
        pressionar(c, 350, 100);
        soltar(c, 200, 250);
        assertCentro(c.getFormas().get(0), 200, 0);
        assertCentro(c.getFormas().get(1), 200, 200);
        c.setModo(ModoFerramenta.CISALHAR);
        pressionar(c, 400, 400);
        soltar(c, 550, 400);
        assertCentro(c.getFormas().get(0), 100, 0);
        assertCentro(c.getFormas().get(1), 300, 200);
    }

    @Test
    void editarVerticeRespeitaSnapEPermiteCancelar() {
        EditorContext c = new EditorContext();
        Poligono p = new Poligono(List.of(new Ponto(100, 100), new Ponto(200, 100), new Ponto(100, 200)));
        c.adicionarForma(p);
        c.setGridSnappingHabilitado(true);
        c.setModo(ModoFerramenta.EDITAR_VERTICE);
        pressionar(c, 100, 100);
        arrastar(c, 78, 82);
        assertEquals(80, p.getPontos().get(0).getX());
        assertEquals(80, p.getPontos().get(0).getY());
        c.cancelarDesenho();
        assertEquals(100, p.getPontos().get(0).getX());
        pressionar(c, 100, 100);
        soltar(c, 80, 80);
        assertEquals(80, p.getPontos().get(0).getX());
        assertFalse(c.getFerramentaAtual().temEdicaoEmAndamento());
    }

    @Test
    void editarVerticeInvalidoMantemUltimaGeometriaValida() {
        EditorContext c = new EditorContext();
        Poligono p = new Poligono(List.of(new Ponto(100, 100), new Ponto(200, 100), new Ponto(100, 200)));
        c.adicionarForma(p);
        c.setModo(ModoFerramenta.EDITAR_VERTICE);
        pressionar(c, 100, 100);
        soltar(c, 150, 150);
        assertEquals(5000, p.calcularArea(), EPS);
        assertEquals(100, p.getPontos().get(0).getX());
    }

    private void assertCentro(FormaGeometrica f, double x, double y) {
        assertEquals(x, f.getCentroide().getX(), EPS);
        assertEquals(y, f.getCentroide().getY(), EPS);
    }
}
