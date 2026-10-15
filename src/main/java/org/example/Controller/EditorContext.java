package org.example.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.function.Consumer;
import org.example.Models.FormaGeometrica;
import org.example.Models.Poligono;
import org.example.Models.Ponto;
import org.example.Models.FormaBase;
import org.example.Models.GrupoFormas;
import org.example.Models.Limites;
import org.example.Models.DocumentoVetorial;

/** Estado do documento e comunicação com a interface, sem dependência das Views. */
public class EditorContext {
    public static final double PASSO_GRADE = 20;
    private final List<FormaGeometrica> formas = new ArrayList<>();
    private final List<FormaGeometrica> selecionados = new ArrayList<>();
    private FormaGeometrica formaEmAndamento;
    private ModoFerramenta modo = ModoFerramenta.SELECIONAR;
    private Ferramenta ferramentaAtual = new FerramentaSelecionar();
    private boolean gridSnappingHabilitado;
    private String corPreenchimentoHex = "#8B9CF7";
    private String corBordaHex = "#24304A";
    private double espessuraBorda = 2;
    private double mouseX;
    private double mouseY;
    private Limites caixaSelecao;
    private final HistoricoController historico = new HistoricoController();
    private Map<FormaGeometrica, FormaGeometrica> edicaoInicial;
    private List<FormaGeometrica> formasSalvas = List.of();
    private boolean gradeSalva;
    private double larguraCanvas = 800;
    private double alturaCanvas = 600;
    private Runnable aoRedesenhar = () -> { };
    private Runnable aoAtualizarStatus = () -> { };
    private Runnable aoAtualizarInterface = () -> { };
    private Consumer<String> aoErro = mensagem -> { };

    public List<FormaGeometrica> getFormas() { return List.copyOf(formas); }
    public List<FormaGeometrica> getSelecionados() { return List.copyOf(selecionados); }
    public FormaGeometrica getFormaEmAndamento() { return formaEmAndamento; }
    public Ferramenta getFerramentaAtual() { return ferramentaAtual; }
    public ModoFerramenta getModo() { return modo; }
    public boolean isGridSnappingHabilitado() { return gridSnappingHabilitado; }
    public String getCorPreenchimentoHex() { return corPreenchimentoHex; }
    public String getCorBordaHex() { return corBordaHex; }
    public double getEspessuraBorda() { return espessuraBorda; }
    public double getMouseX() { return mouseX; }
    public double getMouseY() { return mouseY; }
    public Limites getCaixaSelecao() { return caixaSelecao; }
    public void setCaixaSelecao(Limites caixa) { caixaSelecao = caixa; requestRedraw(); }
    public boolean podeDesfazer() { return historico.podeDesfazer(); }
    public boolean podeRefazer() { return historico.podeRefazer(); }
    public boolean isEdicaoEmAndamento() { return edicaoInicial != null; }

    public boolean temAlteracoesNaoSalvas() {
        return formaEmAndamento != null || gridSnappingHabilitado != gradeSalva
                || !HistoricoController.iguais(formas, formasSalvas);
    }

    public void atualizarTamanhoCanvas(double largura, double altura) {
        if (Double.isFinite(largura) && Double.isFinite(altura) && largura > 0 && altura > 0) {
            larguraCanvas = largura;
            alturaCanvas = altura;
        }
    }

    public DocumentoVetorial exportarDocumento() {
        if (formaEmAndamento != null || isEdicaoEmAndamento()) {
            throw new IllegalStateException("Conclua ou cancele a operação em andamento antes de salvar.");
        }
        return new DocumentoVetorial(larguraCanvas, alturaCanvas, gridSnappingHabilitado, formas);
    }

    public void marcarSalvo() {
        formasSalvas = HistoricoController.copiar(formas);
        gradeSalva = gridSnappingHabilitado;
        atualizarInterface();
    }

    public void carregarDocumento(DocumentoVetorial documento) {
        List<FormaGeometrica> novas = documento.formas();
        ferramentaAtual.cancelar(this);
        formas.clear();
        formas.addAll(novas);
        selecionados.clear();
        formaEmAndamento = null;
        caixaSelecao = null;
        edicaoInicial = null;
        modo = ModoFerramenta.SELECIONAR;
        ferramentaAtual = new FerramentaSelecionar();
        gridSnappingHabilitado = documento.gradeMagnetica();
        larguraCanvas = documento.larguraCanvas();
        alturaCanvas = documento.alturaCanvas();
        historico.limpar();
        marcarSalvo();
    }

