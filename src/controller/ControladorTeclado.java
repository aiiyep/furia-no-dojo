package controller;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import model.Lutador;

/**
 * Lê o teclado (KeyListener) e guarda o estado de cada tecla em variáveis
 * booleanas: "pressionada" (segurando) e "nova" (apertada neste frame).
 * Também traduz teclas em comandos de um lutador segundo um {@link MapaTeclas}.
 */
public class ControladorTeclado implements KeyListener, FocusListener {

    private static final int TOTAL_TECLAS = 1024;

    private final boolean[] pressionadas = new boolean[TOTAL_TECLAS];
    private final boolean[] novas = new boolean[TOTAL_TECLAS];

    /** @return true se alguma das teclas está sendo segurada. */
    public boolean estaPressionada(int... codigos) {
        for (int c : codigos) {
            if (valido(c) && pressionadas[c]) {
                return true;
            }
        }
        return false;
    }

    /** @return true se alguma das teclas foi apertada neste frame (borda de subida). */
    public boolean foiPressionada(int... codigos) {
        for (int c : codigos) {
            if (valido(c) && novas[c]) {
                return true;
            }
        }
        return false;
    }

    /** Deve ser chamado ao fim de cada frame. */
    public void limparNovas() {
        Arrays.fill(novas, false);
    }

    /** Traduz o estado das teclas em comandos para o lutador. */
    public void comandar(Lutador lutador, MapaTeclas mapa) {
        lutador.setMovendoEsquerda(estaPressionada(mapa.getEsquerda()));
        lutador.setMovendoDireita(estaPressionada(mapa.getDireita()));
        lutador.setAgachando(estaPressionada(mapa.getBaixo()));
        if (estaPressionada(mapa.getCima())) {
            lutador.pular();
        }
        if (foiPressionada(mapa.getSoco())) {
            lutador.socar();
        }
        if (foiPressionada(mapa.getChute())) {
            lutador.chutar();
        }
        if (foiPressionada(mapa.getEspecial())) {
            lutador.lancarEspecial();
        }
    }

    private static boolean valido(int codigo) {
        return codigo >= 0 && codigo < TOTAL_TECLAS;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int c = e.getKeyCode();
        if (valido(c)) {
            if (!pressionadas[c]) {
                novas[c] = true; // ignora a auto-repetição do sistema operacional
            }
            pressionadas[c] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int c = e.getKeyCode();
        if (valido(c)) {
            pressionadas[c] = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // não utilizado
    }

    @Override
    public void focusGained(FocusEvent e) {
        // nada a fazer
    }

    @Override
    public void focusLost(FocusEvent e) {
        Arrays.fill(pressionadas, false); // evita teclas "presas" ao trocar de janela
    }
}
