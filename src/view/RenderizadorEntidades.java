package view;

import java.awt.Graphics2D;
import java.util.List;
import model.Entidade;
import model.Lutador;
import model.Projetil;
import model.VisitanteEntidade;

/**
 * Desenha as entidades do Model usando o padrão Visitor:
 * cada entidade chama {@code aceitar(this)} e cai no método {@code visitar}
 * do seu tipo, sem instanceof e sem o Model conhecer a View.
 */
final class RenderizadorEntidades implements VisitanteEntidade {

    private final RenderizadorLutador renderizadorLutador = new RenderizadorLutador();
    private final RenderizadorProjetil renderizadorProjetil = new RenderizadorProjetil();
    private Graphics2D graficos;

    void desenhar(Graphics2D g, List<Entidade> entidades) {
        graficos = g;
        for (Entidade entidade : entidades) {
            entidade.aceitar(this); // polimorfismo (double dispatch)
        }
        graficos = null;
    }

    @Override
    public void visitar(Lutador lutador) {
        renderizadorLutador.desenhar(graficos, lutador);
    }

    @Override
    public void visitar(Projetil projetil) {
        renderizadorProjetil.desenhar(graficos, projetil);
    }
}
