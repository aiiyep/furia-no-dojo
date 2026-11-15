package model;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * MODEL principal: o estado completo da partida e todas as regras.
 *
 * Contém a máquina de estados ({@link EstadoJogo}), os lutadores, os
 * projéteis, os rounds, o tempo, a pontuação e a resolução de colisões.
 * Não conhece Swing, teclado nem desenho: o Controller chama seus métodos
 * e a View apenas lê seus getters.
 */
public class Jogo {

    public static final int TEMPO_ROUND = 60;
    public static final int ROUNDS_PARA_VENCER = 2;
    public static final int DURACAO_INTRO = 110;
    public static final int INICIO_TEXTO_LUTEM = 45;
    public static final int FRAME_EXIBIR_RESULTADO = 50;

    private static final int FPS = 60;
    private static final int DURACAO_FIM_ROUND = 210;
    private static final int FRAME_COMEMORAR = 60;
    private static final double X_INICIAL_ESQUERDA = 200;
    private static final double X_INICIAL_DIREITA = Arena.LARGURA - 200 - Lutador.LARGURA_LUTADOR;

    private final Random aleatorio = new Random();
    private final List<Projetil> projeteis = new ArrayList<>();
    private final List<Faisca> faiscas = new ArrayList<>();
    private final List<TextoFlutuante> textos = new ArrayList<>();
    private final Pontuacao[] pontuacoes = {new Pontuacao(), new Pontuacao()};
    private final int[] vitorias = new int[2];
    private final double[] vidaExibida = {Lutador.VIDA_MAXIMA, Lutador.VIDA_MAXIMA};

    private EstadoJogo estado = EstadoJogo.MENU;
    private ModoJogo modo = ModoJogo.UM_JOGADOR;
    private int opcaoMenu;
    private Lutador lutador1;
    private Lutador lutador2;

    private int roundAtual = 1;
    private int tempoRestante = TEMPO_ROUND;
    private int contadorFramesTempo;
    private int framesIntro;
    private int framesFimRound;
    private int ladoVencedorRound = -1;
    private int ladoVencedorPartida = -1;
    private boolean fimPorNocaute;
    private String subMensagemFimRound = "";
    private String detalheFimRound = "";
    private boolean pausado;

    private int hitstop;
    private int tremor;
    private int recorde;
    private long contadorFrames;

    public Jogo() {
        criarLutadores(modo);
    }

    private void criarLutadores(ModoJogo novoModo) {
        lutador1 = new Lutador(Personagem.TAKESHI, X_INICIAL_ESQUERDA, true);
        if (novoModo == ModoJogo.UM_JOGADOR) {
            LutadorIA cpu = new LutadorIA(Personagem.SOMBRA, X_INICIAL_DIREITA, false);
            cpu.setProjeteisEmCena(Collections.unmodifiableList(projeteis));
            lutador2 = cpu;
        } else {
            lutador2 = new Lutador(Personagem.SOMBRA, X_INICIAL_DIREITA, false);
        }
        lutador1.setOponente(lutador2);
        lutador2.setOponente(lutador1);
    }

    // ==================================================================
    // Ações disparadas pelo Controller
    // ==================================================================

    public void moverSelecaoMenu(int delta) {
        int total = ModoJogo.values().length;
        opcaoMenu = (opcaoMenu + delta + total) % total;
    }

    public void confirmarMenu() {
        iniciarPartida(ModoJogo.values()[opcaoMenu]);
    }

    public void iniciarPartida(ModoJogo novoModo) {
        modo = novoModo;
        criarLutadores(novoModo);
        vitorias[0] = 0;
        vitorias[1] = 0;
        pontuacoes[0].zerar();
        pontuacoes[1].zerar();
        ladoVencedorPartida = -1;
        pausado = false;
        iniciarRound(1);
    }

    public void reiniciarPartida() {
        iniciarPartida(modo);
    }

