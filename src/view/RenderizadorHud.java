package view;

import java.awt.Color;
import java.awt.Graphics2D;
import model.EstadoJogo;
import model.Jogo;
import model.Lutador;
import model.ModoJogo;
import model.Pontuacao;

/**
 * HUD clássico de luta em pixel art: barras de vida que esvaziam em direção
 * ao centro, relógio, rounds vencidos, recarga do especial, pontuação e combos.
 */
final class RenderizadorHud {

    private static final int L = PainelJogo.LARGURA_BAIXA;
    private static final int LARGURA_BARRA = 118;
    private static final int ALTURA_BARRA = 7;
    private static final int X_BARRA_1 = 10;
    private static final int X_BARRA_2 = L - 10 - LARGURA_BARRA;
    private static final int Y_BARRA = 7;

    void desenhar(Graphics2D g, Jogo jogo) {
        long quadro = jogo.getContadorFrames();
        Lutador l1 = jogo.getLutador1();
        Lutador l2 = jogo.getLutador2();

        desenharBarraVida(g, X_BARRA_1, l1, jogo.getVidaExibida(0), true, quadro);
        desenharBarraVida(g, X_BARRA_2, l2, jogo.getVidaExibida(1), false, quadro);

        // Nomes com o rótulo do controle (1P, 2P ou CPU)
        FontePixel.desenhar(g, jogo.getRotuloLado(0) + " " + l1.getNome(), X_BARRA_1, 18, 1, Paleta.TEXTO, Paleta.CONTORNO);
        FontePixel.desenharDireita(g, l2.getNome() + " " + jogo.getRotuloLado(1), X_BARRA_2 + LARGURA_BARRA, 18, 1,
                Paleta.TEXTO, Paleta.CONTORNO);

        // Rounds vencidos
        for (int i = 0; i < Jogo.ROUNDS_PARA_VENCER; i++) {
            desenharMarcador(g, X_BARRA_1 + LARGURA_BARRA - 6 - i * 8, 18, i < jogo.getVitorias(0));
            desenharMarcador(g, X_BARRA_2 + 1 + i * 8, 18, i < jogo.getVitorias(1));
        }

        // Recarga do especial
        desenharBarraEspecial(g, X_BARRA_1, 29, l1, false, quadro);
        desenharBarraEspecial(g, X_BARRA_2 + LARGURA_BARRA - 50, 29, l2, true, quadro);

        // Pontuação
        FontePixel.desenhar(g, String.format("%07d", jogo.getPontuacao(0).getTotal()), X_BARRA_1, 37, 1,
                Paleta.DOURADO, Paleta.CONTORNO);
        if (jogo.getModo() == ModoJogo.DOIS_JOGADORES) {
            FontePixel.desenharDireita(g, String.format("%07d", jogo.getPontuacao(1).getTotal()),
                    X_BARRA_2 + LARGURA_BARRA, 37, 1, Paleta.DOURADO, Paleta.CONTORNO);
        } else {
            FontePixel.desenharDireita(g, "NIVEL " + jogo.getNivelCpu(), X_BARRA_2 + LARGURA_BARRA, 37, 1,
                    Paleta.ROSA, Paleta.CONTORNO);
        }

        desenharRelogio(g, jogo, quadro);
        FontePixel.desenharCentralizado(g, "ROUND " + jogo.getRoundAtual(), L / 2, 29, 1,
                Paleta.ROSA, Paleta.ROSA, Paleta.CONTORNO);

        // Combos (só durante o combate, para não cobrir o letreiro de K.O.)
        if (jogo.getEstado() == EstadoJogo.JOGANDO) {
            desenharCombo(g, jogo.getPontuacao(0), X_BARRA_1, true);
            desenharCombo(g, jogo.getPontuacao(1), X_BARRA_2 + LARGURA_BARRA, false);
        }
    }

