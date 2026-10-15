"""Gera o UML em PDF e fontes PlantUML. Requer Python 3 e reportlab.

As definições abaixo são a fonte comum dos dois formatos. Edite-as e execute
python scripts/gerar_uml.py para atualizar os diagramas da implementação.
"""
from pathlib import Path
from math import atan2, cos, sin
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A3, landscape
from reportlab.lib.colors import HexColor, white
from reportlab.pdfbase.pdfmetrics import stringWidth

ROOT = Path(__file__).resolve().parents[1]
W, H = landscape(A3)


def box(name, x, y, w, h, fields=(), methods=(), kind="class"):
    return dict(name=name, x=x, y=y, w=w, h=h, fields=fields, methods=methods, kind=kind)


PAGES = [
    dict(title="01 / Modelos geométricos", package="org.example.Models", boxes=[
        box("FormaGeometrica", 40, 115, 310, 385, methods=[
            "+ adicionarPonto(Ponto)", "+ getPontos(): List<Ponto>", "+ getQuantidadePontos(): int",
            "+ getCorPreenchimentoHex(): String", "+ setCorPreenchimentoHex(String)",
            "+ getCorBordaHex(): String", "+ setCorBordaHex(String)",
            "+ getEspessuraBorda(): double", "+ setEspessuraBorda(double)",
            "+ getGroupId(): String", "+ setGroupId(String)",
            "+ calcularArea(): double", "+ calcularPerimetro(): double", "+ getCentroide(): Ponto",
            "+ contemPonto(double, double): boolean", "+ transladar(double, double)",
            "+ escalar(double)", "+ rotacionar(double)", "+ cisalhar(double, double)",
            "+ clonar(): FormaGeometrica"], kind="interface"),
        box("FormaBase", 420, 115, 300, 255, fields=[
            "# pontos: List<Ponto>", "- corPreenchimentoHex: String", "- corBordaHex: String",
            "- espessuraBorda: double", "- groupId: String"], methods=[
            "+ getPontos(): List<Ponto>", "+ transladar(double, double)", "+ escalar(double)",
            "+ rotacionar(double)", "+ cisalhar(double, double)",
            "+ restaurarGeometria(FormaGeometrica)"], kind="abstract"),
        box("Ponto", 830, 115, 310, 170, fields=["- x: double", "- y: double"], methods=[
            "+ Ponto(double, double)", "+ getX()/getY(): double", "+ mover(double, double)",
            "+ distancia(Ponto): double", "+ clonar(): Ponto"]),
        box("Poligono", 420, 455, 300, 160, methods=[
            "+ Poligono() / Poligono(List<Ponto>)", "+ adicionarPonto(Ponto)", "+ validar()",
            "+ editarVertice(int, Ponto)", "+ calcularArea()/calcularPerimetro(): double",
            "+ getCentroide(): Ponto", "+ clonar(): Poligono"]),
        box("Circulo", 830, 455, 310, 160, methods=[
            "+ Circulo(Ponto, double)", "+ {static} dePontosControle(List<Ponto>): Circulo",
            "+ calcularArea()/calcularPerimetro(): double", "+ contemPonto(double, double): boolean",
            "+ getCentroide(): Ponto", "+ clonar(): Circulo"]),
        box("Quadrado", 390, 705, 220, 75, methods=["+ Quadrado(Ponto, double)", "+ clonar(): Quadrado"]),
        box("Hexagono", 650, 705, 220, 75, methods=["+ Hexagono(Ponto, double)", "+ clonar(): Hexagono"]),
    ], edges=[
        ([(420, 220), (350, 220)], "realize"),
        ([(720, 205), (830, 205)], "composition"),
        ([(570, 455), (570, 370)], "inherit"),
        ([(985, 455), (985, 415), (670, 415), (670, 370)], "inherit"),
        ([(500, 705), (500, 615)], "inherit"),
        ([(760, 705), (760, 660), (650, 660), (650, 615)], "inherit"),
    ], relations=["FormaBase ..|> FormaGeometrica", "FormaBase *-- \"0..*\" Ponto",
                  "Poligono --|> FormaBase", "Circulo --|> FormaBase",
                  "Quadrado --|> Poligono", "Hexagono --|> Poligono"],
        note="Prototype: todos os clones preservam tipo e copiam os pontos. Circulo usa centro e dois controles de raio; suporta elipses após cisalhamento."),
    dict(title="02 / Ferramentas e padrão State", package="org.example.Controller", boxes=[
        box("EditorContext", 40, 115, 320, 260, fields=[
            "- formas: List<FormaGeometrica>", "- selecionados: List<FormaGeometrica>",
            "- formaEmAndamento: FormaGeometrica", "- ferramentaAtual: Ferramenta", "- modo: ModoFerramenta"],
            methods=["+ setModo(ModoFerramenta)", "+ selecionar(FormaGeometrica, boolean)",
                     "+ moverSelecionados(double, double)", "+ iniciarEdicao()/confirmarEdicao()",
                     "+ cancelarEdicao()", "+ requestRedraw()/requestStatsUpdate()"]),
        box("Ferramenta", 420, 115, 455, 205, methods=[
            "+ aoPressionarMouse(MouseEvent, EditorContext)", "+ aoArrastarMouse(MouseEvent, EditorContext)",
            "+ aoSoltarMouse(MouseEvent, EditorContext)", "+ aoClicar(MouseEvent, EditorContext)",
            "+ concluir(EditorContext)", "+ cancelar(EditorContext)", "+ temEdicaoEmAndamento(): boolean"], kind="interface"),
        box("ModoFerramenta", 935, 115, 210, 205, fields=[
            "SELECIONAR", "POLIGONO", "CIRCULO", "QUADRADO", "HEXAGONO", "ESCALAR",
            "ROTACIONAR", "CISALHAR", "EDITAR_VERTICE"], kind="enum"),
        box("FerramentaDesenhar", 40, 500, 210, 205, fields=["- poligono: Poligono"], methods=[
            "+ aoClicar(MouseEvent, EditorContext)", "+ concluir(EditorContext)", "+ cancelar(EditorContext)"]),
        box("FerramentaInserirForma", 265, 500, 210, 205, fields=[
            "- modo: ModoFerramenta", "- centroInsercao: Ponto"], methods=[
            "+ aoPressionarMouse(...)", "+ aoArrastarMouse(...)", "+ aoSoltarMouse(...)"]),
        box("FerramentaSelecionar", 490, 500, 210, 205, fields=[
            "- ultimoPonto: Ponto", "- inicioCaixa: Ponto", "- selecaoAnterior: List<FormaGeometrica>"], methods=[
            "+ aoPressionarMouse(...)", "+ aoArrastarMouse(...)", "+ aoSoltarMouse(...)"]),
        box("FerramentaTransformar", 715, 500, 210, 205, fields=[
            "- modo: ModoFerramenta", "- estadoInicial: Map<FormaGeometrica,", "  FormaGeometrica>",
            "- inicio/centro: Ponto"], methods=[
            "+ aoPressionarMouse(...)", "+ aoArrastarMouse(...)", "+ cancelar(EditorContext)"]),
        box("FerramentaEditarVertice", 940, 500, 210, 205, fields=[
            "- forma: Poligono", "- indice: int"], methods=[
            "+ aoPressionarMouse(...)", "+ aoArrastarMouse(...)", "+ aoSoltarMouse(...)", "+ cancelar(EditorContext)"]),
    ], edges=[([(360, 220), (420, 220)], "aggregation")]
        + [([(x, 500), (x, 430), (647.5, 430), (647.5, 320)], "realize")
           for x in [145, 370, 595, 820, 1045]],
        relations=["EditorContext o-- Ferramenta", "EditorContext --> ModoFerramenta"]
        + [f"{name} ..|> Ferramenta" for name in ["FerramentaDesenhar", "FerramentaInserirForma",
            "FerramentaSelecionar", "FerramentaTransformar", "FerramentaEditarVertice"]],
        note="State: a View delega os eventos à ferramenta ativa. Assinaturas com (...) usam MouseEvent e EditorContext; operações de grupo e histórico estão na página 3."),
    dict(title="03 / Histórico, grupos e persistência", package="org.example.Controller + org.example.Models", boxes=[
        box("EditorContext", 40, 115, 310, 160, methods=[
            "+ executarComando(Comando)", "+ desfazer()/refazer()", "+ agruparSelecionados()/desagruparSelecionados()",
            "+ transformarSelecionados(ModoFerramenta, double, double)",
            "+ exportarDocumento(): DocumentoVetorial", "+ carregarDocumento(DocumentoVetorial)"]),
        box("Comando", 430, 115, 260, 100, methods=["+ executar()"], kind="interface"),
        box("HistoricoController", 800, 115, 340, 200, fields=[
            "- undoStack: Deque<List<FormaGeometrica>>", "- redoStack: Deque<List<FormaGeometrica>>"], methods=[
            "+ registrarEstado(List<FormaGeometrica>)", "+ desfazer(List<FormaGeometrica>): List<FormaGeometrica>",
            "+ refazer(List<FormaGeometrica>): List<FormaGeometrica>",
            "+ podeDesfazer()/podeRefazer(): boolean", "+ limpar()"]),
        box("GrupoFormas", 40, 400, 310, 245, fields=["- membros: List<FormaGeometrica>"], methods=[
            "+ GrupoFormas(List<FormaGeometrica>)", "+ getCentroide(): Ponto", "+ transladar(double, double)",
            "+ escalar(double)", "+ rotacionar(double)", "+ cisalhar(double, double)",
            "+ calcularArea()/calcularPerimetro(): double", "+ clonar(): GrupoFormas"]),
        box("DocumentoVetorial", 430, 400, 310, 180, fields=[
            "larguraCanvas: double", "alturaCanvas: double", "gradeMagnetica: boolean", "formas: List<FormaGeometrica>"],
            methods=["+ formas(): List<FormaGeometrica>"], kind="record"),
        box("ArquivoController", 810, 400, 330, 180, fields=["- gson: Gson"], methods=[
            "+ salvar(Path, DocumentoVetorial)", "+ carregar(Path): DocumentoVetorial"]),
        box("FormaGeometrica", 40, 690, 310, 90, methods=["+ clonar(): FormaGeometrica"], kind="interface"),
        box("Limites", 430, 660, 310, 120, fields=["minX, minY, maxX, maxY: double"], methods=[
            "+ {static} entre(Ponto, Ponto): Limites", "+ {static} de(FormaGeometrica): Limites",
            "+ contem(Limites): boolean"], kind="record"),
    ], edges=[
        ([(350, 180), (430, 180)], "dependency"),
        ([(350, 245), (380, 245), (380, 350), (970, 350), (970, 315)], "composition"),
        ([(195, 275), (195, 400)], "dependency"),
        ([(810, 480), (740, 480)], "dependency"),
        ([(145, 645), (145, 690)], "composition"),
        ([(245, 645), (245, 690)], "realize"),
    ], relations=["EditorContext ..> Comando", "EditorContext *-- HistoricoController",
        "EditorContext ..> GrupoFormas", "GrupoFormas *-- \"1..*\" FormaGeometrica",
        "GrupoFormas ..|> FormaGeometrica", "ArquivoController ..> DocumentoVetorial",
        "DocumentoVetorial o-- FormaGeometrica", "EditorContext ..> Limites"],
        note="Memento guarda cópias profundas. Command usa operações encapsuladas. Composite transforma grupos por um centro comum. JSON versão 1 preserva ordem, estilos e groupId."),
    dict(title="04 / Interface e separação MVC", package="org.example.Views", boxes=[
        box("EditorContext", 40, 115, 310, 160, methods=[
            "+ setAoRedesenhar(Runnable)", "+ setAoAtualizarStatus(Runnable)", "+ setAoAtualizarInterface(Runnable)",
            "+ setAoErro(Consumer<String>)"]),
        box("ContainerApp", 420, 115, 340, 215, fields=[
            "- contexto: EditorContext", "- areaDeDesenho: Canvas", "- barraLateral: BarraLateralView",
            "- painelStatus: PainelStatusView", "- barraMenu: BarraMenuView"], methods=[
            "+ start(Stage)", "+ inicializarEventos()", "- redesenharCanvas()",
            "- desenharForma(FormaGeometrica, boolean)"]),
        box("ArquivoController", 840, 115, 310, 160, methods=[
            "+ salvar(Path, DocumentoVetorial)", "+ carregar(Path): DocumentoVetorial"]),
        box("BarraLateralView", 40, 450, 310, 175, fields=["- contexto: EditorContext"], methods=[
            "+ BarraLateralView(EditorContext)", "+ atualizar()", "+ atualizarDesenhoEmAndamento()",
            "- construirSecaoFerramentas()", "- construirSecaoTransformacoes()"]),
        box("PainelStatusView", 440, 450, 310, 175, fields=["- contexto: EditorContext"], methods=[
            "+ PainelStatusView(EditorContext)", "+ atualizar()"]),
        box("BarraMenuView", 840, 450, 310, 205, fields=[
            "- contexto: EditorContext", "- arquivos: ArquivoController", "- arquivoAtual: Path"], methods=[
            "+ atualizar()", "- salvarArquivo(boolean): boolean", "- carregarArquivo()",
            "- novoArquivo()", "- confirmarAlteracoes(): boolean"]),
    ], edges=[
        ([(420, 200), (350, 200)], "dependency"),
        ([(480, 330), (480, 390), (195, 390), (195, 450)], "composition"),
        ([(595, 330), (595, 450)], "composition"),
        ([(700, 330), (700, 410), (995, 410), (995, 450)], "composition"),
        ([(1150, 560), (1170, 560), (1170, 195), (1150, 195)], "dependency"),
    ], relations=["ContainerApp ..> EditorContext", "ContainerApp *-- BarraLateralView",
        "ContainerApp *-- PainelStatusView", "ContainerApp *-- BarraMenuView",
        "BarraMenuView ..> ArquivoController", "BarraLateralView ..> EditorContext",
        "PainelStatusView ..> EditorContext", "BarraMenuView ..> EditorContext"],
        note="Models não importam JavaFX. Controllers não importam Views. Views compõem a janela, desenham e exibem diálogos; callbacks atualizam a interface sem acoplamento inverso."),
]


