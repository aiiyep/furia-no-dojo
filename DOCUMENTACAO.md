# 🥋 Fúria no Dojo — Jogo de Luta 2D Arcade em Java (MVC + Pixel Art)

Jogo de luta 2D estilo arcade retrô, desenvolvido em **Java puro** com **Swing/AWT**, sem bibliotecas externas e sem arquivos de imagem. Todo o visual é **pixel art gerada por código**: o jogo desenha numa tela de **300×180 pixels** e a amplia **3×** (para 900×540) sem suavização, como nos fliperamas dos anos 90.

O projeto segue a arquitetura **MVC (Model–View–Controller)** e pode ser jogado **contra a CPU** ou por **2 pessoas no mesmo computador**.

---

## 1. Descrição do jogo

**TAKESHI** (kimono branco, faixa e luvas vermelhas) enfrenta **SOMBRA** (kimono azul-marinho, faixa e luvas douradas) num dojo ao pôr do sol, emoldurado por um portal *torii*, com sol listrado, mar refletindo a luz e lanternas tremulando.

### Modos de jogo

| Modo | Descrição |
|---|---|
| **1 JOGADOR (VS CPU)** | Você controla TAKESHI; SOMBRA é controlado pela IA, que fica mais rápida a cada round. |
| **2 JOGADORES (LOCAL)** | Duas pessoas no mesmo teclado: 1P controla TAKESHI, 2P controla SOMBRA. |

No menu, escolha o modo com `W`/`S` (ou setas) e confirme com `ENTER`.

### Regras

| Regra | Valor |
|---|---|
| Formato | **Melhor de 3** (vence quem ganhar **2 rounds** primeiro) |
| Tempo por round | **60 segundos** |
| Vida | 100 HP por lutador |
| Vitória no round | Zerar a vida do oponente (**K.O.**) ou ter mais vida quando o tempo acaba (**TIME OVER**) |
| Empate | Ninguém pontua e o round é repetido |

### Mecânicas

- **Defesa:** segurar a direção oposta ao oponente faz recuar em guarda; golpes defendidos causam só **25% do dano**.
- **Agachar** reduz a *hurtbox*: o projétil especial **passa por cima**.
- **Pulo** com gravidade; dá para pular por cima de projéteis e do oponente (os lutadores se viram sozinhos).
- **Golpes aéreos:** soco e chute funcionam no ar.
- **Projéteis opostos se anulam.**
- *Hitstop*, tremor de tela, faíscas e pontos flutuantes no impacto.

### Pontuação (cada lado tem a sua)

| Ação | Pontos |
|---|---|
| Soco / Chute / Especial que acerta | 100 / 200 / 300 |
| Golpe defendido pelo oponente | 20 |
| Anular um projétil | 50 |
| **Combo** (acertos seguidos em até 0,75 s) | +50 × (nº do acerto − 1) |
| Vencer o round | 1.000 (+2.000 se **PERFECT**) |
| Bônus de vida / tempo restantes no round vencido | 10 por HP / 20 por segundo |
| Vencer a partida | 5.000 |

A tela final mostra o detalhamento da pontuação (no modo 2P, lado a lado), o total e o recorde da sessão.

### Controles

**Modo 1 jogador** (qualquer combinação funciona):

| Ação | Teclas |
|---|---|
| Mover (para trás = defender) | `A` `D` ou `←` `→` |
| Pular / Agachar | `W` `S` ou `↑` `↓` |
| Soco rápido | `J` ou `Z` |
| Chute forte | `K` ou `X` |
| Especial (projétil) | `L` ou `C` |

**Modo 2 jogadores:**

| Ação | Jogador 1 (TAKESHI) | Jogador 2 (SOMBRA) |
|---|---|---|
| Mover / Pular / Agachar | `W` `A` `S` `D` | Setas |
| Soco rápido | `F` | `J` ou `Num 1` |
| Chute forte | `G` | `K` ou `Num 2` |
| Especial | `H` | `L` ou `Num 3` |

**Gerais:** `ENTER` inicia/reinicia · `P` pausa · `ESC` volta ao menu (na pausa ou na tela final) · `F2` liga/desliga o efeito de *scanlines* (CRT).

> Alguns teclados não registram muitas teclas ao mesmo tempo (*ghosting*). Se um comando "falhar" no modo 2P, teste as alternativas do teclado numérico para o jogador 2.

---

## 2. Como compilar e executar

**Requisito:** JDK 8 ou superior (testado com JDK 21).

### Atalho no Windows

Dê dois cliques em **`executar.bat`**: ele compila tudo e abre o jogo.

