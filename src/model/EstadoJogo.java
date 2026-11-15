package model;

/**
 * Estados possíveis da máquina de estados principal do jogo.
 */
public enum EstadoJogo {
    /** Tela inicial: título, escolha do modo e comandos. */
    MENU,
    /** Combate em andamento. */
    JOGANDO,
    /** Pausa breve exibindo o resultado do round (K.O. / TIME OVER). */
    FIM_ROUND,
    /** Partida encerrada com um vencedor humano. */
    VITORIA,
    /** Partida encerrada com vitória da CPU (modo 1 jogador). */
    GAME_OVER
}