    public void voltarAoMenu() {
        lutador1.reiniciar();
        lutador2.reiniciar();
        projeteis.clear();
        faiscas.clear();
        textos.clear();
        pausado = false;
        tremor = 0;
        estado = EstadoJogo.MENU;
    }

    public void alternarPausa() {
        if (estado == EstadoJogo.JOGANDO) {
            pausado = !pausado;
        }
    }

    // ==================================================================
    // Atualização (um frame)
    // ==================================================================

    public void atualizar() {
        contadorFrames++;
        switch (estado) {
            case MENU:
                lutador1.atualizar();
                lutador2.atualizar();
                break;
            case JOGANDO:
                if (!pausado) {
                    atualizarCombate();
                }
                break;
            case FIM_ROUND:
                atualizarFimRound();
                break;
            case VITORIA:
            case GAME_OVER:
                atualizarEntidades();
                atualizarEfeitos();
                break;
            default:
                break;
        }
    }

    private void iniciarRound(int numero) {
        roundAtual = numero;
        lutador1.reiniciar();
        lutador2.reiniciar();
        if (lutador2 instanceof LutadorIA) {
            ((LutadorIA) lutador2).setNivel(numero + 1); // CPU mais rápida a cada round
        }
        projeteis.clear();
        faiscas.clear();
        textos.clear();
        tempoRestante = TEMPO_ROUND;
        contadorFramesTempo = 0;
        framesIntro = DURACAO_INTRO;
        pontuacoes[0].novoRound();
        pontuacoes[1].novoRound();
        hitstop = 0;
        tremor = 0;
        vidaExibida[0] = Lutador.VIDA_MAXIMA;
        vidaExibida[1] = Lutador.VIDA_MAXIMA;
        ladoVencedorRound = -1;
        definirControle(false);
        estado = EstadoJogo.JOGANDO;
    }

    private void definirControle(boolean ativo) {
        lutador1.setControleAtivo(ativo);
        lutador2.setControleAtivo(ativo);
    }

    private void atualizarCombate() {
        if (hitstop > 0) { // congelamento rápido no impacto (sensação de peso)
            hitstop--;
            return;
        }
        if (tremor > 0) {
            tremor--;
        }
        if (framesIntro > 0) {
            framesIntro--;
            if (framesIntro == 0) {
                definirControle(true);
            }
        } else {
            contarTempo();
        }

        atualizarEntidades();
        resolverColisoes();
        atualizarEfeitos();
        pontuacoes[0].atualizar();
        pontuacoes[1].atualizar();
        verificarFimRound();
    }

    private void contarTempo() {
        contadorFramesTempo++;
        if (contadorFramesTempo >= FPS) {
            contadorFramesTempo = 0;
            if (tempoRestante > 0) {
                tempoRestante--;
            }
        }
    }

    /** Atualiza todas as entidades de forma polimórfica e coleta novos projéteis. */
    private void atualizarEntidades() {
        for (Entidade entidade : getEntidades()) {
            entidade.atualizar(); // Lutador, LutadorIA e Projetil respondem cada um à sua maneira
        }
        adicionarProjetil(lutador1.consumirProjetilPendente());
        adicionarProjetil(lutador2.consumirProjetilPendente());
        projeteis.removeIf(p -> !p.isAtivo());
    }

    private void adicionarProjetil(Projetil p) {
        if (p != null) {
            projeteis.add(p);
        }
    }

    // ==================================================================
    // Colisões
    // ==================================================================

    private void resolverColisoes() {
        verificarGolpeFisico(lutador1, lutador2);
        verificarGolpeFisico(lutador2, lutador1);
        verificarProjeteis();
        separarLutadores();
    }

    private void verificarGolpeFisico(Lutador atacante, Lutador alvo) {
        Rectangle hitbox = atacante.getHitbox();
        if (hitbox == null || !alvo.isVulneravel()) {
            return;
        }
        Rectangle hurtbox = alvo.getHurtbox();
        if (hitbox.intersects(hurtbox)) {
            EstadoLutador tipo = atacante.getEstado();
            int dano = atacante.getDanoGolpeAtual();
            atacante.registrarAcerto(); // o mesmo golpe não acerta duas vezes
            Rectangle impacto = hitbox.intersection(hurtbox);
            int direcao = atacante.isOlhandoDireita() ? 1 : -1;
            aplicarAcerto(atacante, alvo, dano, tipo, direcao, impacto.getCenterX(), impacto.getCenterY());
        }
    }