### Pelo terminal

Na pasta do projeto:

```bash
# Linux / macOS / Git Bash
javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin Main
```

```powershell
# Windows PowerShell
javac -encoding UTF-8 -d bin (Get-ChildItem src -Recurse -Filter *.java).FullName
java -cp bin Main
```

O `-encoding UTF-8` garante que os acentos dos textos sejam lidos corretamente.

### Pela IDE

- **IntelliJ IDEA:** *File → Open* na pasta → marque `src` como *Sources Root* → rode `Main.java`.
- **Eclipse:** crie um *Java Project* usando a pasta `src` como pasta de código-fonte → *Run As → Java Application* em `Main.java`.
- **VS Code:** com o *Extension Pack for Java*, abra a pasta e clique em *Run* acima do `main`.
- **NetBeans:** *New Project → Java with Existing Sources* → adicione `src` → *Run*.

Se os acentos aparecerem errados, configure a codificação do projeto como **UTF-8**.

---

## 3. Arquitetura MVC

```
src/
├── Main.java                    → monta e conecta Model, View e Controller (na EDT)
├── model/                       → MODEL: estado e regras do jogo (sem Swing)
│   ├── Jogo.java                → partida: estados, rounds, tempo, colisões, pontuação
│   ├── Entidade.java            → classe abstrata base
│   ├── Lutador.java             → física, golpes, hitbox/hurtbox, dano
│   ├── LutadorIA.java           → lutador autônomo (CPU)
│   ├── Projetil.java            → golpe especial à distância
│   ├── Pontuacao.java           → pontos, combos e bônus de um lado
│   ├── Faisca.java              → partícula de impacto
│   ├── TextoFlutuante.java      → "+200" que sobe na tela
│   ├── VisitanteEntidade.java   → interface Visitor (ponte para a View)
│   ├── Arena.java               → dimensões do mundo
│   ├── EstadoJogo.java          → MENU, JOGANDO, FIM_ROUND, VITORIA, GAME_OVER
│   ├── EstadoLutador.java       → posturas do lutador
│   ├── FaseGolpe.java           → preparação / ativo / recuperação
│   ├── ModoJogo.java            → 1 jogador / 2 jogadores
│   └── Personagem.java          → TAKESHI / SOMBRA
├── view/                        → VIEW: só lê o Model e desenha
│   ├── JanelaJogo.java          → JFrame 900x540 fixo e centralizado
│   ├── PainelJogo.java          → JPanel: tela 300x180 ampliada 3x + scanlines
│   ├── RenderizadorCenario.java → céu, sol, montanhas, mar, piso, torii, lanternas
│   ├── RenderizadorEntidades.java → Visitor que despacha cada entidade
│   ├── RenderizadorLutador.java → sprite articulado em pixel art
│   ├── RenderizadorProjetil.java→ esfera de energia
│   ├── RenderizadorEfeitos.java → faíscas e pontos flutuantes
│   ├── RenderizadorHud.java     → vida, relógio, rounds, especial, score, combos
│   ├── RenderizadorTelas.java   → menu, intro, K.O., pausa, tela final
│   ├── FontePixel.java          → fonte bitmap 5x7 com acentos
│   └── Paleta.java              → cores de cada personagem e da interface
└── controller/                  → CONTROLLER: entrada e game loop
    ├── ControladorJogo.java     → javax.swing.Timer (16 ms), menu, pausa, comandos
    ├── ControladorTeclado.java  → KeyListener com estado booleano das teclas
    └── MapaTeclas.java          → teclas do 1P solo, 1P e 2P
```

### Fluxo de um frame

```
 Timer (16 ms) ──► ControladorJogo.actionPerformed()
                     │
                     ├─1─► ControladorTeclado ──comandos──► Lutador (Model)
                     │                                   (andar, pular, socar...)
                     ├─2─► jogo.atualizar()   física, IA, colisões, rounds, pontos
                     │
                     └─3─► painelJogo.repaint() ──► View LÊ o Model e desenha
```

### Responsabilidades e regras de dependência

| Camada | Pode depender de | Não conhece |
|---|---|---|
| **Model** | apenas Java padrão (`java.util`, `java.awt.Rectangle` para geometria) | View, Controller, Swing, teclado, cores |
| **View** | Model (somente leitura) | Controller, teclado |
| **Controller** | Model e View | detalhes de desenho |

