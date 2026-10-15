# Editor Vetorial

Projeto acadêmico de Programação Orientada a Objetos: editor vetorial desktop em JavaFX, organizado em MVC e construído com Maven.

## Requisitos

- JDK 17 ou superior (com `java` e `javac` disponíveis no PATH).
- Conexão com a internet na primeira execução para baixar o Maven e as dependências.

O Maven Wrapper incluído dispensa a instalação manual do Maven. Se necessário, configure `JAVA_HOME` com o diretório do JDK.

## Executar

No PowerShell, na raiz do projeto:

```powershell
.\mvnw.cmd javafx:run
```

No Linux ou macOS:

```sh
sh ./mvnw javafx:run
```

## Compilar e verificar

```powershell
.\mvnw.cmd clean verify
```

O Maven grava o resultado em `target/`. Nesta etapa, o JAR gerado não é um aplicativo independente: execute a janela pelo comando `javafx:run`.

## Organização

```text
src/main/java/org/example/
├── Models/       # Modelos geométricos e regras matemáticas
├── Controller/   # Contexto, ferramentas e histórico
└── Views/        # Interface JavaFX e classe inicial ContainerApp
provisorio/       # Enunciado e diagrama de classes fornecidos
pom.xml           # Dependências e configuração da construção
```

Os nomes dos pacotes seguem o enunciado, inclusive as letras maiúsculas.

## Estado atual

Etapa 1 concluída: estrutura Maven, JavaFX e janela inicial com área de desenho redimensionável, barra lateral e status.

Etapa 2 concluída: modelos `Ponto`, `FormaGeometrica`, `FormaBase`, `Poligono`, `Circulo`, `Quadrado` e `Hexagono`, com cálculos geométricos, seleção por coordenadas, transformações, validações e clonagem profunda (Prototype).

Etapa 3 concluída: canvas conectado aos modelos, barra lateral com ferramentas e estilos, lista de camadas e painel de status. As ferramentas de criação e seleção já usam State (`Ferramenta`, `FerramentaDesenhar`, `FerramentaInserirForma` e `FerramentaSelecionar`), gerenciadas pelo `EditorContext`.

Etapa 4 concluída: ferramentas State de escala, rotação, cisalhamento e edição de vértices; controles de transformação por valor; seleção por caixa e agrupamento. `GrupoFormas` aplica Composite aos membros identificados pelo mesmo `groupId`.

Etapa 5 concluída: desfazer/refazer com Memento e comandos de edição, persistência em JSON versionado, menus e atalhos, indicador de alterações não salvas e confirmação antes de fechar, abrir outro arquivo ou criar um desenho novo.

Revisão final: requisitos conferidos, validações reforçadas, documentação de entrega e UML atualizado em PDF preparados. A publicação remota e a submissão do link no portal acadêmico são os passos externos restantes.

## Material de entrega

- [UML implementado em PDF](output/pdf/diagrama_classes_implementado.pdf) e [fontes PlantUML](docs/uml/).
- [Matriz de requisitos e evidências](docs/REQUISITOS.md).
- [Roteiro de demonstração e instruções de entrega](docs/ENTREGA.md).
- [Desenho de exemplo](exemplos/demonstracao.json): abra pelo menu Arquivo > Abrir.
- `scripts/preparar_entrega.py`: gera um ZIP de apoio e checksums usando somente arquivos do trabalho.

## Como desenhar