def render_box(c, b):
    x, top, w, h = (b[k] for k in ["x", "y", "w", "h"])
    y = H - top - h
    c.setStrokeColor(HexColor("#7789A3"))
    c.setFillColor(white)
    c.rect(x, y, w, h, fill=1, stroke=1)
    head = 38 if b["kind"] != "class" else 28
    c.setFillColor(HexColor("#E9EFF9"))
    c.rect(x, H - top - head, w, head, fill=1, stroke=1)
    c.setFillColor(HexColor("#172D4A"))
    if b["kind"] != "class":
        c.setFont("Helvetica", 8)
        c.drawCentredString(x + w / 2, H - top - 12, f'<<{b["kind"]}>>')
    c.setFont("Helvetica-Bold", 11)
    c.drawCentredString(x + w / 2, H - top - head + 10, b["name"])
    baseline = H - top - head - 15
    c.setFont("Courier", 7.5)
    for section in ["fields", "methods"]:
        if section == "methods" and b["fields"] and b["methods"]:
            c.line(x, baseline + 7, x + w, baseline + 7)
            baseline -= 6
        for text in b[section]:
            assert stringWidth(text, "Courier", 7.5) <= w - 16, (b["name"], text)
            assert baseline >= y + 6, (b["name"], "altura insuficiente")
            c.drawString(x + 8, baseline, text)
            baseline -= 12


