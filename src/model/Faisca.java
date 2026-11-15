package model;

/**
 * Partícula de impacto (estado apenas; a View decide cor e forma).
 */
public class Faisca {

    /** Tipo da faísca, que a View traduz em cores. */
    public enum Tipo {
        IMPACTO, BRILHO, DEFESA, ANULACAO
    }

    private double x;
    private double y;
    private double velocidadeX;
    private double velocidadeY;
    private int vida;
    private final int vidaMaxima;
    private final Tipo tipo;

    public Faisca(double x, double y, double velocidadeX, double velocidadeY, int vida, Tipo tipo) {
        this.x = x;
        this.y = y;
        this.velocidadeX = velocidadeX;
        this.velocidadeY = velocidadeY;
        this.vida = vida;
        this.vidaMaxima = vida;
        this.tipo = tipo;
    }

    public void atualizar() {
        x += velocidadeX;
        y += velocidadeY;
        velocidadeY += 0.25;
        velocidadeX *= 0.94;
        vida--;
    }

    public boolean isViva() {
        return vida > 0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVelocidadeX() {
        return velocidadeX;
    }

    public double getVelocidadeY() {
        return velocidadeY;
    }

    /** @return fração de vida restante (1 = recém-criada, 0 = sumindo). */
    public double getFracaoVida() {
        return vida / (double) vidaMaxima;
    }

    public Tipo getTipo() {
        return tipo;
    }
}
