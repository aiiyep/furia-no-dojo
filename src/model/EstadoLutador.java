package model;

/**
 * Estados (posturas) de um lutador. Cada estado define a pose desenhada,
 * a hurtbox e quais ações são permitidas.
 */
public enum EstadoLutador {
    PARADO(false),
    ANDANDO(false),
    PULANDO(false),
    AGACHADO(false),
    DEFENDENDO(false),
    SOCANDO(true),
    CHUTANDO(true),
    ESPECIAL(true),
    ATINGIDO(false),
    DERROTADO(false),
    VITORIOSO(false);

    private final boolean ataque;

    EstadoLutador(boolean ataque) {
        this.ataque = ataque;
    }

    /** @return true se o estado representa a execução de um golpe. */
    public boolean isAtaque() {
        return ataque;
    }
}