def edge(c, route, kind):
    route = [(x, H - y) for x, y in route]
    c.setStrokeColor(HexColor("#526680"))
    c.setLineWidth(1.1)
    c.setDash(5, 3) if kind in ["realize", "dependency"] else c.setDash()
    p = c.beginPath()
    p.moveTo(*route[0])
    for point in route[1:]:
        p.lineTo(*point)
    c.drawPath(p)
    c.setDash()
    if kind in ["inherit", "realize", "dependency"]:
        end, before = route[-1], route[-2]
        angle = atan2(end[1] - before[1], end[0] - before[0])
        ux, uy = cos(angle), sin(angle)
        size = 11 if kind != "dependency" else 8
        a = (end[0] - size * ux + 5 * uy, end[1] - size * uy - 5 * ux)
        b = (end[0] - size * ux - 5 * uy, end[1] - size * uy + 5 * ux)
        p = c.beginPath()
        p.moveTo(*a)
        p.lineTo(*end)
        p.lineTo(*b)
        if kind != "dependency":
            p.close()
            c.setFillColor(white)
            c.drawPath(p, fill=1)
        else:
            c.drawPath(p)
    if kind in ["composition", "aggregation"]:
        start, after = route[0], route[1]
        angle = atan2(after[1] - start[1], after[0] - start[0])
        ux, uy = cos(angle), sin(angle)
        p = c.beginPath()
        p.moveTo(*start)
        p.lineTo(start[0] + 7 * ux + 4 * uy, start[1] + 7 * uy - 4 * ux)
        p.lineTo(start[0] + 14 * ux, start[1] + 14 * uy)
        p.lineTo(start[0] + 7 * ux - 4 * uy, start[1] + 7 * uy + 4 * ux)
        p.close()
        c.setFillColor(HexColor("#526680") if kind == "composition" else white)
        c.drawPath(p, fill=1)