    private void verificarProjeteis() {
        // Projétil x projétil: se forem de donos diferentes, anulam-se
        for (int i = 0; i < projeteis.size(); i++) {
            Projetil a = projeteis.get(i);
            for (int j = i + 1; j < projeteis.size(); j++) {
                Projetil b = projeteis.get(j);
                if (a.isAtivo() && b.isAtivo() && a.getDono() != b.getDono() && a.colideCom(b)) {
                    a.setAtivo(false);
                    b.setAtivo(false);
                    double mx = (a.getCentroX() + b.getCentroX()) / 2;
                    double my = (a.getCentroY() + b.getCentroY()) / 2;
                    criarFaiscas(mx, my, Faisca.Tipo.ANULACAO, 18);
                    criarFaiscas(mx, my, Faisca.Tipo.BRILHO, 8);
                    tremor = 6;
                    pontuacoes[0].registrarAnulacao();
                    pontuacoes[1].registrarAnulacao();
                    textos.add(new TextoFlutuante("ANULADO!", mx, my - 20, TextoFlutuante.Tipo.INFO));
                }
            }
        }

        // Projétil x hurtbox do adversário
        for (Projetil p : projeteis) {
            if (!p.isAtivo()) {
                continue;
            }
            Lutador alvo = (p.getDono() == lutador1) ? lutador2 : lutador1;
            if (alvo.isVulneravel() && p.colideCom(alvo)) {
                p.setAtivo(false);
                aplicarAcerto(p.getDono(), alvo, p.getDano(), EstadoLutador.ESPECIAL,
                        p.getDirecao(), p.getCentroX(), p.getCentroY());
            }
        }
        projeteis.removeIf(p -> !p.isAtivo());
    }

    private void aplicarAcerto(Lutador atacante, Lutador alvo, int dano, EstadoLutador tipo,
                               int direcao, double px, double py) {
        boolean defendeu = alvo.isDefendendo();
        alvo.receberDano(dano, direcao);

        hitstop = defendeu ? 3 : 6;
        tremor = defendeu ? 3 : (tipo == EstadoLutador.SOCANDO ? 5 : 9);
        if (defendeu) {
            criarFaiscas(px, py, Faisca.Tipo.DEFESA, 8);
        } else {
            criarFaiscas(px, py, Faisca.Tipo.IMPACTO, 14);
            criarFaiscas(px, py, Faisca.Tipo.BRILHO, 6);
        }

        Pontuacao pontuacaoAtacante = pontuacoes[getLado(atacante)];
        pontuacoes[getLado(alvo)].interromperCombo();
        if (defendeu) {
            int pontos = pontuacaoAtacante.registrarDefendido();
            textos.add(new TextoFlutuante("+" + pontos, px, py - 20, TextoFlutuante.Tipo.DEFESA));
        } else {
            int pontos = pontuacaoAtacante.registrarAcerto(tipo);
            textos.add(new TextoFlutuante("+" + pontos, px, py - 20, TextoFlutuante.Tipo.PONTOS));
        }
    }

