package model;

/**
 * Modos de jogo disponíveis no menu.
 */
public enum ModoJogo {
    UM_JOGADOR("1 JOGADOR", "VS CPU"),
    DOIS_JOGADORES("2 JOGADORES", "LOCAL");

    private final String titulo;
    private final String descricao;

    ModoJogo(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }
}
