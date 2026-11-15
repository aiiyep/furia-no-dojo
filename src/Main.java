import controller.ControladorJogo;
import javax.swing.SwingUtilities;
import model.Jogo;
import view.JanelaJogo;
import view.PainelJogo;

/**
 * Ponto de entrada: monta as três camadas do MVC e as conecta,
 * tudo dentro da Event Dispatch Thread (EDT) do Swing.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Jogo modelo = new Jogo();                                         // Model
            PainelJogo painel = new PainelJogo(modelo);                       // View
            JanelaJogo janela = new JanelaJogo(painel);
            ControladorJogo controlador = new ControladorJogo(modelo, painel); // Controller
            janela.setVisible(true);
            controlador.iniciar();
        });
    }
}
