package view;

import java.awt.Color;
import java.awt.Graphics2D;
import model.Faisca;
import model.Jogo;
import model.TextoFlutuante;

/**
 * Desenha faíscas de impacto (blocos de pixel) e textos flutuantes de pontos.
 */
final class RenderizadorEfeitos {

    void desenhar(Graphics2D g, Jogo jogo) {
        for (Faisca f : jogo.getFaiscas()) {
            desenharFaisca(g, f);
        }
        for (TextoFlutuante t : jogo.getTextos()) {
            desenharTexto(g, t);
        }
    }

    private void desenharFaisca(Graphics2D g, Faisca f) {
        int x = PainelJogo.paraPixel(f.getX());
        int y = PainelJogo.paraPixel(f.getY());
        double vida = f.getFracaoVida();
        Color cor = corDa(f.getTipo(), vida);
        int tamanho = vida > 0.6 ? 2 : 1;
        g.setColor(cor);
        g.fillRect(x, y, tamanho, tamanho);
        if (vida > 0.4) {
            int rx = x - (int) Math.signum(f.getVelocidadeX());
            int ry = y - (int) Math.signum(f.getVelocidadeY());
            g.fillRect(rx, ry, 1, 1);
        }
    }

    private static Color corDa(Faisca.Tipo tipo, double vida) {
        switch (tipo) {
            case IMPACTO:
                return vida > 0.5 ? Paleta.DOURADO : Paleta.LARANJA;
            case DEFESA:
                return vida > 0.5 ? Paleta.BRANCO : Paleta.AZUL_CLARO;
            case ANULACAO:
                return vida > 0.5 ? Paleta.ROSA : new Color(170, 90, 255);
            default:
                return Paleta.BRANCO;
        }
    }

    private void desenharTexto(Graphics2D g, TextoFlutuante t) {
        if (t.getVida() < 14 && t.getVida() % 4 < 2) {
            return; // pisca antes de sumir
        }
        Color cor;
        switch (t.getTipo()) {
            case DEFESA:
                cor = Paleta.AZUL_CLARO;
                break;
            case INFO:
                cor = Paleta.ROSA;
                break;
            default:
                cor = Paleta.DOURADO;
                break;
        }
        int x = PainelJogo.paraPixel(t.getX());
        int y = PainelJogo.paraPixel(t.getY());
        FontePixel.desenharCentralizado(g, t.getTexto(), x, y, 1, Paleta.BRANCO, cor, Paleta.CONTORNO);
    }
}
