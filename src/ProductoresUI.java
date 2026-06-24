import Bd.ProductorDAO;
import Models.Productor;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana de gestión de Productores (CRUD completo + kg del mes).
 * Se abre desde BalanzaUI al presionar "Productores".
 */
public class ProductoresUI extends JFrame {

    private DefaultTableModel tableModel;
    private JTable            tabla;
    private final List<Productor> productoresActuales = new ArrayList<>();

    // Colores (misma paleta que BalanzaUI)
    private static final Color VERDE_OSCURO = new Color(26, 77, 46);
    private static final Color VERDE_MEDIO  = new Color(79, 119, 45);
    private static final Color NARANJA      = new Color(196, 98, 31);
    private static final Color BEIGE        = new Color(245, 241, 232);
    private static final Color VERDE_CLARO  = new Color(232, 245, 224);
    private static final Color TEXTO_OSCURO = new Color(40, 40, 40);

    public ProductoresUI() {

        setTitle("Gestión de Productores");
        setSize(860, 560);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BEIGE);

        // ── Encabezado ───────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(VERDE_OSCURO);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel lblTitulo = new JLabel("Productores registrados");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        headerPanel.add(lblTitulo, BorderLayout.WEST);

        String mes = java.time.YearMonth.now()
                .getMonth().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("es", "AR"))
                + " " + java.time.Year.now().getValue();
        JLabel lblMes = new JLabel("Kg del mes: " + mes);
        lblMes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMes.setForeground(new Color(200, 230, 180));
        headerPanel.add(lblMes, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ── Tabla ────────────────────────────────────────
        String[] columnas = {"N°", "Nombre", "Teléfono", "Dirección", "Kg del Mes"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabla = new JTable(tableModel);
        tabla.setRowHeight(32);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setSelectionBackground(new Color(200, 230, 180));
        tabla.setSelectionForeground(TEXTO_OSCURO);

        JTableHeader header = tabla.getTableHeader();
        header.setPreferredSize(new Dimension(0, 36));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            { setOpaque(true); setHorizontalAlignment(JLabel.LEFT); }
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, f, r, c);
                setBackground(VERDE_OSCURO);
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setBorder(BorderFactory.createEmptyBorder(0, c == 0 ? 6 : 10, 0, 6));
                setHorizontalAlignment(c == 0 ? JLabel.CENTER : JLabel.LEFT);
                setText(v == null ? "" : v.toString());
                return this;
            }
        });

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (sel) { setBackground(new Color(200, 230, 180)); setForeground(TEXTO_OSCURO); }
                else      { setBackground(row % 2 == 0 ? BEIGE : Color.WHITE); setForeground(TEXTO_OSCURO); }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                // N° centrado, Kg del Mes a la derecha, resto a la izquierda
                setHorizontalAlignment(col == 0 ? JLabel.CENTER : col == 4 ? JLabel.RIGHT : JLabel.LEFT);
                return this;
            }
        });

        // Columna N° visible con ancho fijo
        tabla.getColumnModel().getColumn(0).setPreferredWidth(45);
        tabla.getColumnModel().getColumn(0).setMaxWidth(55);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(190);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(250);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(120);

        tabla.setRowSorter(new TableRowSorter<>(tableModel));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        add(scroll, BorderLayout.CENTER);

        // ── Panel de botones ─────────────────────────────
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelBotones.setBackground(BEIGE);
        panelBotones.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 210, 190)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JButton btnNuevo    = BalanzaUI.crearBoton("+ Nuevo",   VERDE_MEDIO, Color.WHITE);
        JButton btnModif    = BalanzaUI.crearBoton("Modificar", NARANJA,     Color.WHITE);
        JButton btnEliminar = BalanzaUI.crearBoton("Eliminar",  new Color(160, 50, 50), Color.WHITE);
        JButton btnCerrar   = BalanzaUI.crearBoton("Cerrar",    new Color(100, 100, 100), Color.WHITE);

        panelBotones.add(btnNuevo);
        panelBotones.add(btnModif);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnCerrar);
        add(panelBotones, BorderLayout.SOUTH);

        // ── Eventos ──────────────────────────────────────
        btnNuevo.addActionListener(e -> abrirDialogo(null));

        btnModif.addActionListener(e -> {
            int viewRow = tabla.getSelectedRow();
            if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Seleccioná un productor para modificar."); return; }
            int modelRow = tabla.convertRowIndexToModel(viewRow);
            abrirDialogo(productoresActuales.get(modelRow));
        });

        btnEliminar.addActionListener(e -> {
            int viewRow = tabla.getSelectedRow();
            if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Seleccioná un productor para eliminar."); return; }
            int modelRow = tabla.convertRowIndexToModel(viewRow);
            Productor p = productoresActuales.get(modelRow);

            int ok = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar al productor \"" + p.getNombre() + "\"?\n"
                    + "Los registros de balanza vinculados no se borrarán.",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (ok == JOptionPane.YES_OPTION) {
                if (ProductorDAO.eliminar(p.getId())) {
                    tableModel.removeRow(modelRow);
                    productoresActuales.remove(modelRow);
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el productor.");
                }
            }
        });

        btnCerrar.addActionListener(e -> dispose());

        cargarProductores();
    }

    // ─────────────────────────────────────────────────────

    private void cargarProductores() {
        tableModel.setRowCount(0);
        productoresActuales.clear();
        for (Productor p : ProductorDAO.listar()) {
            productoresActuales.add(p);
            double kg = ProductorDAO.getKgMesActual(p.getId());
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getNombre(),
                p.getTelefono(),
                p.getDireccion(),
                String.format("%.2f kg", kg)
            });
        }
    }

    /** Abre un JDialog para crear (productor == null) o editar. */
    private void abrirDialogo(Productor productor) {
        boolean esNuevo = (productor == null);
        JDialog dlg = new JDialog(this, esNuevo ? "Nuevo Productor" : "Editar Productor", true);
        dlg.setSize(380, 310);
        dlg.setLayout(null);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BEIGE);

        // Nombre
        JLabel lblNom = BalanzaUI.crearLabel("Nombre");
        lblNom.setBounds(20, 18, 320, 20);
        dlg.add(lblNom);
        JTextField txtNom = BalanzaUI.crearCampo();
        txtNom.setBounds(20, 40, 320, 30);
        if (!esNuevo) txtNom.setText(productor.getNombre());
        dlg.add(txtNom);

        // Teléfono
        JLabel lblTel = BalanzaUI.crearLabel("Teléfono");
        lblTel.setBounds(20, 84, 320, 20);
        dlg.add(lblTel);
        JTextField txtTel = BalanzaUI.crearCampo();
        txtTel.setBounds(20, 106, 320, 30);
        if (!esNuevo) txtTel.setText(productor.getTelefono());
        dlg.add(txtTel);

        // Dirección
        JLabel lblDir = BalanzaUI.crearLabel("Dirección");
        lblDir.setBounds(20, 150, 320, 20);
        dlg.add(lblDir);
        JTextField txtDir = BalanzaUI.crearCampo();
        txtDir.setBounds(20, 172, 320, 30);
        if (!esNuevo) txtDir.setText(productor.getDireccion());
        dlg.add(txtDir);

        // Botones
        JButton btnOk  = BalanzaUI.crearBoton(esNuevo ? "Crear" : "Guardar", VERDE_MEDIO, Color.WHITE);
        JButton btnCan = BalanzaUI.crearBoton("Cancelar", new Color(120, 120, 120), Color.WHITE);
        btnOk.setBounds(20,  222, 148, 36);
        btnCan.setBounds(180, 222, 148, 36);
        dlg.add(btnOk);
        dlg.add(btnCan);

        btnCan.addActionListener(ev -> dlg.dispose());

        btnOk.addActionListener(ev -> {
            String nom = txtNom.getText().trim();
            String tel = txtTel.getText().trim();
            String dir = txtDir.getText().trim();

            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "El Nombre es obligatorio."); return;
            }
            if (!nom.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
                JOptionPane.showMessageDialog(dlg, "El Nombre solo puede contener letras."); return;
            }

            boolean ok = esNuevo
                ? ProductorDAO.insertar(nom, tel, dir)
                : ProductorDAO.actualizar(productor.getId(), nom, tel, dir);

            if (ok) { dlg.dispose(); cargarProductores(); }
            else    { JOptionPane.showMessageDialog(dlg, "No se pudo guardar el productor."); }
        });

        dlg.setVisible(true);
    }
}
