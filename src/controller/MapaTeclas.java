package controller;

import java.awt.event.KeyEvent;

/**
 * Mapeamento imutável de teclas para os comandos de um lutador.
 * Cada comando aceita uma ou mais teclas alternativas.
 */
public final class MapaTeclas {

    /** Modo 1 jogador: aceita todas as combinações (WASD/setas + J K L / Z X C / F G H). */
    public static final MapaTeclas JOGADOR_SOLO = new MapaTeclas(
            teclas(KeyEvent.VK_A, KeyEvent.VK_LEFT),
            teclas(KeyEvent.VK_D, KeyEvent.VK_RIGHT),
            teclas(KeyEvent.VK_W, KeyEvent.VK_UP),
            teclas(KeyEvent.VK_S, KeyEvent.VK_DOWN),
            teclas(KeyEvent.VK_J, KeyEvent.VK_Z, KeyEvent.VK_F),
            teclas(KeyEvent.VK_K, KeyEvent.VK_X, KeyEvent.VK_G),
            teclas(KeyEvent.VK_L, KeyEvent.VK_C, KeyEvent.VK_H));

    /** Modo 2 jogadores: jogador 1 no lado esquerdo do teclado. */
    public static final MapaTeclas JOGADOR_1 = new MapaTeclas(
            teclas(KeyEvent.VK_A),
            teclas(KeyEvent.VK_D),
            teclas(KeyEvent.VK_W),
            teclas(KeyEvent.VK_S),
            teclas(KeyEvent.VK_F),
            teclas(KeyEvent.VK_G),
            teclas(KeyEvent.VK_H));

    /** Modo 2 jogadores: jogador 2 nas setas + J K L (ou teclado numérico 1 2 3). */
    public static final MapaTeclas JOGADOR_2 = new MapaTeclas(
            teclas(KeyEvent.VK_LEFT),
            teclas(KeyEvent.VK_RIGHT),
            teclas(KeyEvent.VK_UP),
            teclas(KeyEvent.VK_DOWN),
            teclas(KeyEvent.VK_J, KeyEvent.VK_NUMPAD1),
            teclas(KeyEvent.VK_K, KeyEvent.VK_NUMPAD2),
            teclas(KeyEvent.VK_L, KeyEvent.VK_NUMPAD3));

    private final int[] esquerda;
    private final int[] direita;
    private final int[] cima;
    private final int[] baixo;
    private final int[] soco;
    private final int[] chute;
    private final int[] especial;

    private MapaTeclas(int[] esquerda, int[] direita, int[] cima, int[] baixo,
                       int[] soco, int[] chute, int[] especial) {
        this.esquerda = esquerda;
        this.direita = direita;
        this.cima = cima;
        this.baixo = baixo;
        this.soco = soco;
        this.chute = chute;
        this.especial = especial;
    }

    private static int[] teclas(int... codigos) {
        return codigos;
    }

    // Os getters devolvem cópias para manter o mapa imutável (encapsulamento)

    public int[] getEsquerda() {
        return esquerda.clone();
    }

    public int[] getDireita() {
        return direita.clone();
    }

    public int[] getCima() {
        return cima.clone();
    }

    public int[] getBaixo() {
        return baixo.clone();
    }

    public int[] getSoco() {
        return soco.clone();
    }

    public int[] getChute() {
        return chute.clone();
    }

    public int[] getEspecial() {
        return especial.clone();
    }
}
