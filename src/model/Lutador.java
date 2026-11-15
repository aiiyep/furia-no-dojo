package model;

import java.awt.Rectangle;

/**
 * Lutador (Model). Encapsula vida, estado, direção, física de pulo/gravidade,
 * cooldowns, geração de hitbox ofensiva e aplicação de dano/knockback.
 *
 * O lutador não lê o teclado nem se desenha: recebe "intenções" (andar,
 * pular, socar...) por métodos públicos, chamados pelo Controller (teclado)
 * ou pela própria IA, e expõe seu estado para a View desenhar.
 */
public class Lutador extends Entidade {

    public static final int VIDA_MAXIMA = 100;
    public static final int LARGURA_LUTADOR = 60;
    public static final int ALTURA_LUTADOR = 150;

    // Física
    private static final double GRAVIDADE = 0.8;
    private static final double FORCA_PULO = -15.5;
    private static final double VELOCIDADE_ANDAR = 4.0;
    private static final double VELOCIDADE_RECUO = 2.6;

    // Tempos (em frames, ~60 por segundo)
    private static final int COOLDOWN_ESPECIAL = 150;
    private static final int DURACAO_ATORDOAMENTO = 18;
    private static final int DURACAO_BLOQUEIO = 10;

    // Soco rápido
    private static final int SOCO_DURACAO = 16;
    private static final int SOCO_INICIO_ATIVO = 4;
    private static final int SOCO_FIM_ATIVO = 8;
    private static final int SOCO_DANO = 6;
    private static final int SOCO_ALCANCE = 62;

    // Chute forte
    private static final int CHUTE_DURACAO = 28;
    private static final int CHUTE_INICIO_ATIVO = 10;
    private static final int CHUTE_FIM_ATIVO = 16;
    private static final int CHUTE_DANO = 12;
    private static final int CHUTE_ALCANCE = 88;

    // Especial (projétil)
    public static final int ESPECIAL_DISPARO = 13;
    private static final int ESPECIAL_DURACAO = 34;
    private static final int ESPECIAL_DANO = 15;

    private final Personagem personagem;
    private final double xInicial;
    private final boolean olhandoDireitaInicial;

    // Estado de combate
    private int vida;
    private EstadoLutador estado;
    private boolean olhandoDireita;
    private boolean noChao;
    private boolean controleAtivo;
    private Lutador oponente;

    // Intenções de movimento
    private boolean movendoEsquerda;
    private boolean movendoDireita;
    private boolean agachando;
    private boolean pedidoPulo;

    // Controle de golpes e timers
    private int framesAtaque;
    private boolean golpeAcertou;
    private int cooldownEspecial;
    private int framesAtordoado;
    private int framesBloqueio;
    private int framesDerrota;
    private int framesPiscarDano;
    private int contadorAnimacao;
    private Projetil projetilPendente;

    public Lutador(Personagem personagem, double xInicial, boolean olhandoDireita) {
        super(xInicial, Arena.CHAO_Y - ALTURA_LUTADOR, LARGURA_LUTADOR, ALTURA_LUTADOR);
        this.personagem = personagem;
        this.xInicial = xInicial;
        this.olhandoDireitaInicial = olhandoDireita;
        restaurarEstadoInicial();
    }

    /** Restaura o lutador para o início de um round. Subclasses podem estender. */
    public void reiniciar() {
        restaurarEstadoInicial();
    }

    private void restaurarEstadoInicial() {
        setX(xInicial);
        setY(Arena.CHAO_Y - getAltura());
        setVelocidadeX(0);
        setVelocidadeY(0);
        setAtivo(true);
        vida = VIDA_MAXIMA;
        estado = EstadoLutador.PARADO;
        olhandoDireita = olhandoDireitaInicial;
        noChao = true;
        controleAtivo = false;
        limparComandos();
        framesAtaque = 0;
        golpeAcertou = false;
        cooldownEspecial = 0;
        framesAtordoado = 0;
        framesBloqueio = 0;
        framesDerrota = 0;
        framesPiscarDano = 0;
        projetilPendente = null;
    }

    // ==================================================================
    // Comandos (intenções)
    // ==================================================================

    public void setMovendoEsquerda(boolean movendoEsquerda) {
        this.movendoEsquerda = movendoEsquerda;
    }

    public void setMovendoDireita(boolean movendoDireita) {
        this.movendoDireita = movendoDireita;
    }

    public void setAgachando(boolean agachando) {
        this.agachando = agachando;
    }

    public void pular() {
        if (controleAtivo) {
            pedidoPulo = true;
        }
    }

    public boolean socar() {
        return iniciarAtaque(EstadoLutador.SOCANDO);
    }