- O **Model** não sabe que existe tela nem teclado. `Lutador` recebe intenções (`setMovendoEsquerda`, `pular`, `socar`...) e expõe o estado (`getEstado`, `getFaseGolpe`, `getVida`...).
- A **View** não altera nada no jogo: lê os getters do `Jogo` e desenha. As cores ficam na `Paleta`; o Model só sabe qual `Personagem` é cada lutador.
- O **Controller** traduz o teclado em comandos e é dono do *game loop*. No modo 1P só o lutador 1 recebe comandos do teclado (a `LutadorIA` decide sozinha); no modo 2P cada lutador recebe seu próprio `MapaTeclas`.

### Pixel art

- `PainelJogo` desenha tudo numa `BufferedImage` de **300×180**, com antialiasing desligado, e a amplia 3× com interpolação *nearest neighbor*. Cada "pixel de arte" vira um bloco de 3×3 na tela.
- O **cenário** é gerado pixel a pixel (`setRGB`) uma única vez: céu em faixas de cor com *dithering* xadrez nas transições, sol com listras, montanhas em degraus de 2 px, piso de madeira em perspectiva. Estrelas, reflexo no mar e lanternas são animados a cada frame.
- Os **lutadores** são *sprites procedurais*: um esqueleto articulado (ângulos de ombro, cotovelo, quadril e joelho) rasterizado em baixa resolução com **contorno escuro de 1 px**, tons de luz e sombra, e animações em poucos quadros (respiração em 2 quadros, caminhada em 4), como os sprites clássicos.
- A **fonte** (`FontePixel`) é um bitmap 5×7 desenhado em código, com degradê em faixas e acentos do português.

---

## 4. Pilares da Programação Orientada a Objetos

### Abstração
- `Entidade` é **abstrata** e define o contrato de tudo que existe na arena: `atualizar()`, `getHurtbox()` e `aceitar(VisitanteEntidade)`.
- `VisitanteEntidade` é uma **interface** que abstrai "algo que processa entidades"; a View a implementa para desenhar.
- Enums (`EstadoJogo`, `EstadoLutador`, `FaseGolpe`, `ModoJogo`, `Personagem`) dão nome aos conceitos do jogo.
- `Lutador` expõe comandos de alto nível (`socar()`, `chutar()`, `lancarEspecial()`) e esconde frames, física e hitbox.

### Encapsulamento
- Todos os atributos são `private`, com getters/setters.
- A vida **não tem setter**: só muda por `receberDano()`, que aplica as regras de defesa, knockback e nocaute.
- `Jogo` devolve listas **somente leitura** (`Collections.unmodifiableList`), e a View não consegue alterar o estado do jogo.
- `MapaTeclas` é imutável: os getters devolvem cópias dos vetores.
- Classes auxiliares internas (`Pose`, `Esqueleto` na View) são `private`.

### Herança
- `Lutador` e `Projetil` estendem `Entidade`; `LutadorIA` estende `Lutador` e herda toda a física e os golpes, acrescentando só a tomada de decisão.
- `JanelaJogo extends JFrame` e `PainelJogo extends JPanel` especializam componentes Swing.

### Polimorfismo
- **Sobrescrita:** `LutadorIA.atualizar()` "pensa" e depois chama `super.atualizar()`; `LutadorIA.reiniciar()` estende o método do pai.
- **Lista polimórfica no Model:** o `Jogo` atualiza tudo como `Entidade`:

  ```java
  for (Entidade entidade : getEntidades()) {
      entidade.atualizar();   // Lutador, LutadorIA ou Projetil: cada um do seu jeito
  }
  ```

- **Visitor (double dispatch) na View:** para desenhar sem `instanceof` e sem o Model depender da View:

  ```java
  // RenderizadorEntidades implements VisitanteEntidade
  for (Entidade entidade : entidades) {
      entidade.aceitar(this);   // Lutador chama visitar(Lutador), Projetil chama visitar(Projetil)
  }
  ```

- **Interfaces do Swing:** `ControladorJogo` implementa `ActionListener` (ticks do `Timer`) e `ControladorTeclado` implementa `KeyListener` e `FocusListener`.

---

## 5. Colisões

- **Hurtbox** (`Rectangle`): em pé 40×150, **agachado** 44×95, **no ar** 40×115 (unidades do mundo).
- **Hitbox** (`Rectangle`): criada só na fase `ATIVO` do golpe, à frente do atacante (soco: alcance 62; chute: alcance 88).
- **Acerto único:** ao conectar, `registrarAcerto()` liga a flag `golpeAcertou` e a hitbox some até o próximo golpe.
- **Projéteis:** colidem com a hurtbox do adversário; dois projéteis de donos diferentes se anulam.
- **Separação de corpos:** os lutadores não se sobrepõem e são empurrados corretamente quando um está encostado na parede.