    /** O resize da janela é salvo como configuração, mas não é uma ação de desenho. */
    private List<FormaGeometrica> antesDaAlteracao() {
        return edicaoInicial == null ? HistoricoController.copiar(formas) : null;
    }

    private void concluirAlteracao(List<FormaGeometrica> antes) {
        if (antes != null && !HistoricoController.iguais(antes, formas)) historico.registrarEstado(antes);
        atualizarInterface();
    }

    public void executarComando(Comando comando) {
        List<FormaGeometrica> antes = antesDaAlteracao();
        comando.executar();
        concluirAlteracao(antes);
    }

    public void iniciarEdicao() {
        if (edicaoInicial != null) return;
        edicaoInicial = new LinkedHashMap<>();
        formas.forEach(f -> edicaoInicial.put(f, f.clonar()));
    }

    public void confirmarEdicao() {
        if (edicaoInicial == null) return;
        List<FormaGeometrica> antes = List.copyOf(edicaoInicial.values());
        edicaoInicial = null;
        concluirAlteracao(antes);
    }

    public void cancelarEdicao() {
        if (edicaoInicial == null) return;
        var antes = edicaoInicial;
        edicaoInicial = null;
        antes.forEach((forma, copia) -> {
            ((FormaBase) forma).restaurarGeometria(copia);
            forma.setCorPreenchimentoHex(copia.getCorPreenchimentoHex());
            forma.setCorBordaHex(copia.getCorBordaHex());
            forma.setEspessuraBorda(copia.getEspessuraBorda());
            forma.setGroupId(copia.getGroupId());
        });
        atualizarInterface();
    }

    public void desfazer() {
        cancelarDesenho();
        if (!podeDesfazer()) return;
        substituirFormas(historico.desfazer(formas));
    }

    public void refazer() {
        cancelarDesenho();
        if (!podeRefazer()) return;
        substituirFormas(historico.refazer(formas));
    }

    private void substituirFormas(List<FormaGeometrica> novas) {
        formas.clear();
        formas.addAll(novas);
        selecionados.clear();
        caixaSelecao = null;
        atualizarInterface();
    }

    public void setAoRedesenhar(Runnable acao) { aoRedesenhar = Objects.requireNonNull(acao); }
    public void setAoAtualizarStatus(Runnable acao) { aoAtualizarStatus = Objects.requireNonNull(acao); }
    public void setAoAtualizarInterface(Runnable acao) { aoAtualizarInterface = Objects.requireNonNull(acao); }
    public void setAoErro(Consumer<String> acao) { aoErro = Objects.requireNonNull(acao); }
    public void requestRedraw() { aoRedesenhar.run(); }
    public void requestStatsUpdate() { aoAtualizarStatus.run(); }
    public void informarErro(String mensagem) { aoErro.accept(mensagem); }

    public void atualizarInterface() {
        requestRedraw();
        requestStatsUpdate();
        aoAtualizarInterface.run();
    }

    public void setModo(ModoFerramenta novoModo) {
        Objects.requireNonNull(novoModo);
        ferramentaAtual.cancelar(this);
        modo = novoModo;
        ferramentaAtual = switch (novoModo) {
            case SELECIONAR -> new FerramentaSelecionar();
            case POLIGONO -> new FerramentaDesenhar();
            case CIRCULO, QUADRADO, HEXAGONO -> new FerramentaInserirForma(novoModo);
            case ESCALAR, ROTACIONAR, CISALHAR -> new FerramentaTransformar(novoModo);
            case EDITAR_VERTICE -> new FerramentaEditarVertice();
        };
        atualizarInterface();
    }

    public void setGridSnappingHabilitado(boolean habilitado) {
        gridSnappingHabilitado = habilitado;
        atualizarInterface();
    }

    public double aplicarSnap(double valor) {
        if (!Double.isFinite(valor)) throw new IllegalArgumentException("A coordenada deve ser finita.");
        return gridSnappingHabilitado ? Math.round(valor / PASSO_GRADE) * PASSO_GRADE : valor;
    }

    public Ponto pontoDoMouse(double x, double y) {
        return new Ponto(aplicarSnap(x), aplicarSnap(y));
    }

