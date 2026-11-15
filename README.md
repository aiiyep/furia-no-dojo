# 🥋 Fúria no Dojo

Jogo de luta 2D estilo **arcade retrô**, inspirado nos clássicos de fliperama dos anos 90, desenvolvido em **Java puro** com **Swing/AWT**. Não usa nenhuma biblioteca externa nem arquivo de imagem: cenário, personagens, golpes, HUD e até a fonte dos textos são desenhados **por código**, em **pixel art**.

Dá para lutar contra a **CPU** ou chamar um amigo e jogar **2 jogadores no mesmo computador**.

---

## 🎮 Sobre o jogo

**TAKESHI**, de kimono branco e faixa vermelha, enfrenta **SOMBRA**, de kimono azul-marinho e faixa dourada, num dojo ao pôr do sol, emoldurado por um portal *torii*, com o sol listrado se pondo no mar e lanternas de papel tremulando.

- **Melhor de 3 rounds:** vence a partida quem ganhar 2 rounds primeiro.
- **60 segundos por round:** ganha o round quem nocautear o adversário (**K.O.**) ou tiver mais vida quando o tempo acabar (**TIME OVER**).
- **Três golpes:**
  - **soco**: rápido e de curto alcance;
  - **chute**: mais lento, porém mais forte e de maior alcance;
  - **especial**: uma esfera de energia disparada à distância, com tempo de recarga.
- **Defesa:** segurar para trás bloqueia e reduz o dano a 25%.
- **Esquiva:** agachar faz o especial passar por cima, e dá para pular por cima de projéteis e do adversário.
- **Choque de especiais:** dois especiais que se encontram no meio do caminho se anulam.
- **Pontuação:** cada lado ganha pontos por acertos, combos, round perfeito (sem perder vida) e bônus de vida e de tempo restantes ao vencer.

### Modos

| Modo | Como funciona |
|---|---|
| **1 Jogador (vs CPU)** | Você controla TAKESHI. A CPU decide sozinha (aproxima, recua, defende, pula, ataca e desvia de projéteis) e fica mais rápida a cada round. |
| **2 Jogadores (local)** | Duas pessoas no mesmo teclado: uma do lado esquerdo e outra nas setas. |

---

## 🕹️ Controles

| Ação | 1 Jogador | Jogador 1 (modo 2P) | Jogador 2 (modo 2P) |
|---|---|---|---|
| Mover | `A` `D` ou setas | `A` `D` | `←` `→` |
| Pular / Agachar | `W` `S` ou setas | `W` `S` | `↑` `↓` |
| Soco | `J` (ou `Z`) | `F` | `J` (ou `Num 1`) |
| Chute | `K` (ou `X`) | `G` | `K` (ou `Num 2`) |
| Especial | `L` (ou `C`) | `H` | `L` (ou `Num 3`) |

`ENTER` começa · `P` pausa · `ESC` volta ao menu · `F2` liga/desliga o efeito de monitor antigo (*scanlines*)

---

## ▶️ Como rodar

Requisito: **Java (JDK) 8 ou superior**.

**Windows:** dê dois cliques em `executar.bat`. Ele compila e abre o jogo.

**Terminal (qualquer sistema):**
```bash
javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin Main
```

**IDE (IntelliJ, Eclipse, VS Code, NetBeans):** abra a pasta, marque `src` como pasta de código-fonte e execute `Main.java`.

---

## 🛠️ Como foi feito

### Arquitetura MVC

O código é dividido em três camadas, cada uma com uma responsabilidade:

```
src/
├── Main.java        → cria e conecta as três camadas
├── model/           → MODEL: regras e estado do jogo
├── view/            → VIEW: tudo o que é desenhado na tela
└── controller/      → CONTROLLER: teclado e game loop
```

- **Model:** guarda o estado do jogo e aplica as regras: lutadores, física, golpes, colisões, rounds, tempo, pontuação e a IA. Não sabe que existe tela nem teclado, por isso não usa Swing.
- **View:** apenas lê o Model e desenha. Não altera nada no jogo.
- **Controller:** lê o teclado, transforma as teclas em comandos para os lutadores ("andar", "pular", "socar"...) e roda o *game loop*.

A cada frame (~60 por segundo), um `javax.swing.Timer` dispara o Controller, que faz três coisas em ordem:

```
1. lê o teclado  →  envia comandos ao Model
2. jogo.atualizar()   (física, IA, colisões, rounds, pontos)
3. painel.repaint()   (a View redesenha a tela lendo o Model)
```

Com essa separação, foi simples adicionar o modo de 2 jogadores: bastou o Controller mandar os comandos de um segundo conjunto de teclas para o segundo lutador, sem mexer na lógica da luta.

