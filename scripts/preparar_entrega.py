"""Empacota somente os arquivos do trabalho, sem dependências ou arquivos pessoais."""
from pathlib import Path
from hashlib import sha256
from zipfile import ZipFile, ZIP_DEFLATED, ZipInfo

ROOT = Path(__file__).resolve().parents[1]


def arquivos():
    fixos = [".gitignore", ".gitattributes", "pom.xml", "mvnw", "mvnw.cmd", "README.md",
             ".mvn/wrapper/maven-wrapper.properties", "provisorio/trabalho_n2.pdf",
             "provisorio/diagrama_classes.pdf", "output/pdf/diagrama_classes_implementado.pdf"]
    resultado = [ROOT / nome for nome in fixos]
    for pasta, extensoes in [("src", {".java"}), ("docs", {".md", ".puml"}),
                             ("scripts", {".py"}), ("exemplos", {".json", ".md"})]:
        resultado.extend(p for p in (ROOT / pasta).rglob("*") if p.is_file() and p.suffix in extensoes)
    for arquivo in resultado:
        if not arquivo.resolve().is_relative_to(ROOT) or not arquivo.is_file():
            raise ValueError(f"Arquivo obrigatório ausente ou fora do projeto: {arquivo.name}")
    return sorted(set(resultado), key=lambda p: p.relative_to(ROOT).as_posix())


def main():
    destino = ROOT / "entrega"
    destino.mkdir(exist_ok=True)
    pacote = destino / "editor-vetorial.zip"
    manifesto = []
    with ZipFile(pacote, "w", compression=ZIP_DEFLATED, compresslevel=9) as zipado:
        for arquivo in arquivos():
            nome = arquivo.relative_to(ROOT).as_posix()
            dados = arquivo.read_bytes()
            info = ZipInfo(nome, date_time=(2026, 1, 1, 0, 0, 0))
            info.create_system = 3
            info.external_attr = (0o100755 if nome == "mvnw" else 0o100644) << 16
            info.compress_type = ZIP_DEFLATED
            zipado.writestr(info, dados)
            manifesto.append(f"{sha256(dados).hexdigest()}  {len(dados):>9}  {nome}")
    (destino / "MANIFESTO.txt").write_text("\n".join(manifesto) + "\n", encoding="utf-8")
    resumo = f"{sha256(pacote.read_bytes()).hexdigest()}  {pacote.name}\n"
    (destino / "SHA256SUMS.txt").write_text(resumo, encoding="utf-8")
    with ZipFile(pacote) as zipado:
        if zipado.testzip() is not None:
            raise RuntimeError("Falha de integridade do ZIP.")
    print(f"{len(manifesto)} arquivos em {pacote}")
    print(resumo.strip())


if __name__ == "__main__":
    main()