- **Círculo, Quadrado e Hexágono:** selecione a ferramenta e arraste no canvas do centro até a distância desejada. Uma prévia acompanha o arrasto; a forma só é adicionada ao soltar o botão esquerdo.
- **Polígono:** clique com o botão esquerdo para adicionar vértices. Conclua com Enter, duplo clique, botão direito ou o botão da barra lateral. Desenhos inválidos exibem um aviso e permanecem disponíveis para correção.
- **Cancelar:** Esc, com o foco no canvas, ou o botão Cancelar operação. Trocar de ferramenta também descarta o desenho ainda não concluído. Arrastos de movimento, transformação e edição de vértice em andamento são revertidos ao cancelar.
- **Selecionar / mover:** clique em uma forma e arraste para movê-la. Shift + clique adiciona ou remove formas da seleção. Clicar no fundo sem Shift limpa a seleção. Em sobreposições, a forma da frente recebe o clique.
- **Seleção por caixa:** arraste a partir de uma área vazia para selecionar as formas completamente contidas no retângulo. Shift no início do arrasto adiciona à seleção. Esc cancela a caixa e restaura a seleção anterior. Uma caixa contendo um membro de um grupo seleciona o grupo inteiro.
- **Estilo:** preenchimento, cor e espessura da borda se aplicam às formas selecionadas e ficam definidos para as próximas formas. Selecionar uma única forma carrega seu estilo nos controles. Clique no fundo para definir o estilo de novas formas sem alterar as existentes.
- **Grade:** habilite Encaixar na grade para mostrar a grade e arredondar coordenadas em passos de 20 px. Na movimentação, os deslocamentos seguem esses passos.
- **Camadas:** a lista mostra a forma da frente no topo. Clique em uma linha para selecionar, usando Shift para seleção múltipla. Os botões Trazer para frente e Enviar para trás mudam a ordem de pintura, preservando a ordem relativa da seleção.
- **Excluir:** use o botão Excluir selecionadas ou Delete com o foco no canvas.
- **Agrupar:** selecione duas ou mais formas e use Agrupar selecionadas, na seção de camadas. Selecionar um membro passa a selecionar o grupo inteiro. Desagrupar selecionadas permite voltar a selecionar seus membros individualmente. Reagrupar cria um único grupo, sem grupos aninhados.
- **Transformações com o mouse:** selecione as formas e escolha Escalar, Rotacionar ou Cisalhar. Escala usa a razão entre a distância inicial e final do cursor ao centro; rotação usa a variação angular. Comece os gestos de escala/rotação a pelo menos 5 px do centro indicado. No cisalhamento, 150 px de deslocamento correspondem a fator 1 no eixo correspondente. Esc ou Cancelar operação reverte o arrasto em andamento; ao soltar, a transformação é confirmada. Trocar de ferramenta durante o gesto também o cancela.
- **Transformações por valor:** abra a seção Transformações por valor. Informe o fator positivo de escala, o ângulo em graus ou os fatores shx/shy e pressione o botão correspondente. São aceitos ponto e vírgula como separadores decimais. Formas sem grupo usam seus próprios centroides; os membros de cada grupo usam o centro comum ponderado pelas áreas. Operações inválidas não alteram parcialmente a seleção.
- **Editar vértice:** escolha a ferramenta, clique em uma forma para destacar seus vértices e arraste uma das alças. O encaixe na grade se aplica à nova posição. Círculos/elipses não expõem vértices editáveis. Esc reverte o gesto; posições com área zero são rejeitadas, mantendo a última posição válida. Em grupos, a edição altera somente o vértice arrastado, preservando o vínculo entre os membros.
- **Status:** mostra as coordenadas do mouse, total de formas e soma das áreas e perímetros da seleção; durante o desenho, mostra as medidas da prévia. Em seleções sobrepostas, as áreas são somadas individualmente, sem calcular a união.

A barra lateral tem rolagem para acomodar os controles em telas menores.

## Arquivos e histórico

| Operação | Atalho no Windows/Linux |
| --- | --- |
| Novo desenho | Ctrl+N |
| Abrir JSON | Ctrl+O |
| Salvar | Ctrl+S |
| Salvar como | Ctrl+Shift+S |
| Desfazer | Ctrl+Z |
| Refazer | Ctrl+Y ou Ctrl+Shift+Z |

No macOS, os atalhos usam Command no lugar de Ctrl. Também existem menus Arquivo/Editar e botões no topo da janela.

