package view;

import java.awt.Color;
import java.awt.Graphics2D;
import model.Jogo;
import model.ModoJogo;
import model.Pontuacao;

/**
 * Telas e mensagens sobrepostas: menu, intro do round, resultado do round,
 * pausa e tela final.
 */
final class RenderizadorTelas {

    private static final int L = PainelJogo.LARGURA_BAIXA;
    private static final int A = PainelJogo.ALTURA_BAIXA;
    private static final int CX = L / 2;

    private static final Color AMARELO_CLARO = new Color(255, 250, 190);

    // ------------------------------------------------------------------
    // Menu
    // ------------------------------------------------------------------

    void desenharMenu(Graphics2D g, Jogo jogo) {
        long quadro = jogo.getContadorFrames();
        escurecer(g, 150);

        int flutua = (quadro / 30) % 2 == 0 ? 0 : 1;
        FontePixel.desenharCentralizado(g, "FÚRIA NO DOJO", CX, 12 + flutua, 3, AMARELO_CLARO, Paleta.VERMELHO, Paleta.CONTORNO);
        FontePixel.desenharCentralizado(g, "ARCADE DE LUTA  -  MELHOR DE 3 ROUNDS", CX, 39, 1,
                Paleta.ROSA, Paleta.ROSA, Paleta.CONTORNO);

        // Opções de modo
        ModoJogo[] modos = ModoJogo.values();
        for (int i = 0; i < modos.length; i++) {
            boolean selecionado = i == jogo.getOpcaoMenu();
            String texto = modos[i].getTitulo() + " (" + modos[i].getDescricao() + ")";
            int y = 54 + i * 14;
            Color topo = selecionado ? AMARELO_CLARO : Paleta.TEXTO_APAGADO;
            Color base = selecionado ? Paleta.LARANJA : Paleta.TEXTO_APAGADO;
            FontePixel.desenharCentralizado(g, texto, CX, y, 1, topo, base, Paleta.CONTORNO);
            if (selecionado && (quadro / 15) % 2 == 0) {
                int meia = FontePixel.largura(texto, 1) / 2;
                FontePixel.desenhar(g, ">", CX - meia - 12, y, 1, Paleta.DOURADO, Paleta.CONTORNO);
                FontePixel.desenhar(g, "<", CX + meia + 7, y, 1, Paleta.DOURADO, Paleta.CONTORNO);
            }
        }

        // Quadro de comandos (muda conforme o modo selecionado)
        int bx = 26;
        int by = 82;
        int bw = L - 52;
        int bh = 68;
        g.setColor(Paleta.comAlfa(Paleta.PRETO, 200));
        g.fillRect(bx, by, bw, bh);
        g.setColor(Paleta.MOLDURA);
        g.drawRect(bx, by, bw - 1, bh - 1);

        // Colunas: ação | jogador 1 | jogador 2 (vazia no modo solo)
        String[][] linhas;
        if (modos[jogo.getOpcaoMenu()] == ModoJogo.UM_JOGADOR) {
            linhas = new String[][]{
                    {"MOVER", "A D", "OU SETAS"},
                    {"PULAR/AGACHAR", "W S", "OU SETAS"},
                    {"SOCO RÁPIDO", "J", "OU Z"},
                    {"CHUTE FORTE", "K", "OU X"},
                    {"ESPECIAL", "L", "OU C"},
            };
        } else {
            linhas = new String[][]{
                    {"", "JOGADOR 1", "JOGADOR 2"},
                    {"MOVER/PULAR", "W A S D", "SETAS"},
                    {"SOCO RÁPIDO", "F", "J OU NUM 1"},
                    {"CHUTE FORTE", "G", "K OU NUM 2"},
                    {"ESPECIAL", "H", "L OU NUM 3"},
            };
        }
        for (int i = 0; i < linhas.length; i++) {
            int y = by + 6 + i * 10;
            Color corColunas = (i == 0 && linhas[i][0].isEmpty()) ? Paleta.ROSA : Paleta.TEXTO;
            FontePixel.desenhar(g, linhas[i][0], bx + 7, y, 1, Paleta.DOURADO, Paleta.CONTORNO);
            FontePixel.desenhar(g, linhas[i][1], bx + 96, y, 1, corColunas, Paleta.CONTORNO);
            FontePixel.desenhar(g, linhas[i][2], bx + 164, y, 1, corColunas, Paleta.CONTORNO);
        }
        FontePixel.desenharCentralizado(g, "SEGURAR PARA TRÁS = DEFESA    P = PAUSA", CX, by + bh - 12, 1,
                Paleta.TEXTO_APAGADO, Paleta.TEXTO_APAGADO, Paleta.CONTORNO);

        if ((quadro / 30) % 2 == 0) {
            FontePixel.desenharCentralizado(g, "W/S ESCOLHER   ENTER LUTAR", CX, 156, 1,
                    Paleta.BRANCO, Paleta.BRANCO, Paleta.CONTORNO);
        }
        if (jogo.getRecorde() > 0) {
            FontePixel.desenharCentralizado(g, String.format("RECORDE %07d", jogo.getRecorde()), CX, 169, 1,
                    Paleta.DOURADO, Paleta.DOURADO, Paleta.CONTORNO);
        }
    }

