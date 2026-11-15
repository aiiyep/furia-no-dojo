package model;

/**
 * Pontuação de um lado da luta (jogador 1 ou jogador 2/CPU).
 * Concentra as regras de pontos, combos e bônus.
 */
public class Pontuacao {

    public static final int PONTOS_SOCO = 100;
    public static final int PONTOS_CHUTE = 200;
    public static final int PONTOS_ESPECIAL = 300;
    public static final int PONTOS_DEFENDIDO = 20;
    public static final int PONTOS_ANULAR_PROJETIL = 50;
    public static final int PONTOS_POR_COMBO = 50;
    public static final int PONTOS_POR_VIDA = 10;
    public static final int PONTOS_POR_SEGUNDO = 20;
    public static final int BONUS_ROUND = 1000;
    public static final int BONUS_PERFEITO = 2000;
    public static final int BONUS_PARTIDA = 5000;

    /** Intervalo máximo (em frames) entre acertos para contar como combo. */
    private static final int JANELA_COMBO = 45;
    private static final int DURACAO_TEXTO_COMBO = 70;

    private int pontosGolpes;
    private int bonusVida;
    private int bonusTempo;
    private int bonusVitoria;
    private int comboAtual;
    private int maiorCombo;
    private int framesDesdeUltimoAcerto = 999;
    private int comboExibido;
    private int framesTextoCombo;

    /** Zera tudo (nova partida). */
    public void zerar() {
        pontosGolpes = 0;
        bonusVida = 0;
        bonusTempo = 0;
        bonusVitoria = 0;
        maiorCombo = 0;
        novoRound();
    }

    /** Reinicia apenas o estado de combo (novo round). */
    public void novoRound() {
        comboAtual = 0;
        framesDesdeUltimoAcerto = 999;
        framesTextoCombo = 0;
    }

    /** Avança os contadores de tempo (uma vez por frame). */
    public void atualizar() {
        framesDesdeUltimoAcerto++;
        if (framesTextoCombo > 0) {
            framesTextoCombo--;
        }
    }

    /**
     * Registra um golpe que acertou limpo.
     *
     * @return pontos ganhos (base + bônus de combo)
     */
    public int registrarAcerto(EstadoLutador tipoGolpe) {
        int base;
        if (tipoGolpe == EstadoLutador.SOCANDO) {
            base = PONTOS_SOCO;
        } else if (tipoGolpe == EstadoLutador.CHUTANDO) {
            base = PONTOS_CHUTE;
        } else {
            base = PONTOS_ESPECIAL;
        }

        if (comboAtual > 0 && framesDesdeUltimoAcerto <= JANELA_COMBO) {
            comboAtual++;
        } else {
            comboAtual = 1;
        }
        framesDesdeUltimoAcerto = 0;

        int bonusCombo = comboAtual > 1 ? PONTOS_POR_COMBO * (comboAtual - 1) : 0;
        int ganho = base + bonusCombo;
        pontosGolpes += ganho;
        maiorCombo = Math.max(maiorCombo, comboAtual);
        if (comboAtual > 1) {
            comboExibido = comboAtual;
            framesTextoCombo = DURACAO_TEXTO_COMBO;
        }
        return ganho;
    }

    /** Registra um golpe que o oponente defendeu. */
    public int registrarDefendido() {
        comboAtual = 0;
        pontosGolpes += PONTOS_DEFENDIDO;
        return PONTOS_DEFENDIDO;
    }

    /** Registra a anulação de um projétil inimigo. */
    public int registrarAnulacao() {
        pontosGolpes += PONTOS_ANULAR_PROJETIL;
        return PONTOS_ANULAR_PROJETIL;
    }

    /** O lutador deste lado foi atingido: o combo dele acaba. */
    public void interromperCombo() {
        comboAtual = 0;
    }

    /**
     * Aplica os bônus de vitória de round.
     *
     * @return descrição dos bônus para exibição
     */
    public String registrarVitoriaRound(int vidaRestante, int tempoRestante) {
        int bv = vidaRestante * PONTOS_POR_VIDA;
        int bt = tempoRestante * PONTOS_POR_SEGUNDO;
        int br = BONUS_ROUND + (vidaRestante == Lutador.VIDA_MAXIMA ? BONUS_PERFEITO : 0);
        bonusVida += bv;
        bonusTempo += bt;
        bonusVitoria += br;
        return String.format("VIDA +%d  TEMPO +%d  ROUND +%d", bv, bt, br);
    }

    public void registrarVitoriaPartida() {
        bonusVitoria += BONUS_PARTIDA;
    }

    public int getTotal() {
        return pontosGolpes + bonusVida + bonusTempo + bonusVitoria;
    }

    public int getPontosGolpes() {
        return pontosGolpes;
    }

    public int getBonusVida() {
        return bonusVida;
    }

    public int getBonusTempo() {
        return bonusTempo;
    }

    public int getBonusVitoria() {
        return bonusVitoria;
    }

    public int getMaiorCombo() {
        return maiorCombo;
    }

    public int getComboExibido() {
        return comboExibido;
    }

    public int getFramesTextoCombo() {
        return framesTextoCombo;
    }
}