    public boolean chutar() {
        return iniciarAtaque(EstadoLutador.CHUTANDO);
    }

    public boolean lancarEspecial() {
        if (cooldownEspecial > 0 || !noChao) {
            return false;
        }
        if (iniciarAtaque(EstadoLutador.ESPECIAL)) {
            cooldownEspecial = COOLDOWN_ESPECIAL;
            return true;
        }
        return false;
    }

    /** Limpa todas as intenções de movimento. */
    public void limparComandos() {
        movendoEsquerda = false;
        movendoDireita = false;
        agachando = false;
        pedidoPulo = false;
    }

    private boolean iniciarAtaque(EstadoLutador tipo) {
        if (!podeAgir()) {
            return false;
        }
        estado = tipo;
        framesAtaque = 0;
        golpeAcertou = false; // nova execução: o golpe pode acertar novamente (uma vez)
        if (noChao) {
            setVelocidadeX(0);
        }
        return true;
    }

    /** @return true se o lutador está livre para iniciar uma nova ação. */
    public boolean podeAgir() {
        return controleAtivo
                && framesAtordoado == 0
                && framesBloqueio == 0
                && !estado.isAtaque()
                && estado != EstadoLutador.DERROTADO
                && estado != EstadoLutador.VITORIOSO;
    }

    // ==================================================================
    // Atualização (game loop)
    // ==================================================================

    @Override
    public void atualizar() {
        contadorAnimacao++;
        if (cooldownEspecial > 0) {
            cooldownEspecial--;
        }
        if (framesPiscarDano > 0) {
            framesPiscarDano--;
        }

        if (estado == EstadoLutador.DERROTADO) {
            framesDerrota++;
            aplicarAtrito();
            aplicarFisica();
            return;
        }
        if (estado == EstadoLutador.VITORIOSO) {
            setVelocidadeX(0);
            aplicarFisica();
            return;
        }

        // Os lutadores sempre se encaram quando não estão atacando
        if (!estado.isAtaque()) {
            virarParaOponente();
        }

        if (framesAtordoado > 0) {
            framesAtordoado--;
            estado = EstadoLutador.ATINGIDO;
            aplicarAtrito();
            if (framesAtordoado == 0) {
                estado = noChao ? EstadoLutador.PARADO : EstadoLutador.PULANDO;
            }
        } else if (framesBloqueio > 0) {
            framesBloqueio--;
            estado = EstadoLutador.DEFENDENDO;
            aplicarAtrito();
            if (framesBloqueio == 0) {
                estado = EstadoLutador.PARADO;
            }
        } else if (estado.isAtaque()) {
            processarAtaque();
        } else {
            processarMovimento();
        }

        pedidoPulo = false;
        aplicarFisica();
    }

    private void processarAtaque() {
        framesAtaque++;
        if (estado == EstadoLutador.ESPECIAL && framesAtaque == ESPECIAL_DISPARO) {
            criarProjetil();
        }
        if (noChao) {
            setVelocidadeX(0);
        }
        if (framesAtaque >= getDuracaoGolpe(estado)) {
            estado = noChao ? EstadoLutador.PARADO : EstadoLutador.PULANDO;
            framesAtaque = 0;
        }
    }

    private void processarMovimento() {
        if (!controleAtivo) {
            if (noChao) {
                setVelocidadeX(0);
                estado = EstadoLutador.PARADO;
            }
            return;
        }

        int direcao = 0;
        if (movendoEsquerda) {
            direcao--;
        }
        if (movendoDireita) {
            direcao++;
        }

        if (!noChao) {
            estado = EstadoLutador.PULANDO;
            return;
        }

        if (pedidoPulo) {
            setVelocidadeY(FORCA_PULO);
            setVelocidadeX(direcao * VELOCIDADE_ANDAR);
            noChao = false;
            estado = EstadoLutador.PULANDO;
            return;
        }

        if (agachando) {
            estado = EstadoLutador.AGACHADO;
            setVelocidadeX(0);
            return;
        }

        if (direcao == 0) {
            estado = EstadoLutador.PARADO;
            setVelocidadeX(0);
        } else if (isDirecaoParaTras(direcao)) {
            // Segurar para trás = recuar defendendo
            estado = EstadoLutador.DEFENDENDO;
            setVelocidadeX(direcao * VELOCIDADE_RECUO);
        } else {
            estado = EstadoLutador.ANDANDO;
            setVelocidadeX(direcao * VELOCIDADE_ANDAR);
        }
    }

    private boolean isDirecaoParaTras(int direcao) {
        return (direcao > 0 && !olhandoDireita) || (direcao < 0 && olhandoDireita);
    }

