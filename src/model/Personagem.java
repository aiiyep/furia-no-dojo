package model;

/**
 * Personagens jogáveis. O Model guarda apenas a identidade; as cores e o
 * visual de cada personagem são responsabilidade da View.
 */
public enum Personagem {
    TAKESHI("TAKESHI"),
    SOMBRA("SOMBRA");

    private final String nome;

    Personagem(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
