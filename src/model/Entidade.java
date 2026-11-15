package model;

import java.awt.Rectangle;

/**
 * Classe abstrata base para todo objeto que existe na arena
 * (lutadores e projéteis).
 *
 * ABSTRAÇÃO: define "o que" toda entidade faz (atualizar, ter hurtbox,
 * aceitar um visitante) sem dizer "como". Cada subclasse implementa.
 * ENCAPSULAMENTO: atributos privados acessados apenas por getters/setters.
 *
 * No MVC, o desenho saiu daqui e foi para a View: a entidade apenas
 * "aceita" um {@link VisitanteEntidade}, e a View decide como desenhá-la.
 */
public abstract class Entidade {

    private double x;
    private double y;
    private int largura;
    private int altura;
    private double velocidadeX;
    private double velocidadeY;
    private boolean ativo;

    protected Entidade(double x, double y, int largura, int altura) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.ativo = true;
    }

    /** Atualiza a lógica da entidade (chamado uma vez por frame). */
    public abstract void atualizar();

    /** Retângulo vulnerável/colidível da entidade. */
    public abstract Rectangle getHurtbox();

    /** Encaminha a entidade ao método do visitante que corresponde ao seu tipo. */
    public abstract void aceitar(VisitanteEntidade visitante);

    /** Verifica colisão entre as hurtboxes de duas entidades. */
    public boolean colideCom(Entidade outra) {
        return outra != null && getHurtbox().intersects(outra.getHurtbox());
    }

    public double getCentroX() {
        return x + largura / 2.0;
    }

    public double getCentroY() {
        return y + altura / 2.0;
    }

    // ------------------------------------------------------------------
    // Getters e setters
    // ------------------------------------------------------------------

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getLargura() {
        return largura;
    }

    public void setLargura(int largura) {
        this.largura = largura;
    }

    public int getAltura() {
        return altura;
    }

    public void setAltura(int altura) {
        this.altura = altura;
    }

    public double getVelocidadeX() {
        return velocidadeX;
    }

    public void setVelocidadeX(double velocidadeX) {
        this.velocidadeX = velocidadeX;
    }

    public double getVelocidadeY() {
        return velocidadeY;
    }

    public void setVelocidadeY(double velocidadeY) {
        this.velocidadeY = velocidadeY;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
