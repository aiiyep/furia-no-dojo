package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Cenário em pixel art: céu em faixas com dithering, sol retrô listrado,
 * montanhas em degraus, mar com reflexo, piso de madeira em perspectiva e
 * um portal torii. A parte estática é gerada uma vez, pixel a pixel; a
 * parte animada (estrelas, reflexo e lanternas) é desenhada a cada quadro.
 */
final class RenderizadorCenario {

    /** Margem extra para o tremor de tela não mostrar bordas pretas. */
    static final int MARGEM = 4;

    private static final int L = PainelJogo.LARGURA_BAIXA;
    private static final int A = PainelJogo.ALTURA_BAIXA;
    private static final int HORIZONTE = 103;
    private static final int TOPO_PISO = 132;
    private static final int SOL_X = L / 2;
    private static final int SOL_Y = 95;
    private static final int SOL_RAIO = 38;
    private static final int[] LANTERNAS_X = {88, L - 88};

    private static final Color[] CEU = {
            new Color(24, 10, 52), new Color(44, 14, 76), new Color(74, 20, 98), new Color(112, 28, 112),
            new Color(158, 42, 118), new Color(206, 64, 112), new Color(240, 100, 96), new Color(255, 146, 80)};
    private static final Color[] SOL = {
            new Color(255, 240, 130), new Color(255, 200, 92), new Color(255, 148, 96), new Color(250, 90, 120)};
    private static final Color[] MAR = {new Color(92, 28, 96), new Color(64, 20, 78), new Color(40, 14, 58)};
    private static final Color[] PISO = {
            new Color(96, 48, 30), new Color(118, 60, 36), new Color(140, 76, 44), new Color(162, 92, 52)};
    private static final Color LINHA_PISO = new Color(70, 32, 20);
    private static final Color TORII = new Color(192, 38, 38);
    private static final Color TORII_LUZ = new Color(232, 76, 58);
    private static final Color TORII_SOMBRA = new Color(118, 18, 28);
    private static final Color TORII_PRETO = new Color(30, 12, 16);

    private final BufferedImage fundo;
    private final int[][] estrelas;

    RenderizadorCenario() {
        Random r = new Random(7);
        estrelas = new int[60][2];
        for (int[] e : estrelas) {
            e[0] = r.nextInt(L);
            e[1] = r.nextInt(60);
        }
        fundo = criarFundo();
    }

    void desenhar(Graphics2D g, long quadro) {
        g.drawImage(fundo, -MARGEM, -MARGEM, null);
        desenharEstrelasPiscando(g, quadro);
        desenharReflexo(g, quadro);
        desenharLanternas(g, quadro);
    }

    // ------------------------------------------------------------------
    // Fundo estático (gerado uma vez)
    // ------------------------------------------------------------------

    private BufferedImage criarFundo() {
        BufferedImage img = new BufferedImage(L + 2 * MARGEM, A + 2 * MARGEM, BufferedImage.TYPE_INT_RGB);
        Tela t = new Tela(img);

        // Céu em faixas com dithering nas transições
        for (int y = -MARGEM; y < HORIZONTE; y++) {
            double pos = (y + MARGEM) / (double) (HORIZONTE + MARGEM) * CEU.length;
            int faixa = Math.min(CEU.length - 1, (int) pos);
            double fracao = pos - faixa;
            for (int x = -MARGEM; x < L + MARGEM; x++) {
                Color cor = CEU[faixa];
                if (faixa < CEU.length - 1 && fracao > 0.7 && (x + y) % 2 == 0) {
                    cor = CEU[faixa + 1];
                }
                t.pixel(x, y, cor);
            }
        }

        // Estrelas fixas
        for (int[] e : estrelas) {
            t.pixel(e[0], e[1], e[1] < 30 ? new Color(255, 240, 255) : new Color(200, 160, 230));
        }

        desenharSol(t);
        desenharMontanhas(t, 22, 0.035, 0.083, new Color(112, 36, 104), new Color(150, 52, 124));
        desenharMontanhas(t, 13, 0.058, 0.13, new Color(66, 22, 74), new Color(92, 32, 96));

        // Mar
        for (int y = HORIZONTE; y < TOPO_PISO; y++) {
            int faixa = Math.min(MAR.length - 1, (y - HORIZONTE) * MAR.length / (TOPO_PISO - HORIZONTE));
            for (int x = -MARGEM; x < L + MARGEM; x++) {
                t.pixel(x, y, MAR[faixa]);
            }
        }

        desenharPiso(t);
        desenharPortal(t);
        return img;
    }

