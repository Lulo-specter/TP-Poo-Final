import Bd.RegistroDAO;
import Models.Producto;

import javax.swing.*;
import java.awt.*;

public class ActualizarUI extends JFrame {

    public ActualizarUI() {

        setTitle("Actualizar Registro");
        setSize(400, 800);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // cierra solo esta ventana, no todo el programa
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);

        //----------------------------------
        // CAMPOS
        JLabel lblBuscar = new JLabel("Nro Cupón a buscar:");
        lblBuscar.setBounds(20, 20, 200, 25);

        JTextField txtBuscar = new JTextField();
        txtBuscar.setBounds(20, 45, 200, 35);

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setBounds(230, 45, 100, 35);

        JSeparator separador = new JSeparator();
        separador.setBounds(20, 100, 340, 10);

        JLabel lblCupon = new JLabel("Nro Cupón");
        lblCupon.setBounds(20, 110, 200, 25);

        JTextField txtCupon = new JTextField();
        txtCupon.setBounds(20, 130, 250, 35);
        txtCupon.setEditable(false);
        txtCupon.setBackground(new Color(220, 220, 220));

        JLabel lblFecha = new JLabel("Fecha");
        lblFecha.setBounds(20, 175, 200, 25);

        JTextField txtFecha = new JTextField();
        txtFecha.setBounds(20, 195, 250, 35);

        JLabel lblNroProductor = new JLabel("Nro Productor");
        lblNroProductor.setBounds(20, 240, 200, 25);

        JTextField txtNroProductor = new JTextField();
        txtNroProductor.setBounds(20, 260, 250, 35);

        JLabel lblNombre = new JLabel("Nombre");
        lblNombre.setBounds(20, 305, 200, 25);

        JTextField txtNombre = new JTextField();
        txtNombre.setBounds(20, 325, 250, 35);

        JLabel lblBruto = new JLabel("Peso Bruto");
        lblBruto.setBounds(20, 370, 200, 25);

        JTextField txtBruto = new JTextField();
        txtBruto.setBounds(20, 390, 250, 35);

        JLabel lblTara = new JLabel("Tara");
        lblTara.setBounds(20, 435, 200, 25);

        JTextField txtTara = new JTextField();
        txtTara.setBounds(20, 455, 250, 35);

        JLabel lblDescuento = new JLabel("Descuento (%)");
        lblDescuento.setBounds(20, 500, 200, 25);

        JTextField txtDescuento = new JTextField();
        txtDescuento.setBounds(20, 520, 250, 35);

        JLabel lblRemito = new JLabel("Remito");
        lblRemito.setBounds(20, 565, 200, 25);

        JTextField txtRemito = new JTextField();
        txtRemito.setBounds(20, 585, 250, 35);

        JButton btnActualizar = new JButton("Modificar");
        btnActualizar.setBounds(20, 700, 140, 40);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(175, 700, 140, 40);
        //----------------------------------
        // AGREGAR COMPONENTES
        //----------------------------------
        add(lblBuscar);
        add(txtBuscar);
        add(btnBuscar);
        add(separador);
        add(lblCupon);
        add(txtCupon);
        add(lblNroProductor);
        add(txtNroProductor);
        add(lblNombre);
        add(txtNombre);
        add(lblBruto);
        add(txtBruto);
        add(lblTara);
        add(txtTara);
        add(lblDescuento);
        add(txtDescuento);
        add(btnActualizar);
        add(btnCancelar);
        add(lblRemito);
        add(txtRemito);
        add(lblFecha);
        add(txtFecha);

        //----------------------------------
        // EVENTO BUSCAR
        //----------------------------------
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

            int cupon = Integer.parseInt(textoCupon);
            Producto p = RegistroDAO.buscarPorCupon(cupon);

            if (p == null) {
                JOptionPane.showMessageDialog(this, "No se encontró ningún registro con ese Nro de Cupón.");
                btnActualizar.setEnabled(false);
                return;
            }

            // Cargar los datos en los campos
            txtCupon.setText(String.valueOf(p.getNroCupon()));
            txtNroProductor.setText(p.getNroProductor());
            txtNombre.setText(p.getNombre());
            txtBruto.setText(String.valueOf(p.getPesoBruto()));
            txtTara.setText(String.valueOf(p.getTara()));
            txtDescuento.setText(String.valueOf(p.getDescuento()));
            txtRemito.setText(String.valueOf(p.getRemito()));
            btnActualizar.setEnabled(true);
            txtFecha.setText(p.getFecha());
        });

        //----------------------------------
        // EVENTO ACTUALIZAR
        //----------------------------------
        btnActualizar.addActionListener(e -> {

            // Validaciones (igual que en BalanzaUI)
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
            if (txtBruto.getText().trim().isEmpty() || txtTara.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Peso Bruto y Tara no pueden estar vacíos.");
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

                int cupon = Integer.parseInt(txtCupon.getText());

                boolean exito = RegistroDAO.actualizar(
                        cupon,
                        txtNroProductor.getText().trim(),
                        txtNombre.getText().trim(),
                        bruto,
                        tara,
                        descuento,
                        Integer.parseInt(txtRemito.getText().trim()),
                        txtFecha.getText().trim()
                );

                if (exito) {
                    JOptionPane.showMessageDialog(this, "Registro actualizado correctamente.");
                    dispose(); // cierra la ventana
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo actualizar el registro.");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Peso Bruto, Tara y Descuento deben ser números.");
            }
        });

        //----------------------------------
        // EVENTO CANCELAR
        //----------------------------------
        btnCancelar.addActionListener(e -> dispose());
    }
}