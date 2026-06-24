import Bd.RegistroDAO;
import Models.Producto;

import javax.swing.*;
import java.awt.*;

public class ActualizarUI extends JFrame {

    // Colores yerba mate
    private static final Color VERDE_OSCURO = new Color(26,  77,  46);
    private static final Color VERDE_MEDIO  = new Color(79,  119, 45);
    private static final Color NARANJA      = new Color(196, 98,  31);
    private static final Color BEIGE        = new Color(245, 241, 232);
    private static final Color VERDE_CLARO  = new Color(232, 245, 224);
    private static final Color BORDE_CAMPO  = new Color(150, 190, 100);
    private static final Color TEXTO_OSCURO = new Color(40,  40,  40);

    public ActualizarUI() {

        setTitle("Modificar Registro — Balanza");
        setSize(420, 820);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);
        getContentPane().setBackground(BEIGE);

        // ── Título ──────────────────────────────────────
        JLabel lblTitulo = crearLabel("Modificar Registro");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(VERDE_OSCURO);
        lblTitulo.setBounds(20, 14, 350, 28);
        add(lblTitulo);

        JSeparator sep = new JSeparator();
        sep.setBounds(20, 46, 360, 2);
        sep.setForeground(VERDE_MEDIO);
        add(sep);

        // ── Búsqueda ────────────────────────────────────
        JLabel lblBuscar = crearLabel("Nro Cupón a buscar:");
        lblBuscar.setBounds(20, 58, 220, 20);
        add(lblBuscar);

        JTextField txtBuscar = crearCampo();
        txtBuscar.setBounds(20, 80, 220, 30);
        add(txtBuscar);

        JButton btnBuscar = crearBoton("Buscar", VERDE_OSCURO, Color.WHITE);
        btnBuscar.setBounds(252, 80, 110, 30);
        add(btnBuscar);

        JSeparator sep2 = new JSeparator();
        sep2.setBounds(20, 124, 360, 2);
        sep2.setForeground(new Color(200, 210, 190));
        add(sep2);

        // ── Campos del registro ─────────────────────────
        int y = 134;

        JLabel lblCupon = crearLabel("Nro Cupón");
        lblCupon.setBounds(20, y, 220, 20); add(lblCupon);
        JTextField txtCupon = crearCampo();
        txtCupon.setBounds(20, y + 23, 360, 30);
        txtCupon.setEditable(false);
        txtCupon.setBackground(VERDE_CLARO);
        add(txtCupon); y += 68;

        JLabel lblFecha = crearLabel("Fecha");
        lblFecha.setBounds(20, y, 220, 20); add(lblFecha);
        JTextField txtFecha = crearCampo();
        txtFecha.setBounds(20, y + 23, 360, 30); add(txtFecha); y += 68;

        JLabel lblNroProductor = crearLabel("Nro Productor");
        lblNroProductor.setBounds(20, y, 220, 20); add(lblNroProductor);
        JTextField txtNroProductor = crearCampo();
        txtNroProductor.setBounds(20, y + 23, 360, 30); add(txtNroProductor); y += 68;

        JLabel lblNombre = crearLabel("Nombre");
        lblNombre.setBounds(20, y, 220, 20); add(lblNombre);
        JTextField txtNombre = crearCampo();
        txtNombre.setBounds(20, y + 23, 360, 30); add(txtNombre); y += 68;

        JLabel lblBruto = crearLabel("Peso Bruto (kg)");
        lblBruto.setBounds(20, y, 220, 20); add(lblBruto);
        JTextField txtBruto = crearCampo();
        txtBruto.setBounds(20, y + 23, 360, 30); add(txtBruto); y += 68;

        JLabel lblTara = crearLabel("Tara (kg)");
        lblTara.setBounds(20, y, 220, 20); add(lblTara);
        JTextField txtTara = crearCampo();
        txtTara.setBounds(20, y + 23, 360, 30); add(txtTara); y += 68;

        JLabel lblDescuento = crearLabel("Descuento (%)");
        lblDescuento.setBounds(20, y, 220, 20); add(lblDescuento);
        JTextField txtDescuento = crearCampo();
        txtDescuento.setBounds(20, y + 23, 360, 30); add(txtDescuento); y += 68;

        JLabel lblRemito = crearLabel("Remito");
        lblRemito.setBounds(20, y, 220, 20); add(lblRemito);
        JTextField txtRemito = crearCampo();
        txtRemito.setBounds(20, y + 23, 360, 30); add(txtRemito); y += 80;