def main():
    output = ROOT / "output/pdf/diagrama_classes_implementado.pdf"
    output.parent.mkdir(parents=True, exist_ok=True)
    source_dir = ROOT / "docs/uml"
    source_dir.mkdir(parents=True, exist_ok=True)
    c = canvas.Canvas(str(output), pagesize=(W, H))
    c.setTitle("Editor Vetorial - Diagrama de classes da implementação")
    c.setAuthor("Projeto acadêmico - Editor Vetorial")
    for number, page in enumerate(PAGES, 1):
        c.setFillColor(HexColor("#172D4A"))
        c.setFont("Helvetica-Bold", 22)
        c.drawString(40, H - 45, "Editor Vetorial | UML da implementação")
        c.setFont("Helvetica", 12)
        c.drawString(40, H - 70, page["title"] + " - " + page["package"])
        for b in page["boxes"]:
            render_box(c, b)
        for route, kind in page["edges"]:
            edge(c, route, kind)
        c.setFillColor(HexColor("#46576B"))
        c.setFont("Helvetica", 8)
        c.drawString(40, 32, page["note"])
        c.setFont("Helvetica", 7.5)
        c.drawString(40, 17, "Membros principais; getters/setters auxiliares omitidos. Triângulo: herança/realização. Losango: composição/agregação. Linha tracejada: dependência/realização.")
        c.drawRightString(W - 40, 17, f"{number} / {len(PAGES)}")
        c.showPage()
        lines = [f"@startuml pagina_{number}", "skinparam classAttributeIconSize 0", f'title {page["title"]}']
        for b in page["boxes"]:
            keyword = {"abstract": "abstract class", "record": "class"}.get(b["kind"], b["kind"])
            suffix = " <<record>>" if b["kind"] == "record" else ""
            lines.append(f'{keyword} {b["name"]}{suffix} {{')
            for line in b["fields"]:
                if line.startswith("  "):
                    lines[-1] += line.strip()
                else:
                    lines.append("  " + line)
            if b["fields"] and b["methods"]:
                lines.append("  --")
            lines += ["  " + line for line in b["methods"]]
            lines.append("}")
        lines += page["relations"] + ["@enduml", ""]
        (source_dir / f"pagina_{number}.puml").write_text("\n".join(lines), encoding="utf-8")
    c.save()
    print(output)


if __name__ == "__main__":
    main()
