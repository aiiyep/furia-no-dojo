package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import model.EstadoLutador;
import model.FaseGolpe;
import model.Lutador;

/**
 * Desenha um lutador como sprite em pixel art, gerado proceduralmente:
 * um esqueleto articulado (ângulos de ombro, cotovelo, quadril e joelho)
 * rasterizado em baixa resolução, sem antialiasing e com contorno escuro.
 */
final class RenderizadorLutador {

    // Proporções do corpo em pixels de baixa resolução
    private static final double TRONCO = 17;
    private static final double BRACO_SUPERIOR = 10;
    private static final double BRACO_INFERIOR = 9;
    private static final double COXA = 13;
    private static final double CANELA = 13;
    private static final double PESCOCO = 7;
    private static final int RAIO_CABECA = 5;

    private final Stroke[] tracos = new Stroke[16];

    /** Ângulos (graus) de cada articulação. 0° = para baixo, 90° = frente, 180° = cima. */
    private static final class Pose {
        private double quadrilY = -25;
        private double inclinacao = 5;
        private double bracoFrenteSup = 50;
        private double bracoFrenteInf = 150;
        private double bracoTrasSup = 25;
        private double bracoTrasInf = 130;
        private double pernaFrenteSup = 22;
        private double pernaFrenteInf = -2;
        private double pernaTrasSup = -22;
        private double pernaTrasInf = -5;

        private void bracos(double fs, double fi, double ts, double ti) {
            bracoFrenteSup = fs;
            bracoFrenteInf = fi;
            bracoTrasSup = ts;
            bracoTrasInf = ti;
        }

        private void pernas(double fs, double fi, double ts, double ti) {
            pernaFrenteSup = fs;
            pernaFrenteInf = fi;
            pernaTrasSup = ts;
            pernaTrasInf = ti;
        }
    }

    /** Pontos calculados do esqueleto. */
    private static final class Esqueleto {
        private Point2D quadril;
        private Point2D ombro;
        private Point2D ombroTras;
        private Point2D cabeca;
        private Point2D cotoveloFrente;
        private Point2D maoFrente;
        private Point2D cotoveloTras;
        private Point2D maoTras;
        private Point2D joelhoFrente;
        private Point2D tornozeloFrente;
        private Point2D peFrente;
        private Point2D joelhoTras;
        private Point2D tornozeloTras;
        private Point2D peTras;
        private double normalX;
        private double normalY;
    }

