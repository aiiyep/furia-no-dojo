package model;

/**
 * Texto de pontuação que sobe e desaparece (ex.: "+200").
 */
public class TextoFlutuante {

    /** Tipo do texto, que a View traduz em cores. */
    public enum Tipo {
        PONTOS, DEFESA, INFO
    }

    private static final int DURACAO = 50;

    private final String texto;
    private final double x;
    private double y;
    private int vida = DURACAO;
    private final Tipo tipo;

    public TextoFlutuante(String texto, double x, double y, Tipo tipo) {
        this.texto = texto;
        this.x = x;
        this.y = y;
        this.tipo = tipo;
    }

    public void atualizar() {
        y -= 1.0;
        vida--;
    }

    public boolean isVivo() {
        return vida > 0;
    }

    public String getTexto() {
        return texto;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getVida() {
        return vida;
    }

    public Tipo getTipo() {
        return tipo;
    }
}