    // ------------------------------------------------------------------
    // Combate
    // ------------------------------------------------------------------

    void desenharIntro(Graphics2D g, Jogo jogo) {
        int frames = jogo.getFramesIntro();
        if (frames <= 0) {
            return;
        }
        if (frames > Jogo.INICIO_TEXTO_LUTEM) {
            FontePixel.desenharCentralizado(g, "ROUND " + jogo.getRoundAtual(), CX, 62, 4,
                    AMARELO_CLARO, Paleta.LARANJA, Paleta.CONTORNO);
        } else {
            int escala = frames > Jogo.INICIO_TEXTO_LUTEM - 6 ? 6 : 5;
            FontePixel.desenharCentralizado(g, "LUTEM!", CX, 80 - escala * 7 / 2, escala,
                    Paleta.BRANCO, Paleta.VERMELHO, Paleta.CONTORNO);
        }
    }

    void desenharFimRound(Graphics2D g, Jogo jogo) {
        int frames = jogo.getFramesFimRound();
        if (jogo.isFimPorNocaute()) {
            int escala = frames < 8 ? 9 : 7;
            FontePixel.desenharCentralizado(g, "K.O.", CX, 78 - escala * 7 / 2 - 6, escala,
                    new Color(255, 245, 130), Paleta.VERMELHO, Paleta.CONTORNO);
        } else {
            FontePixel.desenharCentralizado(g, "TIME OVER", CX, 52, 3, Paleta.BRANCO, Paleta.LARANJA, Paleta.CONTORNO);
        }
        if (frames > Jogo.FRAME_EXIBIR_RESULTADO) {
            String sub = jogo.getSubMensagemFimRound();
            int escala = FontePixel.escalaQueCabe(sub, L - 16, 2);
            FontePixel.desenharCentralizado(g, sub, CX, 108, escala, Paleta.BRANCO, Paleta.DOURADO, Paleta.CONTORNO);
            String detalhe = jogo.getDetalheFimRound();
            if (!detalhe.isEmpty()) {
                FontePixel.desenharCentralizado(g, detalhe, CX, 126, 1, Paleta.DOURADO, Paleta.DOURADO, Paleta.CONTORNO);
            }
        }
    }

    void desenharPausa(Graphics2D g) {
        escurecer(g, 150);
        FontePixel.desenharCentralizado(g, "PAUSA", CX, 66, 4, AMARELO_CLARO, Paleta.LARANJA, Paleta.CONTORNO);
        FontePixel.desenharCentralizado(g, "P CONTINUAR    ESC MENU", CX, 104, 1, Paleta.TEXTO, Paleta.TEXTO, Paleta.CONTORNO);
    }

    // ------------------------------------------------------------------
    // Tela final
    // ------------------------------------------------------------------