        // ── Botones ─────────────────────────────────────
        JButton btnActualizar = crearBoton("Modificar", VERDE_MEDIO, Color.WHITE);
        JButton btnCancelar   = crearBoton("Cancelar",  NARANJA,     Color.WHITE);
        btnActualizar.setBounds(20,  y, 170, 40);
        btnCancelar.setBounds(200,   y, 170, 40);
        add(btnActualizar);
        add(btnCancelar);

        // Deshabilitar hasta buscar
        btnActualizar.setEnabled(false);

        // ── EVENTO BUSCAR ────────────────────────────────
        btnBuscar.addActionListener(e -> {
            String textoCupon = txtBuscar.getText().trim();
            if (textoCupon.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingresá un Nro de Cupón para buscar.");
                return;
            }
            if (!textoCupon.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "El Nro de Cupón solo puede contener números.");
                return;
            }

            Producto p = RegistroDAO.buscarPorCupon(Integer.parseInt(textoCupon));
            if (p == null) {
                JOptionPane.showMessageDialog(this, "No se encontró ningún registro con ese Nro de Cupón.");
                btnActualizar.setEnabled(false);
                return;
            }

            txtCupon.setText(String.valueOf(p.getNroCupon()));
            txtFecha.setText(p.getFecha());
            txtNroProductor.setText(p.getNroProductor());
            txtNombre.setText(p.getNombre());
            txtBruto.setText(String.valueOf(p.getPesoBruto()));
            txtTara.setText(String.valueOf(p.getTara()));
            txtDescuento.setText(String.valueOf(p.getDescuento()));
            txtRemito.setText(String.valueOf(p.getRemito()));
            btnActualizar.setEnabled(true);
        });

        // ── EVENTO ACTUALIZAR ─────────────────────────────
        btnActualizar.addActionListener(e -> {
            if (txtNroProductor.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El Nro Productor no puede estar vacío."); return;
            }
            if (!txtNroProductor.getText().trim().matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "El Nro Productor solo puede contener números."); return;
            }
            if (txtNombre.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El Nombre no puede estar vacío."); return;
            }
            if (!txtNombre.getText().trim().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
                JOptionPane.showMessageDialog(this, "El Nombre solo puede contener letras."); return;
            }
            if (txtBruto.getText().trim().isEmpty() || txtTara.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Peso Bruto y Tara no pueden estar vacíos."); return;
            }

            try {
                double bruto     = Double.parseDouble(txtBruto.getText().replace(",", "."));
                double tara      = Double.parseDouble(txtTara.getText().replace(",", "."));
                double descuento = Double.parseDouble(txtDescuento.getText().replace(",", "."));

                if (bruto <= 0) { JOptionPane.showMessageDialog(this, "El Peso Bruto debe ser mayor a 0."); return; }
                if (tara < 0)   { JOptionPane.showMessageDialog(this, "La Tara no puede ser negativa."); return; }
                if (tara >= bruto) { JOptionPane.showMessageDialog(this, "La Tara no puede ser mayor o igual al Peso Bruto."); return; }
                if (descuento < 0 || descuento > 100) { JOptionPane.showMessageDialog(this, "El Descuento debe estar entre 0 y 100."); return; }

                if (txtRemito.getText().trim().isEmpty() || !txtRemito.getText().trim().matches("\\d+")
                        || Integer.parseInt(txtRemito.getText().trim()) <= 0) {
                    JOptionPane.showMessageDialog(this, "El Remito debe ser un número mayor a 0."); return;
                }

                boolean exito = RegistroDAO.actualizar(
                        Integer.parseInt(txtCupon.getText()),
                        txtNroProductor.getText().trim(),
                        txtNombre.getText().trim(),
                        bruto, tara, descuento,
                        Integer.parseInt(txtRemito.getText().trim()),
                        txtFecha.getText().trim()
                );

                if (exito) {
                    JOptionPane.showMessageDialog(this, "Registro actualizado correctamente.");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo actualizar el registro.");
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Peso Bruto, Tara y Descuento deben ser números.");
            }
        });

        // ── EVENTO CANCELAR ──────────────────────────────
        btnCancelar.addActionListener(e -> dispose());
    }

    // ── Helpers de estilo ────────────────────────────────
    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(VERDE_OSCURO);
        return lbl;
    }

    private JTextField crearCampo() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(TEXTO_OSCURO);
        tf.setBackground(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_CAMPO, 1),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        return tf;
    }

    private JButton crearBoton(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
