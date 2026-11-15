package model;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Lutador controlado pela CPU (usado apenas no modo 1 jogador).
 *
 * HERANÇA: reaproveita toda a física e os golpes de {@link Lutador}.
 * POLIMORFISMO: sobrescreve atualizar() para, antes da física, "pensar" e
 * emitir comandos (aproximar, recuar/defender, agachar, pular, socar,
 * chutar ou lançar o especial) com base na distância para o adversário.
 */
public class LutadorIA extends Lutador {

    /** Ações contínuas que a IA mantém por alguns frames. */
    private enum Acao {
        PARAR, APROXIMAR, RECUAR, AGACHAR
    }

    private static final double DISTANCIA_LONGA = 320;
    private static final double DISTANCIA_MEDIA = 140;
    private static final double DISTANCIA_CURTA = 85;
    private static final double DISTANCIA_MINIMA = 65;

    private final Random aleatorio = new Random();
    private List<Projetil> projeteisEmCena = Collections.emptyList();
    private Projetil ultimoProjetilAnalisado;
    private Acao acaoAtual = Acao.PARAR;
    private int framesAcao;
    private int contadorDecisao;
    private int nivel = 1;

    public LutadorIA(Personagem personagem, double xInicial, boolean olhandoDireita) {
        super(personagem, xInicial, olhandoDireita);
    }

    @Override
    public void reiniciar() {
        super.reiniciar();
        acaoAtual = Acao.PARAR;
        framesAcao = 0;
        contadorDecisao = 30;
        ultimoProjetilAnalisado = null;
    }

    @Override
    public void atualizar() {
        if (isControleAtivo() && getOponente() != null) {
            pensar();
        } else {
            limparComandos();
        }
        super.atualizar(); // física, golpes e animação herdados
    }

    // ------------------------------------------------------------------
    // Tomada de decisão
    // ------------------------------------------------------------------

    private void pensar() {
        if (framesAcao > 0) {
            framesAcao--;
        }
        boolean reagiu = podeAgir() && reagirAProjetil();
        if (!reagiu) {
            contadorDecisao--;
            if (contadorDecisao <= 0 && podeAgir()) {
                decidir();
                contadorDecisao = calcularTempoReacao();
            }
        }
        aplicarAcao();
    }

    /** Tenta esquivar (pular, agachar) ou defender de um projétil que se aproxima. */
    private boolean reagirAProjetil() {
        for (Projetil p : projeteisEmCena) {
            if (!p.isAtivo() || p.getDono() == this || p == ultimoProjetilAnalisado) {
                continue;
            }
            double dx = getCentroX() - p.getCentroX();
            boolean vindoEmMinhaDirecao = (dx > 0 && p.getDirecao() > 0) || (dx < 0 && p.getDirecao() < 0);
            if (vindoEmMinhaDirecao && Math.abs(dx) < 240) {
                ultimoProjetilAnalisado = p; // decide apenas uma vez por projétil
                if (aleatorio.nextDouble() < 0.40 + 0.12 * nivel) {
                    double r = aleatorio.nextDouble();
                    if (r < 0.4) {
                        definirAcao(Acao.APROXIMAR, 20);
                        pular();
                    } else if (r < 0.7) {
                        definirAcao(Acao.AGACHAR, 35);
                    } else {
                        definirAcao(Acao.RECUAR, 35);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void decidir() {
        Lutador alvo = getOponente();
        double distancia = Math.abs(alvo.getCentroX() - getCentroX());

        // Defesa reativa quando o adversário ataca de perto
        if (alvo.isAtacando() && distancia < 140 && aleatorio.nextDouble() < 0.25 + 0.12 * nivel) {
            definirAcao(Acao.RECUAR, 18);
            return;
        }

        double r = aleatorio.nextDouble();
        if (distancia > DISTANCIA_LONGA) {
            if (isEspecialPronto() && r < 0.45) {
                atacar(lancarEspecial());
            } else if (r < 0.85) {
                definirAcao(Acao.APROXIMAR, 25);
            } else {
                definirAcao(Acao.PARAR, 15);
            }
        } else if (distancia > DISTANCIA_MEDIA) {
            if (isEspecialPronto() && r < 0.20) {
                atacar(lancarEspecial());
            } else if (r < 0.32) {
                definirAcao(Acao.APROXIMAR, 14);
                pular();
            } else if (r < 0.85) {
                definirAcao(Acao.APROXIMAR, 20);
            } else {
                definirAcao(Acao.RECUAR, 15);
            }
        } else if (distancia > DISTANCIA_CURTA) {
            if (r < 0.40) {
                atacar(chutar());
            } else if (r < 0.65) {
                definirAcao(Acao.APROXIMAR, 10);
            } else if (r < 0.80) {
                definirAcao(Acao.RECUAR, 15);
            } else if (r < 0.90) {
                definirAcao(Acao.AGACHAR, 20);
            } else {
                pular();
            }
        } else {
            if (r < 0.50) {
                atacar(socar());
            } else if (r < 0.68) {
                atacar(chutar());
            } else if (r < 0.84) {
                definirAcao(Acao.RECUAR, 20);
            } else {
                definirAcao(Acao.AGACHAR, 15);
            }
        }
    }

    private void atacar(boolean iniciou) {
        if (iniciou) {
            definirAcao(Acao.PARAR, 10);
        }
    }

    private void definirAcao(Acao acao, int frames) {
        acaoAtual = acao;
        framesAcao = frames;
    }

    /** Converte a ação atual em comandos de movimento do lutador. */
    private void aplicarAcao() {
        if (framesAcao <= 0) {
            acaoAtual = Acao.PARAR;
        }
        double dx = getOponente().getCentroX() - getCentroX();
        if (acaoAtual == Acao.APROXIMAR && Math.abs(dx) < DISTANCIA_MINIMA) {
            acaoAtual = Acao.PARAR;
        }
        boolean oponenteADireita = dx > 0;
        boolean esquerda = false;
        boolean direita = false;
        boolean agachar = false;
        switch (acaoAtual) {
            case APROXIMAR:
                direita = oponenteADireita;
                esquerda = !oponenteADireita;
                break;
            case RECUAR: // andar para trás = defender
                direita = !oponenteADireita;
                esquerda = oponenteADireita;
                break;
            case AGACHAR:
                agachar = true;
                break;
            default:
                break;
        }
        setMovendoEsquerda(esquerda);
        setMovendoDireita(direita);
        setAgachando(agachar);
    }

    /** Quanto maior o nível, mais rápido a IA reage. */
    private int calcularTempoReacao() {
        int base = Math.max(6, 22 - nivel * 4);
        return base + aleatorio.nextInt(10);
    }

    // ------------------------------------------------------------------
    // Getters e setters
    // ------------------------------------------------------------------

    /** Recebe uma visão somente-leitura dos projéteis para poder esquivar. */
    public void setProjeteisEmCena(List<Projetil> projeteisEmCena) {
        this.projeteisEmCena = projeteisEmCena;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = Math.max(1, Math.min(4, nivel));
    }
}