- **Salvar:** na primeira vez, escolha o destino; nos salvamentos seguintes, o arquivo atual é atualizado. Salvar como permite escolher outro destino. Conclua ou cancele os desenhos e arrastos em andamento antes de salvar.
- **Formato:** JSON UTF-8, versão 1, com dimensões do canvas, estado da grade e lista de formas na ordem de pintura. Cada forma contém tipo, pontos, cores, espessura e grupo. Círculos preservam os três controles de sua curva, inclusive após cisalhamento; quadrados e hexágonos preservam seus vértices editados.
- **Validação:** arquivos corrompidos, versões desconhecidas, formas degeneradas e dados incompletos exibem erro sem substituir o desenho atual. O salvamento grava primeiro um arquivo temporário no mesmo diretório e depois substitui o destino, usando movimentação atômica quando suportada.
- **Alterações não salvas:** um asterisco aparece no título. Antes de fechar, criar outro desenho ou abrir outro arquivo, escolha Salvar, Descartar ou Cancelar. Falhar ou cancelar o salvamento interrompe a ação solicitada e mantém o desenho aberto. Rascunhos ainda não concluídos também acionam o aviso.
- **Histórico:** criação, exclusão, movimento, transformações, edição de vértices, estilos, grupos e camadas podem ser desfeitos/refeitos. Um arrasto inteiro é uma única ação. Arrastos cancelados e ações sem alteração não criam entradas. Uma nova edição após desfazer limpa a pilha de refazer.
- **Estado salvo:** desfazer até recuperar exatamente as formas salvas remove o asterisco. Selecionar formas não modifica o documento. A grade é uma preferência do documento, salva no JSON, mas não integra o histórico de formas. Redimensionar a janela atualiza as dimensões que serão salvas; sozinho, não aciona o aviso de alterações.
- **Ao abrir ou criar:** o histórico e a seleção são reiniciados. Histórico, seleção, ferramenta ativa e estilo padrão das próximas formas não são persistidos; as formas existentes preservam seus estilos.

## Verificação

`./mvnw.cmd clean verify` executa 92 testes e gera o pacote. Além dos modelos e ferramentas, os testes verificam cópias profundas do histórico, uma ação por arrasto, desfazer/refazer, identificação do estado salvo, recuperação completa do JSON, rejeição de arquivos inválidos e falhas de escrita. A interface também foi verificada em execução com criação e edição de formas, salvamento, atalhos, avisos de fechamento/novo documento, cancelamento, descarte e redimensionamento.

## Padrões de projeto

- **State:** `Ferramenta` e seus estados concretos recebem eventos de mouse por meio de `EditorContext`.
- **Prototype:** `clonar()` cria cópias profundas das formas e de seus pontos.
- **Memento:** `HistoricoController` guarda estados independentes em pilhas de desfazer/refazer. Gestos contínuos capturam o estado inicial e registram somente a confirmação.
- **Command:** `Comando` encapsula operações de edição, executadas por `EditorContext.executarComando()` com registro do estado anterior.
- **Composite:** `GrupoFormas` aplica o contrato de forma geométrica ao conjunto de membros com um `groupId` comum.

## Convenções dos modelos

- Os modelos não dependem de JavaFX e são testados com JUnit 5. Execute `./mvnw.cmd test` no Windows ou `sh ./mvnw test` no Linux/macOS.
- `Poligono()` permite desenho incremental. Chame `validar()` antes de confirmar o desenho: exige pelo menos três pontos e área positiva. O construtor que recebe uma lista já faz essa validação. Os cálculos e a seleção consideram polígonos simples, sem cruzamento entre arestas.
- Enquanto um polígono tem menos de três pontos, área e perímetro são zero. Seu centro provisório é a média dos pontos; um polígono vazio não tem centroide.
- `getPontos()` devolve uma lista imutável de cópias. Use `Poligono.editarVertice(indice, ponto)` para editar; uma edição com área zero é rejeitada e revertida.
- Área de polígonos: fórmula de Gauss; perímetro: soma das arestas; centroide: ponderado pela área. A seleção inclui a borda.
- O raio de quadrados e hexágonos é a distância do centro a um vértice. As formas nascem regulares, mas podem ser deformadas por cisalhamento ou edição de vértices.
- Escala: fator positivo. Escala, rotação e cisalhamento usam o centroide da própria forma. A rotação recebe radianos; com o eixo Y do canvas para baixo, ângulos positivos aparecem no sentido horário.
- O cisalhamento simultâneo usa `x' = x + shx*y` e `y' = shy*x + y` nas coordenadas relativas ao centroide. Matrizes singulares (`1 - shx*shy = 0`) são rejeitadas.
- `Circulo` armazena três pontos de controle: centro e extremidades de dois vetores de raio. Eles não são vértices de um triângulo. Essa representação preserva a elipse produzida pelo cisalhamento; renderização e persistência preservam os três pontos. Área e seleção são analíticas. O perímetro usa Ramanujan II para elipses e resulta em `2*pi*r` para círculos.
- Cores usam `#RRGGBB` ou `#RRGGBBAA`; borda pode ter espessura zero. `groupId = null` indica ausência de grupo.
- `clonar()` preserva o tipo concreto, geometria, estilos e grupo, sem compartilhar pontos com a forma original.