    private void desenharSol(Tela t) {
        // Halo com dithering
        Color halo = new Color(255, 150, 96);
        for (int y = SOL_Y - SOL_RAIO - 8; y < HORIZONTE; y++) {
            for (int x = SOL_X - SOL_RAIO - 8; x <= SOL_X + SOL_RAIO + 8; x++) {
                double d = Math.hypot(x - SOL_X, y - SOL_Y);
                if (d > SOL_RAIO && d <= SOL_RAIO + 7 && (x + y) % 2 == 0 && (d < SOL_RAIO + 4 || (x % 4 == 0))) {
                    t.pixel(x, y, halo);
                }
            }
        }
        // Disco em faixas, com listras horizontais na metade de baixo
        for (int y = SOL_Y - SOL_RAIO; y < HORIZONTE; y++) {
            int dy = y - SOL_Y;
            int meiaLargura = (int) Math.round(Math.sqrt(Math.max(0, SOL_RAIO * SOL_RAIO - dy * dy)));
            int inicioListras = SOL_Y - 12;
            if (y >= inicioListras) {
                int k = y - inicioListras;
                int espessura = 1 + k / 10;
                if (k % 6 < espessura) {
                    continue; // listra: o céu aparece
                }
            }
            int faixa = Math.min(SOL.length - 1, (y - (SOL_Y - SOL_RAIO)) * SOL.length / (SOL_RAIO + 8));
            for (int x = SOL_X - meiaLargura; x <= SOL_X + meiaLargura; x++) {
                t.pixel(x, y, SOL[faixa]);
            }
        }
    }

    private void desenharMontanhas(Tela t, int amplitude, double f1, double f2, Color cor, Color crista) {
        for (int x = -MARGEM; x < L + MARGEM; x++) {
            double altura = 6 + amplitude * Math.abs(Math.sin(x * f1)) + amplitude * 0.5 * Math.abs(Math.sin(x * f2 + 1));
            int topo = HORIZONTE - (int) Math.round(altura / 2) * 2; // degraus de 2 pixels
            for (int y = topo; y < HORIZONTE; y++) {
                t.pixel(x, y, y == topo ? crista : cor);
            }
        }
    }

    private void desenharPiso(Tela t) {
        for (int y = TOPO_PISO; y < A + MARGEM; y++) {
            double pos = (y - TOPO_PISO) / (double) (A + MARGEM - TOPO_PISO) * PISO.length;
            int faixa = Math.min(PISO.length - 1, (int) pos);
            double fracao = pos - faixa;
            for (int x = -MARGEM; x < L + MARGEM; x++) {
                Color cor = PISO[faixa];
                if (faixa < PISO.length - 1 && fracao > 0.75 && (x + y) % 2 == 0) {
                    cor = PISO[faixa + 1];
                }
                t.pixel(x, y, cor);
            }
        }
        // Borda do tablado
        for (int x = -MARGEM; x < L + MARGEM; x++) {
            t.pixel(x, TOPO_PISO, new Color(40, 18, 12));
            t.pixel(x, TOPO_PISO + 1, new Color(40, 18, 12));
            t.pixel(x, TOPO_PISO + 2, new Color(176, 104, 60));
        }
        // Tábuas horizontais (espaçamento crescente = perspectiva)
        for (int k = 1; k < 12; k++) {
            int y = TOPO_PISO + 3 + (int) (k * k * 0.42);
            if (y >= A + MARGEM) {
                break;
            }
            for (int x = -MARGEM; x < L + MARGEM; x++) {
                t.pixel(x, y, LINHA_PISO);
            }
        }
        // Juntas convergindo para o ponto de fuga
        int fugaX = L / 2;
        int fugaY = 40;
        for (int i = -12; i <= 12; i++) {
            int xBase = fugaX + i * 30;
            int yBase = A + MARGEM;
            for (int y = TOPO_PISO + 3; y < yBase; y++) {
                double tt = (y - fugaY) / (double) (yBase - fugaY);
                int x = (int) Math.round(fugaX + (xBase - fugaX) * tt);
                t.pixel(x, y, LINHA_PISO);
            }
        }
        // Brilho do pôr do sol no chão (dithering)
        Color brilho = new Color(196, 118, 66);
        for (int y = TOPO_PISO + 4; y < TOPO_PISO + 26; y++) {
            for (int x = SOL_X - 80; x <= SOL_X + 80; x++) {
                double dx = (x - SOL_X) / 80.0;
                double dy = (y - (TOPO_PISO + 12)) / 12.0;
                if (dx * dx + dy * dy < 1 && (x + y) % 2 == 0 && t.cor(x, y) != LINHA_PISO.getRGB()) {
                    t.pixel(x, y, brilho);
                }
            }
        }
    }

