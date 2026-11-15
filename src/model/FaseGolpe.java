package model;

/**
 * Fase de execução de um golpe, usada pela View para escolher a pose.
 */
public enum FaseGolpe {
    /** O lutador não está atacando. */
    NENHUMA,
    /** Preparação (startup): o golpe ainda não pode acertar. */
    PREPARACAO,
    /** Frames ativos: a hitbox existe (ou o projétil é disparado). */
    ATIVO,
    /** Recuperação: o golpe terminou, mas o lutador ainda não pode agir. */
    RECUPERACAO
}