### Orientação a Objetos

- **Abstração:** a classe abstrata `Entidade` define o que todo objeto da arena precisa ter (atualizar, ter área de colisão); `Lutador` e `Projetil` a estendem.
- **Herança:** `LutadorIA` estende `Lutador` e herda toda a física e os golpes, acrescentando só o "cérebro" da CPU.
- **Polimorfismo:**
  - o jogo atualiza todas as entidades numa única lista, e cada uma reage do seu jeito;
  - para desenhar, a View usa o padrão **Visitor**, o que evita `instanceof` e mantém o Model sem depender da View.
- **Encapsulamento:**
  - os atributos são privados;
  - a vida de um lutador só muda pelo método `receberDano()`, que aplica as regras de defesa e nocaute;
  - a View recebe listas somente leitura.

### Pixel art feita em código

Não existe nenhum arquivo `.png` no projeto. O efeito de pixel art vem de uma técnica simples:

1. **Tela pequena:** todo o jogo é desenhado numa imagem de apenas **300×180 pixels**, com a suavização (*antialiasing*) desligada.
2. **Ampliação:** essa imagem é ampliada **3 vezes** (para 900×540) com interpolação *vizinho mais próximo*, e cada pixel vira um "bloquinho" 3×3 nítido.
3. **Scanlines:** por cima, linhas horizontais escuras imitam um monitor CRT de fliperama.

Em cima disso:

- **Cenário:** é gerado pixel a pixel uma única vez, quando o jogo abre.
  - O céu é feito em faixas de cor com *dithering* (pixels alternados em xadrez nas transições), um truque clássico dos videogames de 16 bits.
  - O sol tem listras, as montanhas sobem em degraus e o piso de madeira tem linhas em perspectiva.
  - Estrelas, o reflexo no mar e as lanternas são animados a cada frame.
- **Lutadores:** não são imagens prontas. Cada um é um **esqueleto articulado** (ombro, cotovelo, quadril, joelho) cujos ângulos mudam conforme a ação: guarda, caminhada, agachamento, pulo, defesa, soco, chute, especial, atordoado, nocaute e comemoração.
  - O corpo é desenhado em baixa resolução, com contorno escuro de 1 pixel e tons de luz e sombra.
  - As animações usam poucos quadros, como os sprites antigos.
  - Os dois lutadores compartilham o mesmo desenho, só com paletas de cores diferentes.
- **Fonte:** a fonte dos textos é um **bitmap 5×7** definido no próprio código, letra por letra, com suporte a acentos do português.

### Sistema de luta

- **Hurtbox:** cada lutador tem um retângulo que representa o corpo vulnerável. Ele fica menor ao agachar e muda de formato no ar.
- **Hitbox:** os golpes criam um retângulo de ataque à frente do lutador, que existe só nos poucos frames em que o golpe está "ativo". Cada golpe passa por três fases: **preparação → ativo → recuperação**.
- **Acerto único:** uma flag garante que cada golpe acerte **uma única vez**, mesmo que os retângulos continuem se tocando por vários frames.
- **Sensação de impacto:** ao acertar, o jogo congela por alguns frames (*hitstop*), a tela treme, saem faíscas e aparecem os pontos ganhos.

### Inteligência artificial

A CPU "pensa" em intervalos que ficam menores a cada round. Assim ela reage mais rápido conforme a partida avança. Ela decide o que fazer de acordo com a distância até o jogador:

- **De longe:** se aproxima ou lança o especial.
- **Na média distância:** avança, pula para dentro ou recua.
- **De perto:** soca, chuta, defende ou agacha.
- **Contra projéteis:** quando vê um vindo, pode pular, agachar ou defender.

---

## 📁 Estrutura de arquivos

| Pasta | Principais classes |
|---|---|
| `model/` | `Jogo` (a partida), `Lutador`, `LutadorIA`, `Projetil`, `Pontuacao`, `Entidade` e enums de estado |
| `view/` | `JanelaJogo`, `PainelJogo`, renderizadores do cenário, lutadores, HUD e telas, `FontePixel`, `Paleta` |
| `controller/` | `ControladorJogo` (game loop), `ControladorTeclado`, `MapaTeclas` |

Para a explicação técnica completa de cada classe, da arquitetura e dos pilares de POO, veja **[DOCUMENTACAO.md](DOCUMENTACAO.md)**.

---

## 💡 Ideias para o futuro

- Mais personagens, com golpes especiais diferentes
- Sons e música em chiptune
- Suporte a controles (gamepad)
- Golpes especiais por sequência de comandos (ex.: ↓ ↘ → + soco)
- Placar de recordes salvo em arquivo