    public void atualizarMouse(double x, double y) {
        mouseX = aplicarSnap(x);
        mouseY = aplicarSnap(y);
        requestStatsUpdate();
        if (formaEmAndamento != null) requestRedraw();
    }

    public void aplicarEstilo(FormaGeometrica forma) {
        forma.setCorPreenchimentoHex(corPreenchimentoHex);
        forma.setCorBordaHex(corBordaHex);
        forma.setEspessuraBorda(espessuraBorda);
    }

    /** Os estilos escolhidos se aplicam à seleção e às próximas formas. */
    public void definirEstilo(String preenchimento, String borda, double espessura) {
        Poligono validador = new Poligono();
        validador.setCorPreenchimentoHex(preenchimento);
        validador.setCorBordaHex(borda);
        validador.setEspessuraBorda(espessura);
        List<FormaGeometrica> antes = antesDaAlteracao();
        corPreenchimentoHex = validador.getCorPreenchimentoHex();
        corBordaHex = validador.getCorBordaHex();
        espessuraBorda = espessura;
        selecionados.forEach(this::aplicarEstilo);
        if (formaEmAndamento != null) aplicarEstilo(formaEmAndamento);
        concluirAlteracao(antes);
    }

    public void setFormaEmAndamento(FormaGeometrica forma) {
        formaEmAndamento = forma;
        requestRedraw();
        requestStatsUpdate();
    }

    public void adicionarForma(FormaGeometrica forma) {
        Objects.requireNonNull(forma);
        if (forma instanceof Poligono poligono) poligono.validar();
        if (formas.contains(forma)) throw new IllegalArgumentException("A forma já pertence ao desenho.");
        List<FormaGeometrica> antes = antesDaAlteracao();
        formas.add(forma);
        formaEmAndamento = null;
        selecionados.clear();
        selecionados.add(forma);
        carregarEstiloDaSelecao();
        concluirAlteracao(antes);
    }

    /** Testa da frente para trás, seguindo a ordem de pintura. */
    public FormaGeometrica encontrarForma(double x, double y) {
        for (int i = formas.size() - 1; i >= 0; i--) {
            if (formas.get(i).contemPonto(x, y)) return formas.get(i);
        }
        return null;
    }

    public void selecionar(FormaGeometrica forma, boolean alternar) {
        if (forma != null && !formas.contains(forma)) {
            throw new IllegalArgumentException("A forma não pertence ao desenho.");
        }
        if (!alternar) selecionados.clear();
        if (forma != null) {
            List<FormaGeometrica> unidade = membrosDoGrupo(forma);
            if (alternar && selecionados.contains(forma)) selecionados.removeAll(unidade);
            else for (FormaGeometrica membro : unidade) if (!selecionados.contains(membro)) selecionados.add(membro);
        }
        carregarEstiloDaSelecao();
        atualizarInterface();
    }

    private void carregarEstiloDaSelecao() {
        if (selecionados.size() == 1) {
            FormaGeometrica forma = selecionados.get(0);
            corPreenchimentoHex = forma.getCorPreenchimentoHex();
            corBordaHex = forma.getCorBordaHex();
            espessuraBorda = forma.getEspessuraBorda();
        }
    }

    private List<FormaGeometrica> membrosDoGrupo(FormaGeometrica forma) {
        return forma.getGroupId() == null ? List.of(forma)
                : formas.stream().filter(f -> forma.getGroupId().equals(f.getGroupId())).toList();
    }

    public void selecionarLista(List<FormaGeometrica> lista) {
        if (!formas.containsAll(lista)) throw new IllegalArgumentException("Seleção fora do desenho.");
        selecionados.clear();
        for (FormaGeometrica forma : lista) {
            for (FormaGeometrica membro : membrosDoGrupo(forma)) {
                if (!selecionados.contains(membro)) selecionados.add(membro);
            }
        }
        carregarEstiloDaSelecao();
        atualizarInterface();
    }

    public void selecionarCaixa(Limites caixa, List<FormaGeometrica> selecaoAnterior) {
        List<FormaGeometrica> resultado = new ArrayList<>(selecaoAnterior);
        for (FormaGeometrica forma : formas) {
            if (caixa.contem(Limites.de(forma)) && !resultado.contains(forma)) resultado.add(forma);
        }
        selecionarLista(resultado);
    }

    public void agruparSelecionados() {
        if (selecionados.size() < 2) throw new IllegalArgumentException("Selecione pelo menos duas formas para agrupar.");
        String id = UUID.randomUUID().toString();
        executarComando(() -> selecionados.forEach(f -> f.setGroupId(id)));
    }

