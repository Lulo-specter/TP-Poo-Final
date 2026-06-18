import Bd.RegistroDAO;
import Models.Producto;
import java.sql.SQLException;
import Bd.Conexion;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

public class VerProductoresUI extends JFrame {

    public VerProductoresUI() {

        setTitle("Ver Productores");
        setSize(1100, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        //----------------------------------
        // TABLA
        //----------------------------------
        String[] columnas = {
                "ID", "Fecha", "Nro Cupón", "Nro Productor",
                "Nombre", "Peso Bruto", "Tara", "Descuento", "Peso Neto", "Remito"
        };

        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // la tabla no se puede editar directamente
            }
        };

        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(25);
        tabla.setIntercellSpacing(new Dimension(10, 5));
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        tabla.getColumnModel().getColumn(1).setPreferredWidth(100);  // Fecha
        tabla.getColumnModel().getColumn(2).setPreferredWidth(80);   // Nro Cupón
        tabla.getColumnModel().getColumn(3).setPreferredWidth(100);  // Nro Productor
        tabla.getColumnModel().getColumn(4).setPreferredWidth(150);  // Nombre
        tabla.getColumnModel().getColumn(5).setPreferredWidth(90);   // Peso Bruto
        tabla.getColumnModel().getColumn(6).setPreferredWidth(80);   // Tara
        tabla.getColumnModel().getColumn(7).setPreferredWidth(80);   // Descuento
        tabla.getColumnModel().getColumn(8).setPreferredWidth(90);   // Peso Neto
        tabla.getColumnModel().getColumn(9).setPreferredWidth(80);   // Remito
        tabla.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(scroll, BorderLayout.CENTER);
        //----------------------------------
        // CARGAR DATOS
        //----------------------------------
        List<Producto> lista = RegistroDAO.listar();
        double totalDiario = 0;
        for (Producto p : lista) {
            modelo.addRow(new Object[]{
                    p.getId(),
                    p.getFecha(),
                    p.getNroCupon(),
                    p.getNroProductor(),
                    p.getNombre(),
                    p.getPesoBruto(),
                    p.getTara(),
                    p.getDescuento(),
                    p.getPesoNeto(),
                    p.getRemito()
            });
            totalDiario += p.getPesoNeto();
        }

        //----------------------------------
        // BOTÓN CERRAR
        //----------------------------------
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> {
            dispose();
            new VerProductoresUI().setVisible(true);
        });

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(e -> {

            int filaSeleccionada = tabla.getSelectedRow(); // ← cambiá por el nombre real de tu JTable

            if (filaSeleccionada == -1) {
                JOptionPane.showMessageDialog(null, "Seleccioná un productor para eliminar.");
                return;
            }

            int idProductor = (int) tabla.getModel().getValueAt(filaSeleccionada, 0); // columna 0 = id

            int confirmacion = JOptionPane.showConfirmDialog(null,
                    "¿Seguro que querés eliminar este productor?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION);

            if (confirmacion == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM Registros WHERE id = ?"; // ← cambiá "productores" por el nombre real de tu tabla

                try (PreparedStatement ps = Conexion.getInstancia().prepareStatement(sql)) {

                    ps.setInt(1, idProductor);
                    int filasAfectadas = ps.executeUpdate();

                    if (filasAfectadas > 0) {
                        JOptionPane.showMessageDialog(null, "Productor eliminado correctamente.");
                        ((DefaultTableModel) tabla.getModel()).removeRow(filaSeleccionada);
                    } else {
                        JOptionPane.showMessageDialog(null, "No se pudo eliminar el productor.");
                    }

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                }
            }
        });

        JPanel panelBoton = new JPanel(new BorderLayout());

        JLabel lblTotal = new JLabel("  Total diario: " + Math.round(totalDiario * 100.0) / 100.0 + " kg");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnCerrar);

        panelBoton.add(lblTotal, BorderLayout.WEST);
        panelBoton.add(panelBotones, BorderLayout.EAST);
        add(panelBoton, BorderLayout.SOUTH);


    }
}