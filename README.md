# 🥋 Fúria no Dojo

Jogo de luta 2D estilo arcade retrô, feito em **Java puro** (Swing/AWT) com visual em **pixel art** gerado por código.

Lute contra a **CPU** ou chame um amigo para jogar **2 jogadores no mesmo computador**. Vence a partida quem ganhar **2 de 3 rounds**.

## Como jogar

Requisito: **Java (JDK) 8 ou superior**.

- **Windows:** dê dois cliques em `executar.bat`.
- **Terminal:**
  ```bash
  javac -encoding UTF-8 -d bin $(find src -name "*.java")
  java -cp bin Main
  ```

No menu, escolha o modo com `W`/`S` e aperte `ENTER`.

## Controles

| Ação | 1 Jogador | Jogador 1 (modo 2P) | Jogador 2 (modo 2P) |
|---|---|---|---|
| Mover | `A` `D` ou setas | `A` `D` | `←` `→` |
| Pular / Agachar | `W` `S` ou setas | `W` `S` | `↑` `↓` |
| Soco | `J` | `F` | `J` |
| Chute | `K` | `G` | `K` |
| Especial | `L` | `H` | `L` |

**Dicas:** segure para trás para **defender**, e agache para desviar do **especial**.  
`P` pausa · `ESC` volta ao menu · `F2` liga/desliga o efeito de monitor antigo.

## Projeto

Organizado no padrão **MVC** (`src/model`, `src/view`, `src/controller`).  
Detalhes da arquitetura e da Orientação a Objetos estão em [DOCUMENTACAO.md](DOCUMENTACAO.md).
