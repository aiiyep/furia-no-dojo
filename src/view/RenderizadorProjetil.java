package view;

import java.awt.Graphics2D;
import model.Projetil;

/**
 * Desenha o projétil especial como uma esfera de energia em pixel art,
 * com rastro em blocos e brilho que alterna entre 2 quadros.
 */
final class RenderizadorProjetil {

    void desenhar(Graphics2D g, Projetil projetil) {
        if (!projetil.isAtivo()) {
            return;
        }
        Paleta.CoresLutador cores = Paleta.de(projetil.getDono().getPersonagem());
        int cx = PainelJogo.paraPixel(projetil.getCentroX());
        int cy = PainelJogo.paraPixel(projetil.getCentroY());
        int direcao = projetil.getDirecao();
        int quadro = projetil.getContadorAnimacao();

        // Rastro em blocos
        for (int i = 3; i >= 1; i--) {
            int tamanho = 6 - i;
            int tx = cx - direcao * (i * 4 + 2);
            int ty = cy + ((quadro / 3 + i) % 2 == 0 ? 0 : (i % 2 == 0 ? 1 : -1));
            g.setColor(i == 1 ? cores.energiaNucleo : Paleta.comAlfa(cores.energiaBrilho, 230 - i * 50));
            g.fillRect(tx - tamanho / 2, ty - tamanho / 2, tamanho, tamanho);
        }

        // Aura (2 quadros)
        int raio = (quadro / 3) % 2 == 0 ? 6 : 7;
        g.setColor(Paleta.comAlfa(cores.energiaBrilho, 190));
        g.fillOval(cx - raio, cy - raio, raio * 2 + 1, raio * 2 + 1);

        // Corpo e miolo
        g.setColor(cores.energiaNucleo);
        g.fillOval(cx - 4, cy - 4, 9, 9);
        g.setColor(Paleta.BRANCO);
        g.fillOval(cx - 2, cy - 2, 5, 5);

        // Centelhas orbitando
        double angulo = quadro * 0.45;
        for (int k = 0; k < 2; k++) {
            int sx = cx + (int) Math.round(Math.cos(angulo + k * Math.PI) * 8);
            int sy = cy + (int) Math.round(Math.sin(angulo + k * Math.PI) * 8);
            g.fillRect(sx, sy, 1, 1);
        }
    }
}
