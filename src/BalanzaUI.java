import Models.Producto;

import javax.swing.*;
import java.awt.*;
import Bd.RegistroDAO;
import Bd.Conexion;


public class BalanzaUI extends JFrame {

    private JPanel panelRegistros;
    public BalanzaUI() {

        setTitle("Sistema de Balanza");
        setSize(1400, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2, 10, 10));

        // Contador interno para el cupón
        final int[] contadorCupon = {RegistroDAO.getUltimoCupon() + 1};

        //----------------------------------
        // PANEL IZQUIERDO
        //----------------------------------

        JPanel panelFormulario = new JPanel();
        panelFormulario.setLayout(null);

        // FECHA (solo lectura)
        JLabel lblFecha = new JLabel("Fecha");
        lblFecha.setBounds(20, 20, 200, 25);

        JTextField txtFecha = new JTextField();
        txtFecha.setBounds(20, 45, 250, 35);
        txtFecha.setEditable(false);
        txtFecha.setBackground(new Color(220, 220, 220));
        txtFecha.setText(java.time.LocalDate.now().toString());

        // NRO CUPÓN (solo lectura)
        JLabel lblCupon = new JLabel("Nro Cupón");
        lblCupon.setBounds(20, 95, 200, 25);

        JTextField txtCupon = new JTextField();
        txtCupon.setBounds(20, 120, 250, 35);
        txtCupon.setEditable(false);
        txtCupon.setBackground(new Color(220, 220, 220));
        txtCupon.setText(String.valueOf(contadorCupon[0]));

        // NRO PRODUCTOR
        JLabel lblNroProductor = new JLabel("Nro Productor");
        lblNroProductor.setBounds(20, 170, 200, 25);

        JTextField txtNroProductor = new JTextField();
        txtNroProductor.setBounds(20, 195, 250, 35);

        // NOMBRE
        JLabel lblNombre = new JLabel("Nombre");
        lblNombre.setBounds(20, 245, 200, 25);

        JTextField txtNombre = new JTextField();
        txtNombre.setBounds(20, 270, 250, 35);

        // PESO BRUTO
        JLabel lblBruto = new JLabel("Peso Bruto");
        lblBruto.setBounds(20, 320, 200, 25);

        JTextField txtBruto = new JTextField();
        txtBruto.setBounds(20, 345, 250, 35);

        // TARA
        JLabel lblTara = new JLabel("Tara");
        lblTara.setBounds(20, 395, 200, 25);

        JTextField txtTara = new JTextField();
        txtTara.setBounds(20, 420, 250, 35);

        // DESCUENTO
        JLabel lblDescuento = new JLabel("Descuento (%)");
        lblDescuento.setBounds(20, 470, 200, 25);

        JTextField txtDescuento = new JTextField();
        txtDescuento.setBounds(20, 495, 120, 35);
        txtDescuento.setText("0");

