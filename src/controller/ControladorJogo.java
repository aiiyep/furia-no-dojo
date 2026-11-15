package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.Timer;
import model.Jogo;
import model.ModoJogo;
import view.PainelJogo;

/**
 * CONTROLLER principal: dono do game loop.
 *
 * A cada tick do javax.swing.Timer (~60 FPS):
 *   1. lê a entrada do teclado e a converte em ações no Model;
 *   2. manda o Model avançar um frame ({@code jogo.atualizar()});
 *   3. pede para a View redesenhar ({@code painel.repaint()}).
 */
public class ControladorJogo implements ActionListener {

    private static final int DELAY_MS = 16;

    private final Jogo jogo;
    private final PainelJogo painel;
    private final ControladorTeclado teclado;
    private final Timer timer;

    public ControladorJogo(Jogo jogo, PainelJogo painel) {
        this.jogo = jogo;
        this.painel = painel;
        this.teclado = new ControladorTeclado();
        this.timer = new Timer(DELAY_MS, this);
        painel.addKeyListener(teclado);
        painel.addFocusListener(teclado);
    }

    /** Inicia o game loop e pede o foco do teclado para o painel. */
    public void iniciar() {
        timer.start();
        painel.requestFocusInWindow();
    }

    public void parar() {
        timer.stop();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        processarEntrada();
        jogo.atualizar();
        painel.repaint();
        teclado.limparNovas();
    }

    private void processarEntrada() {
        if (teclado.foiPressionada(KeyEvent.VK_F2)) {
            painel.alternarScanlines();
        }
        switch (jogo.getEstado()) {
            case MENU:
                if (teclado.foiPressionada(KeyEvent.VK_W, KeyEvent.VK_UP)) {
                    jogo.moverSelecaoMenu(-1);
                }
                if (teclado.foiPressionada(KeyEvent.VK_S, KeyEvent.VK_DOWN)) {
                    jogo.moverSelecaoMenu(1);
                }
                if (teclado.foiPressionada(KeyEvent.VK_ENTER, KeyEvent.VK_SPACE)) {
                    jogo.confirmarMenu();
                }
                break;

            case JOGANDO:
                if (teclado.foiPressionada(KeyEvent.VK_P)) {
                    jogo.alternarPausa();
                }
                if (jogo.isPausado()) {
                    if (teclado.foiPressionada(KeyEvent.VK_ESCAPE)) {
                        jogo.voltarAoMenu();
                    }
                } else {
                    comandarLutadores();
                }
                break;

            case VITORIA:
            case GAME_OVER:
                if (teclado.foiPressionada(KeyEvent.VK_ENTER)) {
                    jogo.reiniciarPartida();
                } else if (teclado.foiPressionada(KeyEvent.VK_ESCAPE)) {
                    jogo.voltarAoMenu();
                }
                break;

            default:
                break;
        }
    }

    /** No modo 1 jogador só o lutador 1 é do teclado; a CPU decide sozinha. */
    private void comandarLutadores() {
        if (jogo.getModo() == ModoJogo.UM_JOGADOR) {
            teclado.comandar(jogo.getLutador1(), MapaTeclas.JOGADOR_SOLO);
        } else {
            teclado.comandar(jogo.getLutador1(), MapaTeclas.JOGADOR_1);
            teclado.comandar(jogo.getLutador2(), MapaTeclas.JOGADOR_2);
        }
    }

    /** Usado em testes automatizados. */
    public ControladorTeclado getTeclado() {
        return teclado;
    }
}
