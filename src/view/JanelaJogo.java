package view;

import javax.swing.JFrame;

/**
 * Janela principal (JFrame): título, tamanho fixo de 900x540,
 * centralizada e não redimensionável.
 */
public class JanelaJogo extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String TITULO = "Fúria no Dojo - Arcade Fighter";

    public JanelaJogo(PainelJogo painel) {
        super(TITULO);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(painel);
        pack();                       // área útil = tamanho preferido do painel (900x540)
        setLocationRelativeTo(null);  // centraliza na tela
    }
}
