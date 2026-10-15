# Conferência dos requisitos

Referências: [enunciado](../provisorio/trabalho_n2.pdf) e [diagrama fornecido](../provisorio/diagrama_classes.pdf).
O arquivo do enunciado tem N2 no nome, mas seu conteúdo identifica a avaliação como N3.

Esta matriz registra o que foi implementado e como foi verificado; não substitui a avaliação do professor.

| Requisito do enunciado | Implementação / evidência |
| --- | --- |
| Java 17 ou superior | `pom.xml`: `maven.compiler.release=17`; compilação verificada com JDK 21 |
| JavaFX 17+ | Dependência JavaFX 17.0.12; interface construída em Java, sem FXML |
| Maven | `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties` |
| MVC e pacotes exigidos | `org.example.Models`, `org.example.Controller`, `org.example.Views`; modelos sem JavaFX, controllers sem imports de Views |
| Encapsulamento | Campos privados/protegidos; listas de pontos retornadas como cópias imutáveis |
| Contrato `FormaGeometrica` | Métodos do contrato fornecido, implementados por `FormaBase` e formas concretas |
| Formas | `Ponto`, `Poligono`, `Circulo`, `Quadrado`, `Hexagono`; vértices regulares calculados automaticamente |
| Validações | Polígono concluído com pelo menos três pontos e área positiva; raio positivo; números e medidas finitos |
| Área e perímetro | Gauss e distâncias euclidianas para polígonos; cálculos analíticos do círculo e da área da elipse |
| Translação, escala e rotação | Modelos e ferramentas do mouse; escala e rotação ao redor do centroide, ângulo interno em radianos |
| Cisalhamento e edição de vértices do UML | `FerramentaTransformar`, `FerramentaEditarVertice`; controles numéricos e alças |
| State | `Ferramenta`, cinco estados concretos e `EditorContext`; comportamento do mouse delegado ao estado ativo |
| Prototype | `clonar()` preserva tipo, estilo e grupo, copiando profundamente os pontos |
| Memento / desfazer e refazer | `HistoricoController` com pilhas de estados independentes; uma entrada por gesto confirmado |
| Command | `Comando` e `EditorContext.executarComando()` encapsulam operações com registro do estado anterior |
| Composite / agrupamento | `GrupoFormas` implementa `FormaGeometrica`; seleção expande `groupId` e transforma os membros como um conjunto |
| Canvas e criação dinâmica | Polígonos por cliques; círculo, quadrado e hexágono por arrasto com prévia |
| Snap to grid | Grade de 20 px e arredondamento das coordenadas de criação/edição e deslocamentos |
| Seleção visual e gerenciamento de camadas | Destaque no canvas, seleção por clique/Shift/caixa, lista de camadas, frente/trás, exclusão |
| Barra lateral e status | Ferramentas, cores, borda e camadas; coordenadas, contagem, área e perímetro atualizados |
| Persistência estruturada | `ArquivoController`: JSON UTF-8 versão 1, canvas, formas, estilo, coordenadas, grupos e ordem |
| Erros de leitura/escrita | Validação antes de substituir o documento; alertas gráficos; gravação temporária antes da substituição |
| Alterações não salvas | Asterisco no título; Salvar/Descartar/Cancelar ao fechar, abrir outro arquivo ou criar novo |
| README e UML em PDF | [README](../README.md), [UML implementado](../output/pdf/diagrama_classes_implementado.pdf) e fontes em `docs/uml/` |
| Código em repositório Git | Preparação local; publicação remota e submissão do link no portal são os passos externos da entrega |

## Evidências de verificação

- `ArquivoTest`: recuperação de dados, exemplo distribuído, JSON inválido, tipos desconhecidos, medidas inválidas, sobrescrita e falha de gravação.
- `HistoricoTest`: cópias profundas, arrasto como uma ação, cancelamento, retorno ao estado salvo, estilos, grupos, camadas e exclusão durante arrasto.
- `EdicaoTest` e `FerramentasTest`: eventos de criação/seleção, grade, caixa, agrupamento, transformações, edição de vértices e Esc.
- `FormasTest`, `PoligonoTest` e `PontoTest`: fórmulas, centroide, interior/borda, clones e validações.
- Verificação gráfica em execução: criação/edição, valores numéricos, agrupamento, salvamento, atalhos Ctrl+Z/Ctrl+Y, avisos Cancelar/Descartar e redimensionamento.

## Decisões de implementação

- Foi escolhido JSON: a seção de persistência exige JSON ou XML, apesar de a tabela técnica também mencionar serialização nativa.
- O UML implementado mantém a estrutura central fornecida e acrescenta classes auxiliares para grupos, arquivos, documentos, limites e menus.
- Os cálculos de polígonos pressupõem formas simples. Não há validação específica de cruzamento entre arestas; para demonstrar área e centroide, use polígonos sem auto-interseção.
- Um círculo cisalhado é tratado como elipse: área e seleção são analíticas, perímetro usa Ramanujan II e desenho usa curvas cúbicas.
- Formas agrupadas usam centroide ponderado pelas áreas individuais; sobreposições são contadas separadamente.
- A grade é persistida como preferência, mas não entra no histórico de formas. Seleção e redimensionamento da janela, sozinhos, não acionam o aviso de desenho alterado.
- O JAR gerado é o artefato Maven comum. A execução documentada usa `javafx:run`; não foi criado instalador ou JAR independente com JavaFX embutido.
- A execução foi verificada no Windows com JDK 21 e compilação para Java 17. Linux/macOS não foram executados nesta revisão.