    private void virarParaOponente() {
        if (oponente == null) {
            return;
        }
        if (oponente.getCentroX() > getCentroX()) {
            olhandoDireita = true;
        } else if (oponente.getCentroX() < getCentroX()) {
            olhandoDireita = false;
        }
    }

    private void aplicarFisica() {
        setX(getX() + getVelocidadeX());
        setVelocidadeY(getVelocidadeY() + GRAVIDADE);
        setY(getY() + getVelocidadeY());

        double chao = Arena.CHAO_Y - getAltura();
        if (getY() >= chao) {
            setY(chao);
            setVelocidadeY(0);
            if (!noChao) {
                noChao = true;
                if (estado == EstadoLutador.PULANDO) {
                    estado = EstadoLutador.PARADO;
                }
            }
        } else {
            noChao = false;
        }
        limitarNaArena();
    }

    private void aplicarAtrito() {
        if (noChao) {
            setVelocidadeX(getVelocidadeX() * 0.82);
            if (Math.abs(getVelocidadeX()) < 0.1) {
                setVelocidadeX(0);
            }
        }
    }

    private void limitarNaArena() {
        double minimo = Arena.MARGEM;
        double maximo = Arena.LARGURA - Arena.MARGEM - getLargura();
        if (getX() < minimo) {
            setX(minimo);
        } else if (getX() > maximo) {
            setX(maximo);
        }
    }

    /** Desloca o lutador horizontalmente (usado para separar corpos), respeitando a arena. */
    public void empurrar(double deslocamento) {
        setX(getX() + deslocamento);
        limitarNaArena();
    }

    private void criarProjetil() {
        int direcao = olhandoDireita ? 1 : -1;
        double px = getCentroX() + direcao * 45 - Projetil.TAMANHO / 2.0;
        double py = getY() + getAltura() - 112 - Projetil.TAMANHO / 2.0;
        projetilPendente = new Projetil(this, px, py, direcao, ESPECIAL_DANO);
    }

    /** Entrega (uma única vez) o projétil criado neste frame, se houver. */
    public Projetil consumirProjetilPendente() {
        Projetil p = projetilPendente;
        projetilPendente = null;
        return p;
    }

    // ==================================================================
    // Combate: hurtbox, hitbox e dano
    // ==================================================================

    @Override
    public Rectangle getHurtbox() {
        int cx = (int) getCentroX();
        int pes = (int) (getY() + getAltura());
        if (estado == EstadoLutador.DERROTADO) {
            return new Rectangle(cx - 20, pes - 30, 40, 30);
        }
        if (estado == EstadoLutador.AGACHADO) {
            return new Rectangle(cx - 22, pes - 95, 44, 95);   // hurtbox reduzida
        }
        if (!noChao) {
            return new Rectangle(cx - 20, pes - 135, 40, 115); // pernas encolhidas no ar
        }
        return new Rectangle(cx - 20, pes - ALTURA_LUTADOR, 40, ALTURA_LUTADOR);
    }

    /**
     * Hitbox ofensiva: existe apenas nos frames ativos de um soco/chute e
     * apenas enquanto o golpe ainda não acertou (um acerto por execução).
     *
     * @return o retângulo do golpe, ou null se não houver golpe ativo.
     */
    public Rectangle getHitbox() {
        if (golpeAcertou || getFaseGolpe() != FaseGolpe.ATIVO) {
            return null;
        }
        int alcance;
        int topo;
        int alturaGolpe;
        if (estado == EstadoLutador.SOCANDO) {
            alcance = SOCO_ALCANCE;
            topo = 130;
            alturaGolpe = 40;
        } else if (estado == EstadoLutador.CHUTANDO) {
            alcance = CHUTE_ALCANCE;
            topo = 105;
            alturaGolpe = 45;
        } else {
            return null; // o especial causa dano pelo projétil, não por hitbox
        }
        int cx = (int) getCentroX();
        int pes = (int) (getY() + getAltura());
        int hx = olhandoDireita ? cx + 10 : cx - 10 - alcance;
        return new Rectangle(hx, pes - topo, alcance, alturaGolpe);
    }

    /** Fase do golpe atual (preparação, ativo, recuperação). */
    public FaseGolpe getFaseGolpe() {
        switch (estado) {
            case SOCANDO:
                return calcularFase(SOCO_INICIO_ATIVO, SOCO_FIM_ATIVO);
            case CHUTANDO:
                return calcularFase(CHUTE_INICIO_ATIVO, CHUTE_FIM_ATIVO);
            case ESPECIAL:
                return calcularFase(ESPECIAL_DISPARO, ESPECIAL_DISPARO + 6);
            default:
                return FaseGolpe.NENHUMA;
        }
    }