    RenderizadorLutador() {
        for (int i = 1; i < tracos.length; i++) {
            tracos[i] = new BasicStroke(i, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }
    }

    void desenhar(Graphics2D g, Lutador lutador) {
        Paleta.CoresLutador cores = Paleta.de(lutador.getPersonagem());
        int cx = PainelJogo.paraPixel(lutador.getCentroX());
        int pes = PainelJogo.paraPixel(lutador.getY() + lutador.getAltura());

        desenharSombra(g, lutador, cx, pes);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(cx, pes);
        if (!lutador.isOlhandoDireita()) {
            g2.scale(-1, 1);
        }
        if (lutador.getEstado() == EstadoLutador.DERROTADO) {
            double progresso = Math.min(1.0, lutador.getFramesDerrota() / 18.0);
            g2.translate(0, -Math.round(3 * progresso));
            g2.rotate(-Math.PI / 2 * progresso);
        } else if (lutador.getEstado() == EstadoLutador.ATINGIDO) {
            g2.translate((lutador.getContadorAnimacao() % 4 < 2) ? -1 : 1, 0);
        }

        Esqueleto e = montarEsqueleto(calcularPose(lutador));
        desenharCorpo(g2, lutador, e, cores);
        g2.dispose();
    }

    private void desenharSombra(Graphics2D g, Lutador lutador, int cx, int pes) {
        int chao = PainelJogo.paraPixel(model.Arena.CHAO_Y);
        int alturaNoAr = Math.max(0, chao - pes);
        int largura = Math.max(8, 26 - alturaNoAr / 3);
        int deslocamento = 0;
        if (lutador.getEstado() == EstadoLutador.DERROTADO) {
            largura = 50;
            deslocamento = lutador.isOlhandoDireita() ? -20 : 20;
        }
        g.setColor(new Color(58, 28, 22));
        g.fillOval(cx + deslocamento - largura / 2, chao - 2, largura, 4);
    }

    // ------------------------------------------------------------------
    // Poses
    // ------------------------------------------------------------------

    private Pose calcularPose(Lutador l) {
        Pose p = new Pose();
        int anim = l.getContadorAnimacao();
        double respiracao = ((anim / 20) % 2 == 0) ? 0 : 1; // 2 quadros, como sprite clássico
        FaseGolpe fase = l.getFaseGolpe();

        switch (l.getEstado()) {
            case PARADO:
                p.quadrilY = -25 + respiracao;
                p.bracos(50, 150 + respiracao * 6, 25, 130);
                break;

            case ANDANDO: {
                int quadro = (anim / 7) % 4; // ciclo de caminhada de 4 quadros
                double fase4 = (quadro == 0) ? 1 : (quadro == 2 ? -1 : 0);
                p.quadrilY = -26 + Math.abs(fase4) * 2;
                p.pernas(10 + 22 * fase4, -2 + 10 * fase4, -10 - 22 * fase4, -22 - 10 * fase4);
                p.bracos(50 - fase4 * 6, 150, 25 + fase4 * 6, 130);
                p.inclinacao = 8;
                break;
            }

            case AGACHADO:
                p.quadrilY = -15;
                p.inclinacao = 18;
                p.pernas(78, -12, -75, -5);
                p.bracos(62, 155, 38, 140);
                break;

            case PULANDO:
                p.quadrilY = -27;
                p.inclinacao = 10;
                p.pernas(80, -20, 25, -45);
                p.bracos(70, 160, 40, 150);
                break;

            case DEFENDENDO:
                p.quadrilY = -24;
                p.inclinacao = -6;
                p.pernas(26, -4, -26, -6);
                p.bracos(78, 172, 58, 168);
                break;

            case SOCANDO:
                p.inclinacao = 10;
                if (fase == FaseGolpe.PREPARACAO) {
                    p.bracos(40, 115, 10, 110);
                } else if (fase == FaseGolpe.ATIVO) {
                    p.inclinacao = 14;
                    p.bracos(90, 90, -25, 80);
                } else {
                    p.bracos(70, 125, 0, 100);
                }
                p.pernas(30, 0, -26, -8);
                break;

            case CHUTANDO:
                p.quadrilY = -26;
                if (fase == FaseGolpe.PREPARACAO) {
                    p.inclinacao = -5;
                    p.pernas(75, -10, -8, -4);
                } else if (fase == FaseGolpe.ATIVO) {
                    p.inclinacao = -18;
                    p.pernas(96, 94, -12, -6);
                } else {
                    p.inclinacao = -8;
                    p.pernas(55, 15, -10, -5);
                }
                p.bracos(60, 140, -30, 40);
                break;

            case ESPECIAL:
                if (fase == FaseGolpe.PREPARACAO) {
                    p.inclinacao = -6;
                    p.bracos(-40, 40, -35, 50);
                } else {
                    p.inclinacao = 16;
                    p.bracos(90, 90, 84, 88);
                }
                p.pernas(34, 2, -30, -10);
                break;

            case ATINGIDO:
                p.inclinacao = -22;
                p.bracos(140, 110, -40, -10);
                p.pernas(15, 0, -30, -12);
                break;

            case VITORIOSO:
                p.quadrilY = -25 + respiracao;
                p.inclinacao = 0;
                p.bracos(165, 178, 30, 120);
                p.pernas(16, 0, -16, -2);
                break;

            case DERROTADO:
                p.quadrilY = -26;
                p.inclinacao = 0;
                p.bracos(160, 170, 150, 165);
                p.pernas(6, 0, -6, 0);
                break;

            default:
                break;
        }
        return p;
    }

    private Esqueleto montarEsqueleto(Pose p) {
        Esqueleto e = new Esqueleto();
        double inc = Math.toRadians(p.inclinacao);
        e.normalX = Math.cos(inc);
        e.normalY = Math.sin(inc);
        e.quadril = new Point2D.Double(0, p.quadrilY);
        e.ombro = new Point2D.Double(Math.sin(inc) * TRONCO, p.quadrilY - Math.cos(inc) * TRONCO);
        e.ombroTras = new Point2D.Double(e.ombro.getX() - 2, e.ombro.getY() + 1);
        e.cabeca = new Point2D.Double(e.ombro.getX() + Math.sin(inc) * PESCOCO, e.ombro.getY() - Math.cos(inc) * PESCOCO);

        e.cotoveloFrente = ponto(e.ombro, p.bracoFrenteSup, BRACO_SUPERIOR);
        e.maoFrente = ponto(e.cotoveloFrente, p.bracoFrenteInf, BRACO_INFERIOR);
        e.cotoveloTras = ponto(e.ombroTras, p.bracoTrasSup, BRACO_SUPERIOR);
        e.maoTras = ponto(e.cotoveloTras, p.bracoTrasInf, BRACO_INFERIOR);

        e.joelhoFrente = ponto(e.quadril, p.pernaFrenteSup, COXA);
        e.tornozeloFrente = ponto(e.joelhoFrente, p.pernaFrenteInf, CANELA);
        e.peFrente = calcularPe(e.tornozeloFrente, p.pernaFrenteInf);
        e.joelhoTras = ponto(e.quadril, p.pernaTrasSup, COXA);
        e.tornozeloTras = ponto(e.joelhoTras, p.pernaTrasInf, CANELA);
        e.peTras = calcularPe(e.tornozeloTras, p.pernaTrasInf);
        return e;
    }

    private static Point2D calcularPe(Point2D tornozelo, double anguloCanela) {
        if (anguloCanela > 60) {
            return ponto(tornozelo, anguloCanela, 4); // chute alto: pé alinhado à canela
        }
        return new Point2D.Double(tornozelo.getX() + 4, tornozelo.getY());
    }

    // ------------------------------------------------------------------
    // Desenho
    // ------------------------------------------------------------------

    private void desenharCorpo(Graphics2D g, Lutador l, Esqueleto e, Paleta.CoresLutador c) {
        boolean flash = l.getFramesPiscarDano() > 0 && l.getFramesPiscarDano() % 4 < 2;
        Color kimono = flash ? Paleta.BRANCO : c.kimono;
        Color kimonoSombra = flash ? Paleta.BRANCO : c.kimonoSombra;
        Color kimonoLuz = flash ? Paleta.BRANCO : c.kimonoLuz;
        Color pele = flash ? Paleta.BRANCO : c.pele;
        Color peleSombra = flash ? Paleta.BRANCO : c.peleSombra;
        Color faixa = flash ? Paleta.BRANCO : c.faixa;
        Color faixaSombra = flash ? Paleta.BRANCO : c.faixaSombra;
        Color luvas = flash ? Paleta.BRANCO : c.luvas;
        Color luvasSombra = flash ? Paleta.BRANCO : c.luvasSombra;
        Color cabelo = flash ? Paleta.BRANCO : c.cabelo;

        // 1) Silhueta: contorno de 1 pixel em volta de todo o corpo
        g.setColor(Paleta.CONTORNO);
        contornoPerna(g, e.quadril, e.joelhoTras, e.tornozeloTras, e.peTras);
        contornoBraco(g, e.ombroTras, e.cotoveloTras, e.maoTras);
        linha(g, e.quadril, e.ombro, 11);
        linha(g, e.ombro, e.cabeca, 6);
        g.fillOval(px(e.cabeca.getX()) - RAIO_CABECA - 1, px(e.cabeca.getY()) - RAIO_CABECA - 1,
                RAIO_CABECA * 2 + 3, RAIO_CABECA * 2 + 3);
        contornoPerna(g, e.quadril, e.joelhoFrente, e.tornozeloFrente, e.peFrente);
        contornoBraco(g, e.ombro, e.cotoveloFrente, e.maoFrente);

        // 2) Membros de trás (tons mais escuros)
        preencherPerna(g, e.quadril, e.joelhoTras, e.tornozeloTras, e.peTras, kimonoSombra, peleSombra);
        preencherBraco(g, e.ombroTras, e.cotoveloTras, e.maoTras, kimonoSombra, peleSombra, luvasSombra);

        // 3) Tronco com luz na frente, lapela e faixa
        g.setColor(kimono);
        linha(g, e.quadril, e.ombro, 9);
        g.setColor(kimonoLuz);
        linha(g, desloca(e.quadril, e.normalX * 3, e.normalY * 3 - 2),
                desloca(e.ombro, e.normalX * 3, e.normalY * 3 + 1), 2);
        g.setColor(pele);
        linha(g, desloca(e.ombro, e.normalX, e.normalY), meio(e.ombro, e.quadril, 0.3), 2);
        g.setColor(kimonoSombra);
        linha(g, desloca(e.ombro, e.normalX * 3, e.normalY * 3), meio(e.ombro, e.quadril, 0.55), 1);
        g.setColor(faixa);
        linha(g, desloca(e.quadril, -e.normalX * 5, -e.normalY * 5 - 1),
                desloca(e.quadril, e.normalX * 5, e.normalY * 5 - 1), 3);
        g.setColor(faixaSombra);
        int balanco = ((l.getContadorAnimacao() / 8) % 2 == 0) ? 0 : 1;
        linha(g, desloca(e.quadril, e.normalX * 2, 0), desloca(e.quadril, e.normalX * 2 + balanco, 5), 2);

        // 4) Perna da frente (com contorno próprio, separando-a do tronco)
        g.setColor(Paleta.CONTORNO);
        contornoPerna(g, e.quadril, e.joelhoFrente, e.tornozeloFrente, e.peFrente);
        preencherPerna(g, e.quadril, e.joelhoFrente, e.tornozeloFrente, e.peFrente, kimono, pele);

        // 5) Cabeça
        g.setColor(peleSombra);
        linha(g, e.ombro, e.cabeca, 4);
        desenharCabeca(g, l, e.cabeca, pele, cabelo, faixa, faixaSombra);

        // 6) Braço da frente
        g.setColor(Paleta.CONTORNO);
        contornoBraco(g, e.ombro, e.cotoveloFrente, e.maoFrente);
        preencherBraco(g, e.ombro, e.cotoveloFrente, e.maoFrente, kimono, pele, luvas);

        // 7) Energia do especial entre as mãos
        if (l.getEstado() == EstadoLutador.ESPECIAL) {
            desenharEnergia(g, l, e, c);
        }
    }

    private void desenharCabeca(Graphics2D g, Lutador l, Point2D centro, Color pele, Color cabelo,
                                Color faixa, Color faixaSombra) {
        int x = px(centro.getX());
        int y = px(centro.getY());
        int r = RAIO_CABECA;

        // Fitas da bandana (2 quadros de animação)
        boolean quadroA = (l.getContadorAnimacao() / 8) % 2 == 0;
        g.setColor(faixaSombra);
        g.setStroke(tracos[1]);
        g.drawLine(x - r, y - 3, x - r - 5, quadroA ? y - 4 : y - 2);
        g.drawLine(x - r, y - 2, x - r - 4, quadroA ? y : y + 1);

        g.setColor(pele);
        g.fillOval(x - r, y - r, r * 2 + 1, r * 2 + 1);

        g.setColor(cabelo);
        g.fillRect(x - r, y - r, r * 2 + 1, 3);
        g.fillRect(x - r, y - r + 2, 3, 5);

        g.setColor(faixa);
        g.fillRect(x - r, y - 3, r * 2 + 1, 2);

        g.setColor(Paleta.CONTORNO);
        EstadoLutador estado = l.getEstado();
        if (estado == EstadoLutador.ATINGIDO || estado == EstadoLutador.DERROTADO) {
            g.fillRect(x + 2, y - 1, 1, 1);
            g.fillRect(x + 4, y - 1, 1, 1);
            g.fillRect(x + 3, y, 1, 1);
        } else {
            g.fillRect(x + 2, y - 1, 2, 2);
        }
        g.setColor(Paleta.misturar(pele, Paleta.CONTORNO, 0.5));
        g.fillRect(x + 3, y + 3, 2, 1);
    }

    private void desenharEnergia(Graphics2D g, Lutador l, Esqueleto e, Paleta.CoresLutador c) {
        int frames = l.getFramesAtaque();
        int raio;
        if (frames < Lutador.ESPECIAL_DISPARO) {
            raio = 2 + frames / 3;
        } else if (frames < Lutador.ESPECIAL_DISPARO + 6) {
            raio = 7 - (frames - Lutador.ESPECIAL_DISPARO);
        } else {
            return;
        }
        int mx = px((e.maoFrente.getX() + e.maoTras.getX()) / 2);
        int my = px((e.maoFrente.getY() + e.maoTras.getY()) / 2);
        boolean pisca = (frames / 2) % 2 == 0;
        g.setColor(pisca ? c.energiaBrilho : c.energiaNucleo);
        g.fillOval(mx - raio, my - raio, raio * 2 + 1, raio * 2 + 1);
        g.setColor(Paleta.BRANCO);
        int miolo = Math.max(1, raio / 2);
        g.fillOval(mx - miolo, my - miolo, miolo * 2 + 1, miolo * 2 + 1);
    }

    private void contornoBraco(Graphics2D g, Point2D ombro, Point2D cotovelo, Point2D mao) {
        linha(g, ombro, cotovelo, 6);
        linha(g, cotovelo, mao, 5);
        g.fillRect(px(mao.getX()) - 3, px(mao.getY()) - 3, 7, 7);
    }

    private void preencherBraco(Graphics2D g, Point2D ombro, Point2D cotovelo, Point2D mao,
                                Color manga, Color pele, Color luva) {
        g.setColor(manga);
        linha(g, ombro, cotovelo, 4);
        g.setColor(pele);
        linha(g, cotovelo, mao, 3);
        int mx = px(mao.getX());
        int my = px(mao.getY());
        g.setColor(luva);
        g.fillRect(mx - 2, my - 2, 5, 5);
        g.setColor(Paleta.comAlfa(Paleta.BRANCO, 170));
        g.fillRect(mx - 1, my - 1, 1, 1);
    }

    private void contornoPerna(Graphics2D g, Point2D quadril, Point2D joelho, Point2D tornozelo, Point2D pe) {
        linha(g, quadril, joelho, 7);
        linha(g, joelho, tornozelo, 6);
        linha(g, tornozelo, pe, 5);
    }

    private void preencherPerna(Graphics2D g, Point2D quadril, Point2D joelho, Point2D tornozelo, Point2D pe,
                                Color calca, Color pele) {
        g.setColor(calca);
        linha(g, quadril, joelho, 5);
        linha(g, joelho, tornozelo, 4);
        g.setColor(pele);
        linha(g, tornozelo, pe, 3);
    }

    private void linha(Graphics2D g, Point2D a, Point2D b, int largura) {
        g.setStroke(tracos[largura]);
        g.draw(new Line2D.Double(px(a.getX()), px(a.getY()), px(b.getX()), px(b.getY())));
    }

    private static Point2D ponto(Point2D origem, double anguloGraus, double comprimento) {
        double rad = Math.toRadians(anguloGraus);
        return new Point2D.Double(origem.getX() + Math.sin(rad) * comprimento,
                origem.getY() + Math.cos(rad) * comprimento);
    }

    private static Point2D desloca(Point2D p, double dx, double dy) {
        return new Point2D.Double(p.getX() + dx, p.getY() + dy);
    }

    private static Point2D meio(Point2D a, Point2D b, double t) {
        return new Point2D.Double(a.getX() + (b.getX() - a.getX()) * t, a.getY() + (b.getY() - a.getY()) * t);
    }

    /** Arredonda para a grade de pixels. */
    private static int px(double v) {
        return (int) Math.round(v);
    }
}