    private void desenharBarraVida(Graphics2D g, int x, Lutador lutador, double vidaExibida,
                                   boolean ancoradaADireita, long quadro) {
        int w = LARGURA_BARRA;
        int h = ALTURA_BARRA;
        int y = Y_BARRA;
        int larguraVida = (int) Math.round(w * lutador.getVida() / (double) Lutador.VIDA_MAXIMA);
        int larguraRastro = (int) Math.round(w * vidaExibida / Lutador.VIDA_MAXIMA);

        g.setColor(Paleta.PRETO);
        g.fillRect(x - 2, y - 2, w + 4, h + 4);
        g.setColor(Paleta.MOLDURA);
        g.drawRect(x - 2, y - 2, w + 3, h + 3);
        g.setColor(Paleta.VERMELHO_ESCURO);
        g.fillRect(x, y, w, h);

        // A barra esvazia em direção ao centro da tela
        int xRastro = ancoradaADireita ? x + w - larguraRastro : x;
        int xVida = ancoradaADireita ? x + w - larguraVida : x;
        g.setColor(Paleta.VERMELHO);
        g.fillRect(xRastro, y, larguraRastro, h);

        boolean critico = lutador.getVida() <= Lutador.VIDA_MAXIMA / 4 && (quadro / 8) % 2 == 0;
        Color[] faixas = critico
                ? new Color[]{new Color(255, 200, 120), new Color(255, 120, 40), new Color(220, 60, 20)}
                : new Color[]{new Color(255, 252, 190), new Color(255, 216, 60), new Color(232, 150, 0)};
        g.setColor(faixas[0]);
        g.fillRect(xVida, y, larguraVida, 2);
        g.setColor(faixas[1]);
        g.fillRect(xVida, y + 2, larguraVida, 3);
        g.setColor(faixas[2]);
        g.fillRect(xVida, y + 5, larguraVida, 2);
    }

    private void desenharMarcador(Graphics2D g, int x, int y, boolean vencido) {
        g.setColor(Paleta.PRETO);
        g.fillRect(x - 1, y - 1, 7, 7);
        g.setColor(vencido ? Paleta.DOURADO : new Color(70, 60, 76));
        g.fillRect(x, y, 5, 5);
        if (vencido) {
            g.setColor(Paleta.BRANCO);
            g.fillRect(x + 1, y + 1, 1, 1);
        }
    }

    private void desenharBarraEspecial(Graphics2D g, int x, int y, Lutador lutador, boolean alinharDireita, long quadro) {
        int w = 50;
        int h = 3;
        boolean pronto = lutador.isEspecialPronto();
        Color cor = Paleta.de(lutador.getPersonagem()).energiaBrilho;
        if (pronto && (quadro / 6) % 2 == 0) {
            cor = Paleta.BRANCO;
        }
        g.setColor(Paleta.PRETO);
        g.fillRect(x - 1, y - 1, w + 2, h + 2);
        g.setColor(new Color(36, 30, 56));
        g.fillRect(x, y, w, h);
        int preenchido = (int) (w * lutador.getPercentualRecarga());
        g.setColor(cor);
        g.fillRect(alinharDireita ? x + w - preenchido : x, y, preenchido, h);

        String rotulo = pronto ? "SP!" : "SP";
        Color corRotulo = pronto ? Paleta.AZUL_CLARO : Paleta.TEXTO_APAGADO;
        if (alinharDireita) {
            FontePixel.desenharDireita(g, rotulo, x - 4, y - 2, 1, corRotulo, Paleta.CONTORNO);
        } else {
            FontePixel.desenhar(g, rotulo, x + w + 4, y - 2, 1, corRotulo, Paleta.CONTORNO);
        }
    }

    private void desenharRelogio(Graphics2D g, Jogo jogo, long quadro) {
        int x = L / 2 - 14;
        int y = 3;
        g.setColor(Paleta.PRETO);
        g.fillRect(x, y, 29, 22);
        g.setColor(Paleta.MOLDURA);
        g.drawRect(x + 1, y + 1, 26, 19);

        int tempo = Math.max(0, jogo.getTempoRestante());
        boolean critico = tempo <= 10 && (quadro / 15) % 2 == 0;
        Color topo = critico ? new Color(255, 160, 140) : Paleta.BRANCO;
        Color base = critico ? Paleta.VERMELHO : Paleta.DOURADO;
        FontePixel.desenharCentralizado(g, String.format("%02d", tempo), L / 2 + 1, y + 4, 2, topo, base, Paleta.CONTORNO);
    }

    private void desenharCombo(Graphics2D g, Pontuacao pontuacao, int x, boolean alinharEsquerda) {
        if (pontuacao.getFramesTextoCombo() <= 0) {
            return;
        }
        String texto = pontuacao.getComboExibido() + " HITS!";
        int escala = pontuacao.getFramesTextoCombo() > 62 ? 3 : 2;
        int largura = FontePixel.largura(texto, escala);
        int tx = alinharEsquerda ? x : x - largura;
        FontePixel.desenhar(g, texto, tx, 49, escala, new Color(255, 250, 170), Paleta.LARANJA, Paleta.CONTORNO);
    }
}