    private FaseGolpe calcularFase(int inicioAtivo, int fimAtivo) {
        if (framesAtaque < inicioAtivo) {
            return FaseGolpe.PREPARACAO;
        }
        if (framesAtaque <= fimAtivo) {
            return FaseGolpe.ATIVO;
        }
        return FaseGolpe.RECUPERACAO;
    }

    /** Marca que o golpe atual já acertou, desativando sua hitbox. */
    public void registrarAcerto() {
        golpeAcertou = true;
    }

    public int getDanoGolpeAtual() {
        if (estado == EstadoLutador.SOCANDO) {
            return SOCO_DANO;
        }
        if (estado == EstadoLutador.CHUTANDO) {
            return CHUTE_DANO;
        }
        return 0;
    }

    /**
     * Aplica dano e knockback.
     *
     * @param dano    dano bruto do golpe
     * @param direcao direção do empurrão (+1 direita, -1 esquerda)
     * @return dano efetivamente aplicado
     */
    public int receberDano(int dano, int direcao) {
        if (estado == EstadoLutador.DERROTADO) {
            return 0;
        }
        projetilPendente = null;
        framesAtaque = 0;

        if (isDefendendo()) {
            int reduzido = Math.max(1, dano / 4);
            vida = Math.max(0, vida - reduzido);
            framesBloqueio = DURACAO_BLOQUEIO;
            estado = EstadoLutador.DEFENDENDO;
            setVelocidadeX(direcao * 3.5);
            if (vida == 0) {
                derrotar(direcao);
            }
            return reduzido;
        }

        vida = Math.max(0, vida - dano);
        framesPiscarDano = 8;
        if (vida == 0) {
            derrotar(direcao);
        } else {
            framesAtordoado = DURACAO_ATORDOAMENTO;
            estado = EstadoLutador.ATINGIDO;
            setVelocidadeX(direcao * 6);
            if (!noChao) {
                setVelocidadeY(-4);
            }
        }
        return dano;
    }

    private void derrotar(int direcao) {
        estado = EstadoLutador.DERROTADO;
        framesDerrota = 0;
        framesAtordoado = 0;
        framesBloqueio = 0;
        setVelocidadeX(direcao * 5);
        setVelocidadeY(-8);
        noChao = false;
    }

    /** Coloca o lutador em pose de vitória. */
    public void comemorar() {
        if (estado != EstadoLutador.DERROTADO) {
            estado = EstadoLutador.VITORIOSO;
            framesAtaque = 0;
            framesAtordoado = 0;
            framesBloqueio = 0;
        }
    }

    public boolean isDefendendo() {
        return estado == EstadoLutador.DEFENDENDO && noChao;
    }

    public boolean isVulneravel() {
        return estado != EstadoLutador.DERROTADO;
    }

    public boolean isAtacando() {
        return estado.isAtaque();
    }

    public boolean isEspecialPronto() {
        return cooldownEspecial == 0;
    }

    /** @return valor entre 0 (recém usado) e 1 (pronto). */
    public double getPercentualRecarga() {
        return 1.0 - (double) cooldownEspecial / COOLDOWN_ESPECIAL;
    }

    private static int getDuracaoGolpe(EstadoLutador tipo) {
        switch (tipo) {
            case SOCANDO:
                return SOCO_DURACAO;
            case CHUTANDO:
                return CHUTE_DURACAO;
            case ESPECIAL:
                return ESPECIAL_DURACAO;
            default:
                return 0;
        }
    }

    @Override
    public void aceitar(VisitanteEntidade visitante) {
        visitante.visitar(this);
    }

    // ==================================================================
    // Getters e setters
    // ==================================================================

    public Personagem getPersonagem() {
        return personagem;
    }

    public String getNome() {
        return personagem.getNome();
    }

    public int getVida() {
        return vida;
    }

    public EstadoLutador getEstado() {
        return estado;
    }

    public boolean isOlhandoDireita() {
        return olhandoDireita;
    }

    public boolean isNoChao() {
        return noChao;
    }

    public int getFramesAtaque() {
        return framesAtaque;
    }

    public int getFramesDerrota() {
        return framesDerrota;
    }

    public int getFramesPiscarDano() {
        return framesPiscarDano;
    }

    public int getContadorAnimacao() {
        return contadorAnimacao;
    }

    public boolean isControleAtivo() {
        return controleAtivo;
    }

    public void setControleAtivo(boolean controleAtivo) {
        this.controleAtivo = controleAtivo;
        if (!controleAtivo) {
            limparComandos();
        }
    }

    protected Lutador getOponente() {
        return oponente;
    }

    public void setOponente(Lutador oponente) {
        this.oponente = oponente;
    }
}
