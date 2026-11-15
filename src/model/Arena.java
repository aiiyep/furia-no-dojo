package model;

/**
 * Dimensões lógicas do mundo do jogo (em "unidades de mundo").
 * O Model não conhece pixels de tela: a View converte essas unidades
 * para a resolução de pixel art.
 */
public final class Arena {

    public static final int LARGURA = 900;
    public static final int ALTURA = 540;
    public static final int CHAO_Y = 471;
    public static final int MARGEM = 15;

    private Arena() {
        // classe utilitária: não instanciável
    }
}
