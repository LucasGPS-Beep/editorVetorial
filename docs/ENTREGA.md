# Entrega e demonstração

## Material preparado

- Projeto Maven e Wrapper na raiz.
- Instruções de execução e uso no [README](../README.md).
- [Matriz de requisitos](REQUISITOS.md).
- [UML da implementação em PDF](../output/pdf/diagrama_classes_implementado.pdf), com quatro páginas.
- Fontes PlantUML em `docs/uml/`; definições comuns e gerador em `scripts/gerar_uml.py`.
- Desenho de exemplo em [exemplos/demonstracao.json](../exemplos/demonstracao.json).
- Script `scripts/preparar_entrega.py` para gerar o ZIP e sua lista de arquivos/checksums.

O ZIP é uma cópia de apoio. A entrega pedida pelo enunciado é o **link do repositório Git**, submetido no portal acadêmico.

## Conferência antes de enviar

1. Em um terminal na raiz, execute `./mvnw.cmd clean verify` no Windows ou `sh ./mvnw clean verify` no Linux/macOS.
2. Abra com `./mvnw.cmd javafx:run` (Windows) ou `sh ./mvnw javafx:run` (Linux/macOS).
3. Faça o roteiro abaixo e confira as informações de identificação/prazo exigidas no portal da disciplina.
4. Publique o repositório em um destino escolhido e confirme que o professor terá acesso.
5. Submeta o link no portal. Publicar no Git e submeter no portal são ações distintas.

## Roteiro de demonstração

1. **Abrir e desenhar:** abra `exemplos/demonstracao.json`. Ele contém as quatro formas, um grupo com duas formas e um círculo cisalhado. Depois crie outro desenho para demonstrar a construção por mouse.
2. **Polígono:** clique em três ou mais posições e pressione Enter. Tente concluir com apenas dois pontos para mostrar o alerta e a possibilidade de corrigir o desenho.
3. **Formas regulares:** crie círculo, quadrado e hexágono por arrasto. Mostre as medidas no rodapé.
4. **Seleção:** selecione por clique, Shift e caixa. Mova uma forma e mostre o destaque visual. Ative a grade e repita o movimento.
5. **Estilos/camadas:** altere preenchimento, borda e espessura; sobreponha duas formas e use frente/trás.
6. **Transformações:** selecione uma forma, abra Transformações por valor e aplique escala, rotação e cisalhamento. Mostre o gesto equivalente pelo mouse e Esc durante um arrasto.
7. **Grupo:** selecione duas formas, agrupe e mova/rotacione o conjunto. Desagrupe e mostre a seleção individual.
8. **Vértice:** selecione Editar vértice e arraste uma alça de um polígono. Mostre a atualização da área/perímetro.
9. **Histórico:** mova uma forma em um único arrasto longo e use Ctrl+Z uma vez. Use Ctrl+Y para refazer. Mostre que uma nova alteração após desfazer remove a possibilidade de refazer a ação anterior.
10. **Persistência:** salve um JSON, crie outro desenho e reabra o arquivo salvo. Confira geometria, cores, grupos e camadas.
11. **Robustez:** tente abrir um arquivo JSON inválido criado para o teste. Confirme o alerta e a permanência do desenho atual.
12. **Fechamento:** altere o desenho, tente fechar e escolha Cancelar. Depois salve e feche normalmente.

## Gerar os arquivos auxiliares

O aplicativo requer somente o JDK e o Maven Wrapper. Python é opcional, usado apenas para documentação e empacotamento.

```powershell
python scripts/gerar_uml.py
python scripts/preparar_entrega.py
```

O primeiro comando requer `reportlab` instalado no Python escolhido. Ele recria o PDF e os arquivos `.puml` a partir das definições em `gerar_uml.py`. Os arquivos `.puml` também podem ser abertos em uma ferramenta PlantUML para edição/renderização independente.

O segundo usa apenas a biblioteca padrão do Python e gera `entrega/editor-vetorial.zip`, `entrega/MANIFESTO.txt` e `entrega/SHA256SUMS.txt`. A seleção de arquivos é explícita: código, configuração, documentação, exemplo e PDFs do trabalho. Temporários, dependências locais, `.git`, `target` e arquivos pessoais não entram no pacote.

## Publicar o Git local

Depois de criar um repositório vazio no serviço escolhido e obter sua URL:

```powershell
git remote add origin URL_DO_REPOSITORIO
git push -u origin main
```

Substitua `URL_DO_REPOSITORIO` pelo endereço real. Não execute o comando com esse texto literal. A autenticação é feita pela conta do proprietário do repositório.