    void desenharTelaFinal(Graphics2D g, Jogo jogo) {
        long quadro = jogo.getContadorFrames();
        escurecer(g, 170);
        boolean doisJogadores = jogo.getModo() == ModoJogo.DOIS_JOGADORES;
        int vencedor = jogo.getLadoVencedorPartida();

        String titulo;
        Color topo;
        Color base;
        if (doisJogadores) {
            titulo = jogo.getRotuloLado(vencedor) + " " + jogo.getLutador(vencedor).getNome() + " VENCE!";
            topo = AMARELO_CLARO;
            base = Paleta.LARANJA;
        } else if (vencedor == 0) {
            titulo = "VOCÊ VENCEU!";
            topo = AMARELO_CLARO;
            base = Paleta.LARANJA;
        } else {
            titulo = "GAME OVER";
            topo = new Color(255, 150, 150);
            base = new Color(170, 0, 30);
        }
        int escala = FontePixel.escalaQueCabe(titulo, L - 12, 3);
        FontePixel.desenharCentralizado(g, titulo, CX, 12, escala, topo, base, Paleta.CONTORNO);

        String placar = String.format("%s %d  X  %d %s", jogo.getLutador1().getNome(), jogo.getVitorias(0),
                jogo.getVitorias(1), jogo.getLutador2().getNome());
        FontePixel.desenharCentralizado(g, placar, CX, 38, 1, Paleta.TEXTO, Paleta.TEXTO, Paleta.CONTORNO);

        int bx = doisJogadores ? 40 : 55;
        int bw = L - 2 * bx;
        int by = 51;
        int bh = 92;
        g.setColor(Paleta.comAlfa(new Color(24, 12, 40), 235));
        g.fillRect(bx, by, bw, bh);
        g.setColor(Paleta.MOLDURA);
        g.drawRect(bx, by, bw - 1, bh - 1);

        String[] rotulos = {"GOLPES", "BÔNUS DE VIDA", "BÔNUS DE TEMPO", "BÔNUS DE VITÓRIA", "MAIOR COMBO"};
        Pontuacao p1 = jogo.getPontuacao(0);
        Pontuacao p2 = jogo.getPontuacao(1);
        int colunaValor2 = bx + bw - 8;
        int colunaValor1 = doisJogadores ? colunaValor2 - 56 : colunaValor2;

        if (doisJogadores) {
            FontePixel.desenharDireita(g, "1P", colunaValor1, by + 5, 1, Paleta.ROSA, Paleta.CONTORNO);
            FontePixel.desenharDireita(g, "2P", colunaValor2, by + 5, 1, Paleta.ROSA, Paleta.CONTORNO);
        }
        int yInicio = by + (doisJogadores ? 16 : 8);
        for (int i = 0; i < rotulos.length; i++) {
            int y = yInicio + i * 11;
            FontePixel.desenhar(g, rotulos[i], bx + 8, y, 1, Paleta.TEXTO, Paleta.CONTORNO);
            FontePixel.desenharDireita(g, valor(p1, i), colunaValor1, y, 1, Paleta.DOURADO, Paleta.CONTORNO);
            if (doisJogadores) {
                FontePixel.desenharDireita(g, valor(p2, i), colunaValor2, y, 1, Paleta.DOURADO, Paleta.CONTORNO);
            }
        }
        g.setColor(Paleta.comAlfa(Paleta.MOLDURA, 150));
        g.fillRect(bx + 6, by + bh - 22, bw - 12, 1);
        FontePixel.desenhar(g, "TOTAL", bx + 8, by + bh - 16, 1, Paleta.BRANCO, Paleta.CONTORNO);
        FontePixel.desenharDireita(g, String.format("%07d", p1.getTotal()), colunaValor1, by + bh - 16, 1,
                AMARELO_CLARO, Paleta.CONTORNO);
        if (doisJogadores) {
            FontePixel.desenharDireita(g, String.format("%07d", p2.getTotal()), colunaValor2, by + bh - 16, 1,
                    AMARELO_CLARO, Paleta.CONTORNO);
        }

        FontePixel.desenharCentralizado(g, String.format("RECORDE DA SESSÃO %07d", jogo.getRecorde()), CX, 150, 1,
                Paleta.ROSA, Paleta.ROSA, Paleta.CONTORNO);
        if ((quadro / 30) % 2 == 0) {
            FontePixel.desenharCentralizado(g, "ENTER JOGAR DE NOVO    ESC MENU", CX, 165, 1,
                    Paleta.BRANCO, Paleta.BRANCO, Paleta.CONTORNO);
        }
    }

    private static String valor(Pontuacao p, int linha) {
        switch (linha) {
            case 0:
                return String.format("%07d", p.getPontosGolpes());
            case 1:
                return String.format("%07d", p.getBonusVida());
            case 2:
                return String.format("%07d", p.getBonusTempo());
            case 3:
                return String.format("%07d", p.getBonusVitoria());
            default:
                return p.getMaiorCombo() + " HITS";
        }
    }

    private static void escurecer(Graphics2D g, int alfa) {
        g.setColor(Paleta.comAlfa(Paleta.PRETO, alfa));
        g.fillRect(0, 0, L, A);
    }
}