    /** Impede que os corpos dos lutadores se sobreponham. */
    private void separarLutadores() {
        if (lutador1.getEstado() == EstadoLutador.DERROTADO || lutador2.getEstado() == EstadoLutador.DERROTADO) {
            return;
        }
        if (!lutador1.getHurtbox().intersects(lutador2.getHurtbox())) {
            return;
        }
        Lutador esquerda;
        Lutador direita;
        if (lutador1.getCentroX() < lutador2.getCentroX()
                || (lutador1.getCentroX() == lutador2.getCentroX() && lutador1.isOlhandoDireita())) {
            esquerda = lutador1;
            direita = lutador2;
        } else {
            esquerda = lutador2;
            direita = lutador1;
        }
        double sobreposicao = sobreposicaoHorizontal(esquerda, direita);
        if (sobreposicao <= 0) {
            return;
        }
        esquerda.empurrar(-sobreposicao / 2);
        direita.empurrar(sobreposicao / 2);

        // Se um deles estiver encostado na parede, o outro é empurrado o restante
        double restante = sobreposicaoHorizontal(esquerda, direita);
        if (restante > 0) {
            double antes = esquerda.getX();
            esquerda.empurrar(-restante);
            double movido = antes - esquerda.getX();
            direita.empurrar(restante - movido);
        }
    }

    private static double sobreposicaoHorizontal(Lutador esquerda, Lutador direita) {
        return esquerda.getHurtbox().getMaxX() - direita.getHurtbox().getX();
    }

    // ==================================================================
    // Fim de round / partida
    // ==================================================================

    private void verificarFimRound() {
        if (lutador1.getVida() <= 0 || lutador2.getVida() <= 0) {
            finalizarRound(true);
        } else if (tempoRestante <= 0) {
            finalizarRound(false);
        }
    }

    private void finalizarRound(boolean nocaute) {
        definirControle(false);
        projeteis.clear();
        fimPorNocaute = nocaute;
        detalheFimRound = "";

        int v1 = lutador1.getVida();
        int v2 = lutador2.getVida();
        ladoVencedorRound = v1 > v2 ? 0 : (v2 > v1 ? 1 : -1);

        if (ladoVencedorRound >= 0) {
            Lutador vencedor = getLutador(ladoVencedorRound);
            vitorias[ladoVencedorRound]++;
            boolean perfeito = vencedor.getVida() == Lutador.VIDA_MAXIMA;
            String bonus = pontuacoes[ladoVencedorRound].registrarVitoriaRound(vencedor.getVida(), tempoRestante);
            boolean vencedorEhCpu = modo == ModoJogo.UM_JOGADOR && ladoVencedorRound == 1;
            detalheFimRound = vencedorEhCpu ? "" : bonus;
            subMensagemFimRound = (perfeito ? "PERFECT! " : "") + vencedor.getNome() + " VENCE O ROUND!";
        } else {
            subMensagemFimRound = "EMPATE! O ROUND SERÁ REPETIDO";
        }

        framesFimRound = 0;
        hitstop = nocaute ? 25 : 0; // "câmera lenta" no nocaute
        tremor = nocaute ? 14 : 0;
        estado = EstadoJogo.FIM_ROUND;
    }

    private void atualizarFimRound() {
        if (hitstop > 0) {
            hitstop--;
            return;
        }
        framesFimRound++;
        if (tremor > 0) {
            tremor--;
        }
        atualizarEntidades();
        separarLutadores();
        atualizarEfeitos();

        if (framesFimRound == FRAME_COMEMORAR && ladoVencedorRound >= 0) {
            getLutador(ladoVencedorRound).comemorar();
        }
        if (framesFimRound >= DURACAO_FIM_ROUND) {
            int lado = vitorias[0] >= ROUNDS_PARA_VENCER ? 0 : (vitorias[1] >= ROUNDS_PARA_VENCER ? 1 : -1);
            if (lado >= 0) {
                ladoVencedorPartida = lado;
                pontuacoes[lado].registrarVitoriaPartida();
                atualizarRecorde();
                boolean cpuVenceu = modo == ModoJogo.UM_JOGADOR && lado == 1;
                estado = cpuVenceu ? EstadoJogo.GAME_OVER : EstadoJogo.VITORIA;
            } else {
                iniciarRound(roundAtual + 1);
            }
        }
    }

    private void atualizarRecorde() {
        recorde = Math.max(recorde, pontuacoes[0].getTotal());
        if (modo == ModoJogo.DOIS_JOGADORES) {
            recorde = Math.max(recorde, pontuacoes[1].getTotal());
        }
    }