        txtDescuento.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtDescuento.getText().equals("0")) {
                    txtDescuento.setText("");
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtDescuento.getText().isEmpty()) {
                    txtDescuento.setText("0");
                }
            }
        });

        // REMITO (solo lectura, igual a Nro Cupón)
        JLabel lblRemito = new JLabel("Remito");
        lblRemito.setBounds(20, 545, 200, 25);

        JTextField txtRemito = new JTextField();
        txtRemito.setBounds(20, 570, 250, 35);


        // BOTÓN GUARDAR
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(20, 640, 120, 40);

        // BOTÓN VER PRODUCTORES
        JButton btnVerProductores = new JButton("Ver Productores");
        btnVerProductores.setBounds(150, 640, 160, 40);

        // BOTÓN SALIR
        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(320, 640, 100, 40);

        //BOTÓN ACTUAIZAR
        JButton btnModificar = new JButton("Modificar");
        btnModificar.setBounds(320, 640, 120, 40);
        panelFormulario.add(btnModificar);

        panelFormulario.add(lblFecha);
        panelFormulario.add(txtFecha);
        panelFormulario.add(lblCupon);
        panelFormulario.add(txtCupon);
        panelFormulario.add(lblNroProductor);
        panelFormulario.add(txtNroProductor);
        panelFormulario.add(lblNombre);
        panelFormulario.add(txtNombre);
        panelFormulario.add(lblBruto);
        panelFormulario.add(txtBruto);
        panelFormulario.add(lblTara);
        panelFormulario.add(txtTara);
        panelFormulario.add(lblDescuento);
        panelFormulario.add(txtDescuento);
        panelFormulario.add(lblRemito);
        panelFormulario.add(txtRemito);
        panelFormulario.add(btnGuardar);
        panelFormulario.add(btnVerProductores);
        panelFormulario.add(btnSalir);



        //----------------------------------
        // PANEL DERECHO
        //----------------------------------

        panelRegistros = new JPanel();
        panelRegistros.setLayout(
                new BoxLayout(panelRegistros, BoxLayout.Y_AXIS)
        );

        JScrollPane scroll = new JScrollPane(panelRegistros);

        //----------------------------------
        // EVENTO GUARDAR
        //----------------------------------
        btnGuardar.addActionListener(e -> {

            if (txtNroProductor.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El Nro Productor no puede estar vacío.");
                return;
            }
            if (!txtNroProductor.getText().trim().matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "El Nro Productor solo puede contener números.");
                return;
            }
            if (txtNombre.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El Nombre no puede estar vacío.");
                return;
            }
            if (!txtNombre.getText().trim().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
                JOptionPane.showMessageDialog(this, "El Nombre solo puede contener letras.");
                return;
            }
            if (txtBruto.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El Peso Bruto no puede estar vacío.");
                return;
            }
            if (txtTara.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "La Tara no puede estar vacía.");
                return;
            }

            ///REMITO
            if (txtRemito.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El Remito no puede estar vacío.");
                return;
            }
            if (!txtRemito.getText().trim().matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "El Remito solo puede contener números.");
                return;
            }
            if (Integer.parseInt(txtRemito.getText().trim()) <= 0) {
                JOptionPane.showMessageDialog(this, "El Remito debe ser mayor a 0.");
                return;
            }
            if (RegistroDAO.remitoExiste(Integer.parseInt(txtRemito.getText().trim()))) {
                JOptionPane.showMessageDialog(this, "Ya existe un registro con ese número de Remito.");
                return;
            }

            try {
                double bruto     = Double.parseDouble(txtBruto.getText().replace(",", "."));
                double tara      = Double.parseDouble(txtTara.getText().replace(",", "."));
                double descuento = Double.parseDouble(txtDescuento.getText().replace(",", "."));

                if (bruto <= 0) {
                    JOptionPane.showMessageDialog(this, "El Peso Bruto debe ser mayor a 0.");
                    return;
                }
                if (tara < 0) {
                    JOptionPane.showMessageDialog(this, "La Tara no puede ser negativa.");
                    return;
                }
                if (tara >= bruto) {
                    JOptionPane.showMessageDialog(this, "La Tara no puede ser mayor o igual al Peso Bruto.");
                    return;
                }
                if (descuento < 0 || descuento > 100) {
                    JOptionPane.showMessageDialog(this, "El Descuento debe estar entre 0 y 100.");
                    return;
                }

                double neto = bruto - tara;
                neto        = Math.round((neto - (neto * descuento / 100)) * 100.0) / 100.0;

                RegistroDAO.insertar(
                        txtFecha.getText(),
                        Integer.parseInt(txtCupon.getText()),
                        txtNroProductor.getText(),
                        txtNombre.getText(),
                        bruto,
                        tara,
                        descuento,
                        neto,
                        Integer.parseInt(txtRemito.getText())
                );

                JLabel registro = new JLabel(
                        "Fecha: "       + txtFecha.getText()
                                + " | Cupón: "  + txtCupon.getText()
                                + " | Prod: "   + txtNroProductor.getText()
                                + " | "         + txtNombre.getText()
                                + " | Neto: "   + neto + " kg"
                                + " | Remito: " + txtRemito.getText()
                );

                registro.setBorder(
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                );

                panelRegistros.add(registro);
                panelRegistros.revalidate();
                panelRegistros.repaint();

                contadorCupon[0]++;
                txtCupon.setText(String.valueOf(contadorCupon[0]));
                txtRemito.setText("");
                txtFecha.setText(java.time.LocalDate.now().toString());

                txtNroProductor.setText("");
                txtNombre.setText("");
                txtBruto.setText("");
                txtTara.setText("");
                txtDescuento.setText("0");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Peso Bruto, Tara y Descuento deben ser números.");
            }
        });

        //----------------------------------
        // EVENTO VER PRODUCTORES
        //----------------------------------
        btnVerProductores.addActionListener(e -> {
            VerProductoresUI ventana = new VerProductoresUI();
            ventana.setVisible(true);
        });
        //----------------------------------
        // EVENTO SALIR
        //----------------------------------
        btnSalir.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Desea salir del sistema?",
                    "Salir",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                Conexion.cerrar();
                System.exit(0);
            }
        });

        btnModificar.addActionListener(e -> {
            ActualizarUI ventanaActualizar = new ActualizarUI();
            ventanaActualizar.setVisible(true);
        });

        add(panelFormulario);
        add(scroll);

    }
}