    private void desenharPortal(Tela t) {
        // Pilares com luz à esquerda e sombra à direita
        int[] pilares = {22, L - 22 - 9};
        for (int px : pilares) {
            for (int y = 22; y < TOPO_PISO; y++) {
                for (int x = px; x < px + 9; x++) {
                    Color cor = TORII;
                    if (x - px < 2) {
                        cor = TORII_LUZ;
                    } else if (x - px > 6) {
                        cor = TORII_SOMBRA;
                    }
                    t.pixel(x, y, cor);
                }
            }
            for (int y = TOPO_PISO - 6; y < TOPO_PISO + 1; y++) {
                for (int x = px - 2; x < px + 11; x++) {
                    t.pixel(x, y, TORII_PRETO);
                }
            }
        }
        // Viga inferior (nuki)
        for (int y = 37; y < 42; y++) {
            for (int x = 12; x < L - 12; x++) {
                Color cor = y == 37 ? TORII_LUZ : (y == 41 ? TORII_SOMBRA : TORII);
                t.pixel(x, y, cor);
            }
        }
        // Escora central
        for (int y = 22; y < 37; y++) {
            for (int x = L / 2 - 4; x <= L / 2 + 4; x++) {
                t.pixel(x, y, x < L / 2 - 2 ? TORII_LUZ : TORII);
            }
        }
        // Viga superior curva (kasagi): faixa preta em cima, vermelha embaixo
        for (int x = 2; x < L - 2; x++) {
            double n = (x - L / 2.0) / (L / 2.0 - 2);
            int curva = (int) Math.round(7 * n * n);
            int topo = 12 - curva;
            for (int y = topo; y < topo + 4; y++) {
                t.pixel(x, y, TORII_PRETO);
            }
            for (int y = topo + 4; y < topo + 10; y++) {
                Color cor = (y == topo + 4) ? TORII_LUZ : (y == topo + 9 ? TORII_SOMBRA : TORII);
                t.pixel(x, y, cor);
            }
        }
    }

    // ------------------------------------------------------------------
    // Partes animadas
    // ------------------------------------------------------------------

    private void desenharEstrelasPiscando(Graphics2D g, long quadro) {
        for (int i = 0; i < estrelas.length; i += 6) {
            if (((quadro / 20) + i) % 3 == 0) {
                g.setColor(Paleta.BRANCO);
                g.fillRect(estrelas[i][0], estrelas[i][1] - 1, 1, 3);
                g.fillRect(estrelas[i][0] - 1, estrelas[i][1], 3, 1);
            }
        }
    }

    private void desenharReflexo(Graphics2D g, long quadro) {
        int fase = (int) (quadro / 12);
        for (int y = HORIZONTE + 2; y < TOPO_PISO - 1; y += 2) {
            int k = y - HORIZONTE;
            int largura = Math.max(4, 44 - k * 2);
            int oscilacao = ((fase + k) % 3) - 1;
            g.setColor(k % 4 == 0 ? new Color(255, 160, 96) : new Color(250, 100, 120));
            g.fillRect(SOL_X - largura / 2 + oscilacao * 2, y, largura, 1);
            if (largura > 12) {
                g.fillRect(SOL_X - largura / 2 - 8 - oscilacao, y, 3, 1);
                g.fillRect(SOL_X + largura / 2 + 6 + oscilacao, y, 3, 1);
            }
        }
    }

    private void desenharLanternas(Graphics2D g, long quadro) {
        for (int i = 0; i < LANTERNAS_X.length; i++) {
            int lx = LANTERNAS_X[i];
            boolean forte = ((quadro / 9) + i * 3) % 4 != 0; // tremulação
            int raio = forte ? 17 : 15;
            g.setColor(new Color(255, 160, 70, 45));
            g.fillOval(lx - raio, 56 - raio, raio * 2 + 1, raio * 2 + 1);
            g.setColor(new Color(255, 180, 90, 55));
            g.fillOval(lx - raio / 2, 56 - raio / 2, raio + 1, raio + 1);

            g.setColor(TORII_PRETO);
            g.fillRect(lx, 42, 1, 7);
            g.fillRect(lx - 3, 48, 7, 2);
            g.setColor(new Color(214, 58, 40));
            g.fillRect(lx - 5, 51, 11, 11);
            g.fillRect(lx - 4, 50, 9, 13);
            g.setColor(forte ? new Color(255, 196, 96) : new Color(255, 160, 80));
            g.fillRect(lx - 3, 52, 4, 9);
            g.setColor(new Color(130, 24, 22));
            g.fillRect(lx - 5, 54, 11, 1);
            g.fillRect(lx - 5, 58, 11, 1);
            g.setColor(TORII_PRETO);
            g.fillRect(lx - 3, 63, 7, 2);
        }
    }

    /** Acesso a pixels com origem deslocada pela margem e verificação de limites. */
    private static final class Tela {
        private final BufferedImage img;

        private Tela(BufferedImage img) {
            this.img = img;
        }

        private void pixel(int x, int y, Color cor) {
            int ix = x + MARGEM;
            int iy = y + MARGEM;
            if (ix >= 0 && iy >= 0 && ix < img.getWidth() && iy < img.getHeight()) {
                img.setRGB(ix, iy, cor.getRGB());
            }
        }

        private int cor(int x, int y) {
            int ix = x + MARGEM;
            int iy = y + MARGEM;
            if (ix >= 0 && iy >= 0 && ix < img.getWidth() && iy < img.getHeight()) {
                return img.getRGB(ix, iy);
            }
            return 0;
        }
    }
}
