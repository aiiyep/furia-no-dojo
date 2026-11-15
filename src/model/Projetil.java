package model;

import java.awt.Rectangle;

/**
 * Golpe especial à distância (esfera de energia).
 * Move-se horizontalmente até atingir o adversário, colidir com outro
 * projétil ou sair da arena.
 *
 * HERANÇA: estende {@link Entidade}.
 * POLIMORFISMO: sobrescreve atualizar(), getHurtbox() e aceitar().
 */
public class Projetil extends Entidade {

    public static final int TAMANHO = 30;
    private static final double VELOCIDADE = 8.5;

    private final Lutador dono;
    private final int dano;
    private int contadorAnimacao;

    public Projetil(Lutador dono, double x, double y, int direcao, int dano) {
        super(x, y, TAMANHO, TAMANHO);
        this.dono = dono;
        this.dano = dano;
        setVelocidadeX(direcao * VELOCIDADE);
    }

    @Override
    public void atualizar() {
        if (!isAtivo()) {
            return;
        }
        contadorAnimacao++;
        setX(getX() + getVelocidadeX());
        if (getX() + getLargura() < 0 || getX() > Arena.LARGURA) {
            setAtivo(false);
        }
    }

    @Override
    public Rectangle getHurtbox() {
        return new Rectangle((int) getX() + 3, (int) getY() + 3, TAMANHO - 6, TAMANHO - 6);
    }

    @Override
    public void aceitar(VisitanteEntidade visitante) {
        visitante.visitar(this);
    }

    public int getDirecao() {
        return getVelocidadeX() >= 0 ? 1 : -1;
    }

    public Lutador getDono() {
        return dono;
    }

    public int getDano() {
        return dano;
    }

    public int getContadorAnimacao() {
        return contadorAnimacao;
    }
}