    public void desagruparSelecionados() {
        executarComando(() -> selecionados.forEach(f -> f.setGroupId(null)));
    }

    public Ponto getCentroSelecao() { return new GrupoFormas(selecionados).getCentroide(); }

    public Map<FormaGeometrica, FormaGeometrica> capturarSelecao() {
        Map<FormaGeometrica, FormaGeometrica> estado = new LinkedHashMap<>();
        selecionados.forEach(f -> estado.put(f, f.clonar()));
        return estado;
    }

    public void restaurarGeometrias(Map<FormaGeometrica, FormaGeometrica> estado) {
        estado.forEach((destino, origem) -> ((FormaBase) destino).restaurarGeometria(origem));
        atualizarInterface();
    }

    public void transformarSelecionados(ModoFerramenta tipo, double a, double b) {
        transformarDesde(capturarSelecao(), tipo, a, b);
    }

    /** Calcula tudo em cópias antes de confirmar, evitando transformações parciais. */
    public void transformarDesde(Map<FormaGeometrica, FormaGeometrica> estado,
            ModoFerramenta tipo, double a, double b) {
        if (estado.isEmpty()) throw new IllegalArgumentException("Selecione uma forma para transformar.");
        if (tipo != ModoFerramenta.ESCALAR && tipo != ModoFerramenta.ROTACIONAR && tipo != ModoFerramenta.CISALHAR) {
            throw new IllegalArgumentException("Modo de transformação inválido.");
        }
        Map<FormaGeometrica, FormaGeometrica> novas = new LinkedHashMap<>();
        estado.forEach((destino, origem) -> novas.put(destino, origem.clonar()));
        Map<Object, List<FormaGeometrica>> unidades = new LinkedHashMap<>();
        for (FormaGeometrica forma : novas.values()) {
            Object chave = forma.getGroupId() == null ? forma : forma.getGroupId();
            unidades.computeIfAbsent(chave, id -> new ArrayList<>()).add(forma);
        }
        for (List<FormaGeometrica> membros : unidades.values()) {
            GrupoFormas grupo = new GrupoFormas(membros);
            switch (tipo) {
                case ESCALAR -> grupo.escalar(a);
                case ROTACIONAR -> grupo.rotacionar(a);
                case CISALHAR -> grupo.cisalhar(a, b);
                default -> throw new IllegalStateException();
            }
        }
        for (FormaGeometrica forma : novas.values()) {
            double area = forma.calcularArea();
            if (!(area > 0) || !Double.isFinite(area) || !Double.isFinite(forma.calcularPerimetro())) {
                throw new IllegalArgumentException("A transformação produz uma forma degenerada ou grande demais.");
            }
        }
        List<FormaGeometrica> antes = antesDaAlteracao();
        novas.forEach((destino, origem) -> ((FormaBase) destino).restaurarGeometria(origem));
        concluirAlteracao(antes);
    }

    public void moverSelecionados(double dx, double dy) {
        if (dx == 0 && dy == 0) return;
        Map<FormaGeometrica, FormaGeometrica> novas = capturarSelecao();
        novas.values().forEach(forma -> forma.transladar(dx, dy));
        List<FormaGeometrica> antes = antesDaAlteracao();
        novas.forEach((destino, origem) -> ((FormaBase) destino).restaurarGeometria(origem));
        concluirAlteracao(antes);
    }

    public void excluirSelecionados() {
        if (isEdicaoEmAndamento()) {
            ferramentaAtual.cancelar(this);
            cancelarEdicao();
        }
        executarComando(() -> {
            formas.removeAll(selecionados);
            selecionados.clear();
        });
    }

    public void trazerParaFrente() {
        List<FormaGeometrica> mover = formas.stream().filter(selecionados::contains).toList();
        executarComando(() -> {
            formas.removeAll(mover);
            formas.addAll(mover);
        });
    }

    public void enviarParaTras() {
        List<FormaGeometrica> mover = formas.stream().filter(selecionados::contains).toList();
        executarComando(() -> {
            formas.removeAll(mover);
            formas.addAll(0, mover);
        });
    }

    public void concluirDesenho() { ferramentaAtual.concluir(this); }
    public void cancelarDesenho() { ferramentaAtual.cancelar(this); }
}