    // ==================================================================
    // Efeitos
    // ==================================================================

    private void atualizarEfeitos() {
        for (Iterator<Faisca> it = faiscas.iterator(); it.hasNext(); ) {
            Faisca f = it.next();
            f.atualizar();
            if (!f.isViva()) {
                it.remove();
            }
        }
        for (Iterator<TextoFlutuante> it = textos.iterator(); it.hasNext(); ) {
            TextoFlutuante t = it.next();
            t.atualizar();
            if (!t.isVivo()) {
                it.remove();
            }
        }
        vidaExibida[0] = aproximarVida(vidaExibida[0], lutador1.getVida());
        vidaExibida[1] = aproximarVida(vidaExibida[1], lutador2.getVida());
    }

    private static double aproximarVida(double exibida, int real) {
        if (exibida > real) {
            return Math.max(real, exibida - 0.6);
        }
        return real;
    }

    private void criarFaiscas(double x, double y, Faisca.Tipo tipo, int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            double angulo = aleatorio.nextDouble() * Math.PI * 2;
            double velocidade = 2 + aleatorio.nextDouble() * 5;
            faiscas.add(new Faisca(x, y, Math.cos(angulo) * velocidade, Math.sin(angulo) * velocidade - 1.5,
                    14 + aleatorio.nextInt(12), tipo));
        }
    }

    // ==================================================================
    // Consultas (usadas pela View e pelo Controller)
    // ==================================================================

    /** Entidades na ordem de desenho. */
    public List<Entidade> getEntidades() {
        List<Entidade> entidades = new ArrayList<>();
        entidades.add(lutador2);
        entidades.add(lutador1);
        entidades.addAll(projeteis);
        return entidades;
    }

    public int getLado(Lutador lutador) {
        return lutador == lutador1 ? 0 : 1;
    }

    public Lutador getLutador(int lado) {
        return lado == 0 ? lutador1 : lutador2;
    }

    public Lutador getLutador1() {
        return lutador1;
    }

    public Lutador getLutador2() {
        return lutador2;
    }

    /** Rótulo do controlador de cada lado: "1P", "2P" ou "CPU". */
    public String getRotuloLado(int lado) {
        if (lado == 0) {
            return "1P";
        }
        return modo == ModoJogo.UM_JOGADOR ? "CPU" : "2P";
    }

    public int getNivelCpu() {
        return lutador2 instanceof LutadorIA ? ((LutadorIA) lutador2).getNivel() : 0;
    }

    public List<Projetil> getProjeteis() {
        return Collections.unmodifiableList(projeteis);
    }

    public List<Faisca> getFaiscas() {
        return Collections.unmodifiableList(faiscas);
    }

    public List<TextoFlutuante> getTextos() {
        return Collections.unmodifiableList(textos);
    }

    public Pontuacao getPontuacao(int lado) {
        return pontuacoes[lado];
    }

    public int getVitorias(int lado) {
        return vitorias[lado];
    }

    public double getVidaExibida(int lado) {
        return vidaExibida[lado];
    }

    public EstadoJogo getEstado() {
        return estado;
    }

    public ModoJogo getModo() {
        return modo;
    }

    public int getOpcaoMenu() {
        return opcaoMenu;
    }

    public int getRoundAtual() {
        return roundAtual;
    }

    public int getTempoRestante() {
        return tempoRestante;
    }

    public int getFramesIntro() {
        return framesIntro;
    }

    public int getFramesFimRound() {
        return framesFimRound;
    }

    public boolean isFimPorNocaute() {
        return fimPorNocaute;
    }

    public String getSubMensagemFimRound() {
        return subMensagemFimRound;
    }

    public String getDetalheFimRound() {
        return detalheFimRound;
    }

    public int getLadoVencedorPartida() {
        return ladoVencedorPartida;
    }

    public boolean isPausado() {
        return pausado;
    }

    public int getTremor() {
        return tremor;
    }

    public int getRecorde() {
        return recorde;
    }

    public long getContadorFrames() {
        return contadorFrames;
    }
}
