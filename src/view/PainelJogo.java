package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.JPanel;
import model.Arena;
import model.EstadoJogo;
import model.Jogo;

/**
 * VIEW principal: painel de renderização (JPanel com double buffering).
 *
 * Tudo é desenhado numa imagem de baixa resolução (300x180) e depois
 * ampliado 3x com "vizinho mais próximo", sem suavização. É isso que dá o
 * visual de pixel art. O painel apenas LÊ o Model: não altera o estado do jogo.
 */
public class PainelJogo extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Fator de ampliação de cada pixel de arte. */
    public static final int ESCALA = 3;
    public static final int LARGURA_BAIXA = Arena.LARGURA / ESCALA;
    public static final int ALTURA_BAIXA = Arena.ALTURA / ESCALA;

    private final transient Jogo jogo;
    private final transient BufferedImage tela =
            new BufferedImage(LARGURA_BAIXA, ALTURA_BAIXA, BufferedImage.TYPE_INT_RGB);
    private final transient RenderizadorCenario cenario = new RenderizadorCenario();
    private final transient RenderizadorEntidades entidades = new RenderizadorEntidades();
    private final transient RenderizadorEfeitos efeitos = new RenderizadorEfeitos();
    private final transient RenderizadorHud hud = new RenderizadorHud();
    private final transient RenderizadorTelas telas = new RenderizadorTelas();
    private final transient Random aleatorio = new Random();
    private boolean scanlines = true;

    public PainelJogo(Jogo jogo) {
        this.jogo = jogo;
        setPreferredSize(new Dimension(Arena.LARGURA, Arena.ALTURA));
        setDoubleBuffered(true);
        setFocusable(true);
        setBackground(Color.BLACK);
    }

    /** Converte uma coordenada do mundo (Model) para pixel de arte. */
    static int paraPixel(double coordenadaMundo) {
        return (int) Math.round(coordenadaMundo / ESCALA);
    }

    public void alternarScanlines() {
        scanlines = !scanlines;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderizarTelaBaixa();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(tela, 0, 0, LARGURA_BAIXA * ESCALA, ALTURA_BAIXA * ESCALA, null);
        if (scanlines) {
            desenharScanlines(g2);
        }
        g2.dispose();
    }

    private void renderizarTelaBaixa() {
        Graphics2D g = tela.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        // Mundo (com tremor de tela em pixels inteiros)
        Graphics2D mundo = (Graphics2D) g.create();
        int tremor = jogo.getTremor();
        if (tremor > 0) {
            int intensidade = Math.min(RenderizadorCenario.MARGEM - 1, 1 + tremor / 4);
            mundo.translate(aleatorio.nextInt(intensidade * 2 + 1) - intensidade,
                    aleatorio.nextInt(intensidade * 2 + 1) - intensidade);
        }
        cenario.desenhar(mundo, jogo.getContadorFrames());
        entidades.desenhar(mundo, jogo.getEntidades());
        efeitos.desenhar(mundo, jogo);
        mundo.dispose();

        // Camadas de interface
        EstadoJogo estado = jogo.getEstado();
        switch (estado) {
            case MENU:
                telas.desenharMenu(g, jogo);
                break;
            case JOGANDO:
                hud.desenhar(g, jogo);
                telas.desenharIntro(g, jogo);
                if (jogo.isPausado()) {
                    telas.desenharPausa(g);
                }
                break;
            case FIM_ROUND:
                hud.desenhar(g, jogo);
                telas.desenharFimRound(g, jogo);
                break;
            case VITORIA:
            case GAME_OVER:
                telas.desenharTelaFinal(g, jogo);
                break;
            default:
                break;
        }
        g.dispose();
    }

    /** Linhas horizontais escuras imitando um monitor CRT de fliperama. */
    private void desenharScanlines(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 45));
        for (int y = ESCALA - 1; y < ALTURA_BAIXA * ESCALA; y += ESCALA) {
            g.fillRect(0, y, LARGURA_BAIXA * ESCALA, 1);
        }
    }
}
