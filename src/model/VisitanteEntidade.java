package model;

/**
 * Padrão Visitor: permite que outra camada (a View) trate cada tipo de
 * entidade de forma específica sem que o Model dependa dela e sem usar
 * instanceof. Cada entidade chama o método correspondente ao seu tipo
 * (double dispatch, uma forma de polimorfismo).
 */
public interface VisitanteEntidade {

    void visitar(Lutador lutador);

    void visitar(Projetil projetil);
}
