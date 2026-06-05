import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            BalanzaUI ventana =
                    new BalanzaUI();

            ventana.setVisible(true);

        });

    }

}