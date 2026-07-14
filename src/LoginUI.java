import javax.swing.*;
import java.awt.*;

public class LoginUI extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;

    private static final Color VERDE_OSCURO = new Color(30, 80, 40);
    private static final Color BEIGE = new Color(245, 240, 230);
    private static final Color VERDE_BTN = new Color(45, 110, 55);

    public LoginUI() {
        setTitle("Yerbatera C&M — Iniciar Sesión");
        setSize(500, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setIconImage(BalanzaUI.iconoApp(32));

        // Header verde
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(VERDE_OSCURO);
        header.setPreferredSize(new Dimension(500, 80));
        JLabel titulo = new JLabel("Balanza — Yerbatera C&M");
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        titulo.setForeground(Color.WHITE);
        header.add(titulo);

        // Panel central beige
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BEIGE);
        panel.setBorder(BorderFactory.createEmptyBorder(50, 60, 50, 60));

        // Label usuario
        JLabel lblUsuario = new JLabel("Usuario");
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 13));
        lblUsuario.setForeground(VERDE_OSCURO);
        lblUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblUsuario);
        panel.add(Box.createVerticalStrut(5));

        txtUsuario = new JTextField();
        txtUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtUsuario.setFont(new Font("Arial", Font.PLAIN, 14));
        txtUsuario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VERDE_OSCURO, 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panel.add(txtUsuario);
        panel.add(Box.createVerticalStrut(20));

        // Label contraseña
        JLabel lblPassword = new JLabel("Contraseña");
        lblPassword.setFont(new Font("Arial", Font.BOLD, 13));
        lblPassword.setForeground(VERDE_OSCURO);
        lblPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblPassword);
        panel.add(Box.createVerticalStrut(5));

        // Campo contraseña con botón Ver
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JButton btnVer = new JButton(iconoOjo(false));
        btnVer.setFont(new Font("Arial", Font.PLAIN, 11));
        btnVer.setBackground(Color.WHITE);
        btnVer.setForeground(VERDE_OSCURO);
        btnVer.setFocusPainted(false);
        btnVer.setBorderPainted(false);
        btnVer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVer.setPreferredSize(new Dimension(60, 40));
        btnVer.addActionListener(e -> {
            if (txtPassword.getEchoChar() == 0) {
                txtPassword.setEchoChar('•');
                btnVer.setIcon(iconoOjo(false));
            } else {
                txtPassword.setEchoChar((char) 0);
                btnVer.setIcon(iconoOjo(true));
            }
        });

        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passPanel.setBackground(BEIGE);
        passPanel.setBorder(BorderFactory.createLineBorder(VERDE_OSCURO, 1, true));
        passPanel.add(txtPassword, BorderLayout.CENTER);
        passPanel.add(btnVer, BorderLayout.EAST);

        panel.add(passPanel);
        panel.add(Box.createVerticalStrut(30));

        // Boton ingresar
        JButton btnLogin = new JButton("Ingresar");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnLogin.setFont(new Font("Arial", Font.BOLD, 15));
        btnLogin.setBackground(VERDE_BTN);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setBorderPainted(false);
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> validar());
        txtPassword.addActionListener(e -> validar());
        panel.add(btnLogin);

        panel.add(Box.createVerticalStrut(10));

        // Boton salir
        JButton btnSalir = new JButton("Salir");
        btnSalir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnSalir.setFont(new Font("Arial", Font.BOLD, 15));
        btnSalir.setBackground(new Color(150, 40, 40));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setFocusPainted(false);
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalir.setBorderPainted(false);
        btnSalir.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSalir.addActionListener(e -> System.exit(0));
        panel.add(btnSalir);

        add(header, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
    }

    private void validar() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (usuario.equals("C&M") && password.equals("123456")) {
            dispose();
            new BalanzaUI().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Usuario o contraseña incorrectos.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
        }
    }
    private static ImageIcon iconoOjo(boolean visible) {
        int s = 16;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(s, s, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(VERDE_OSCURO);
        // Dibujar ojo
        g.drawArc(2, 4, 12, 8, 0, 180);
        g.drawArc(2, 4, 12, 8, 0, -180);
        g.fillOval(6, 6, 4, 4);
        if (!visible) {
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(2, 2, 14, 14);
        }
        g.dispose();
        return new ImageIcon(img);
    }
}