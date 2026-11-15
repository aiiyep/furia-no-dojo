package view;

import java.awt.Color;
import model.Personagem;

/**
 * Paleta de cores limitada (estilo 16 bits) usada por toda a View.
 * O Model só conhece o {@link Personagem}; aqui ele vira cores.
 */
final class Paleta {

    static final Color CONTORNO = new Color(22, 12, 28);
    static final Color PRETO = new Color(10, 6, 14);
    static final Color BRANCO = new Color(255, 250, 240);
    static final Color TEXTO = new Color(255, 245, 225);
    static final Color TEXTO_APAGADO = new Color(150, 140, 165);
    static final Color DOURADO = new Color(255, 222, 90);
    static final Color LARANJA = new Color(245, 130, 40);
    static final Color VERMELHO = new Color(230, 40, 40);
    static final Color VERMELHO_ESCURO = new Color(120, 0, 20);
    static final Color ROSA = new Color(255, 170, 210);
    static final Color AZUL_CLARO = new Color(150, 220, 255);
    static final Color MOLDURA = new Color(255, 225, 150);

    /** Conjunto de cores de um personagem. */
    static final class CoresLutador {
        final Color kimono;
        final Color kimonoSombra;
        final Color kimonoLuz;
        final Color faixa;
        final Color faixaSombra;
        final Color luvas;
        final Color luvasSombra;
        final Color pele;
        final Color peleSombra;
        final Color cabelo;
        final Color energiaNucleo;
        final Color energiaBrilho;

        CoresLutador(Color kimono, Color kimonoSombra, Color kimonoLuz, Color faixa, Color faixaSombra,
                     Color luvas, Color luvasSombra, Color pele, Color peleSombra, Color cabelo,
                     Color energiaNucleo, Color energiaBrilho) {
            this.kimono = kimono;
            this.kimonoSombra = kimonoSombra;
            this.kimonoLuz = kimonoLuz;
            this.faixa = faixa;
            this.faixaSombra = faixaSombra;
            this.luvas = luvas;
            this.luvasSombra = luvasSombra;
            this.pele = pele;
            this.peleSombra = peleSombra;
            this.cabelo = cabelo;
            this.energiaNucleo = energiaNucleo;
            this.energiaBrilho = energiaBrilho;
        }
    }

    private static final CoresLutador TAKESHI = new CoresLutador(
            new Color(236, 232, 220), new Color(160, 156, 170), new Color(255, 255, 255),
            new Color(214, 36, 44), new Color(140, 16, 30),
            new Color(214, 36, 44), new Color(140, 16, 30),
            new Color(236, 180, 132), new Color(184, 120, 88),
            new Color(52, 34, 26),
            new Color(110, 200, 255), new Color(40, 110, 255));

    private static final CoresLutador SOMBRA = new CoresLutador(
            new Color(54, 62, 112), new Color(32, 36, 70), new Color(88, 100, 160),
            new Color(255, 196, 36), new Color(190, 120, 20),
            new Color(255, 196, 36), new Color(190, 120, 20),
            new Color(196, 136, 96), new Color(140, 92, 64),
            new Color(28, 26, 36),
            new Color(255, 140, 220), new Color(160, 50, 255));

    private Paleta() {
    }

    static CoresLutador de(Personagem personagem) {
        return personagem == Personagem.TAKESHI ? TAKESHI : SOMBRA;
    }

    /** Interpola duas cores (t = 0 → a, t = 1 → b). */
    static Color misturar(Color a, Color b, double t) {
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    static Color comAlfa(Color c, int alfa) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alfa)));
    }
}
