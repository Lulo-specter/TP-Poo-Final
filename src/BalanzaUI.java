import Bd.Conexion;
import Bd.ProductorDAO;
import Bd.RegistroDAO;
import Models.Producto;
import Models.Productor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class BalanzaUI extends JFrame {

    // ── Paleta yerba mate ────────────────────────────────
    static final Color VERDE_OSCURO = new Color(26,  77,  46);
    static final Color VERDE_MEDIO  = new Color(79,  119, 45);
    static final Color NARANJA      = new Color(196, 98,  31);
    static final Color BEIGE        = new Color(245, 241, 232);
    static final Color VERDE_CLARO  = new Color(232, 245, 224);
    static final Color BORDE_CAMPO  = new Color(150, 190, 100);
    static final Color TEXTO_OSCURO = new Color(40,  40,  40);

    // ── Estado tabla ─────────────────────────────────────
    private DefaultTableModel    tableModel;
    private JTable               tabla;
    private final List<Producto> registrosActuales = new ArrayList<>();

    // ── Combo productores ────────────────────────────────
    private JComboBox<String>        cmbNombre;
    private String                   selectedProductorNro = ""; // Id del productor seleccionado (vacío si no está registrado)
    private final Map<String,String> mapaProductores = new HashMap<>();

    // ── Cálculo en tiempo real ───────────────────────────
    private JLabel lblNetoCalc;
    private JLabel lblDetalleCalc;

    // Layout de 2 columnas para el formulario
    private static final int C1 = 15;    // x columna izquierda
    private static final int C2 = 345;   // x columna derecha
    private static final int CW = 295;   // ancho de cada columna
    private static final int FH = 30;    // alto de campo
    private static final int RH = 62;    // alto de fila (label+campo+gap)

    // ─────────────────────────────────────────────────────
    public BalanzaUI() {

        setTitle("Yerbatera C&M — Sistema de Balanza");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);   // ocupa toda la pantalla
        setMinimumSize(new Dimension(1100, 600));
        setIconImage(iconoApp(32));
        setLayout(new GridLayout(1, 2, 6, 0));
        getContentPane().setBackground(BEIGE);

        final int[] contadorCupon = {RegistroDAO.getUltimoCupon() + 1};

        // ═══════════════════════════════════════════════════
        //  PANEL IZQUIERDO  (formulario en 2 columnas)
        // ═══════════════════════════════════════════════════
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(BEIGE);

        // ── Cabecera (se estira automáticamente con BorderLayout.NORTH) ─
        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 11));
        cabecera.setBackground(VERDE_OSCURO);
        JLabel lblTitulo = new JLabel("Balanza — Yerbatera C&M");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setIcon(new ImageIcon(iconoApp(24)));
        lblTitulo.setIconTextGap(10);
        cabecera.add(lblTitulo);
        left.add(cabecera, BorderLayout.NORTH);

        // Panel de formulario con null layout dentro del centro
        JPanel form = new JPanel(null);
        form.setBackground(BEIGE);
        left.add(form, BorderLayout.CENTER);

        // Alias para no cambiar todas las llamadas left.add → form.add
        final JPanel lp = form;

        // ── Campos en cuadrícula 2×4 ────────────────────
        int y = 12;   // primera fila dentro de form

        // Fila 1: Fecha | Nro Cupón
        lp.add(lbl("Fecha", C1, y));
        JTextField txtFecha = campo(lp, C1, y + 22, CW, FH);
        txtFecha.setEditable(false);
        txtFecha.setBackground(VERDE_CLARO);
        txtFecha.setText(java.time.LocalDate.now().toString());

        lp.add(lbl("Nro Cupón", C2, y));
        JTextField txtCupon = campo(lp, C2, y + 22, CW, FH);
        txtCupon.setEditable(false);
        txtCupon.setBackground(VERDE_CLARO);
        txtCupon.setText(String.valueOf(contadorCupon[0]));
        y += RH;

        // Fila 2: Nombre del Productor (fila completa)
        lp.add(lbl("Nombre del Productor", C1, y));
        cmbNombre = new JComboBox<>();
        cmbNombre.setEditable(true);
        cmbNombre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbNombre.setBounds(C1, y + 22, C2 + CW - C1, FH); // ancho completo de la fila
        cmbNombre.setBackground(Color.WHITE);
        lp.add(cmbNombre);

        // Cuando se selecciona un productor registrado, guarda su Id internamente
        cmbNombre.addActionListener(e -> {
            if ("comboBoxChanged".equals(e.getActionCommand())) {
                Object sel = cmbNombre.getSelectedItem();
                String nro = sel != null ? mapaProductores.get(sel.toString()) : null;
                selectedProductorNro = nro != null ? nro : "";
            }
        });
        y += RH;

        // Fila 3: Peso Bruto | Tara
        lp.add(lbl("Peso Bruto (kg)", C1, y));
        JTextField txtBruto = campo(lp, C1, y + 22, CW, FH);

        lp.add(lbl("Tara (kg)", C2, y));
        JTextField txtTara = campo(lp, C2, y + 22, CW, FH);
        y += RH;

        // Fila 4: Descuento | Remito
        lp.add(lbl("Descuento (%)", C1, y));
        JTextField txtDescuento = campo(lp, C1, y + 22, 130, FH);
        txtDescuento.setText("0");
        txtDescuento.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if ("0".equals(txtDescuento.getText())) txtDescuento.setText("");
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtDescuento.getText().isEmpty()) txtDescuento.setText("0");
            }
        });

        lp.add(lbl("Nro Remito", C2, y));
        JTextField txtRemito = campo(lp, C2, y + 22, CW, FH);
        y += RH;

        // ── Panel de cálculo ────────────────────────────
        JPanel calcPanel = new JPanel(null);
        calcPanel.setBackground(VERDE_CLARO);
        calcPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VERDE_MEDIO, 2),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        calcPanel.setBounds(C1, y, 625, 100);
        lp.add(calcPanel);

        JLabel lCalcTit = lbl("Cálculo en tiempo real", 0, 6);
        calcPanel.add(lCalcTit);

        lblDetalleCalc = new JLabel("Bruto: — kg   |   Tara: — kg   |   Descuento: — %");
        lblDetalleCalc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetalleCalc.setForeground(new Color(60, 90, 60));
        lblDetalleCalc.setBounds(0, 28, 600, 18);
        calcPanel.add(lblDetalleCalc);

        lblNetoCalc = new JLabel("Peso Neto:  — kg");
        lblNetoCalc.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblNetoCalc.setForeground(VERDE_OSCURO);
        lblNetoCalc.setBounds(0, 52, 600, 36);
        calcPanel.add(lblNetoCalc);

        // DocumentListener para actualizar el cálculo
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { actualizarCalculo(txtBruto, txtTara, txtDescuento); }
            public void removeUpdate(DocumentEvent e)  { actualizarCalculo(txtBruto, txtTara, txtDescuento); }
            public void changedUpdate(DocumentEvent e) { actualizarCalculo(txtBruto, txtTara, txtDescuento); }
        };
        txtBruto.getDocument().addDocumentListener(dl);
        txtTara.getDocument().addDocumentListener(dl);
        txtDescuento.getDocument().addDocumentListener(dl);

        y += 112;

        // ── Botones ─────────────────────────────────────
        JButton btnGuardar     = btn("Guardar",     VERDE_MEDIO,            Color.WHITE);
        JButton btnProductores = btn("Productores", VERDE_OSCURO,           Color.WHITE);
        JButton btnSalir       = btn("Salir",       new Color(155, 50, 50), Color.WHITE);
        btnGuardar.setBounds(C1,  y, 180, 38);
        btnProductores.setBounds(205, y, 180, 38);
        btnSalir.setBounds(395,   y, 140, 38);
        lp.add(btnGuardar);
        lp.add(btnProductores);
        lp.add(btnSalir);

        // ═══════════════════════════════════════════════════
        //  PANEL DERECHO  (tabla de registros)
        // ═══════════════════════════════════════════════════
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Color.WHITE);

        JPanel rHeader = new JPanel(new BorderLayout());
        rHeader.setBackground(VERDE_OSCURO);
        rHeader.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel lblRT = new JLabel("Registros de la Balanza");
        lblRT.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblRT.setForeground(Color.WHITE);
        rHeader.add(lblRT, BorderLayout.WEST);
        right.add(rHeader, BorderLayout.NORTH);

        // Tabla
        String[] cols = {
            "Fecha","Cupón","N° Prod","Nombre",
            "Bruto (kg)","Tara (kg)","Desc %","Neto (kg)","Remito"," ","  "
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 9 || c == 10; }
        };

        tabla = new JTable(tableModel);
        tabla.setRowHeight(34);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setSelectionBackground(new Color(200, 230, 180));
        tabla.setSelectionForeground(TEXTO_OSCURO);

        // Sorter que cicla ASC ↔ DESC sin pasar por "sin orden"
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel) {
            @Override public void toggleSortOrder(int col) {
                List<? extends RowSorter.SortKey> keys = getSortKeys();
                SortOrder actual = SortOrder.UNSORTED;
                for (RowSorter.SortKey k : keys)
                    if (k.getColumn() == col) { actual = k.getSortOrder(); break; }
                SortOrder next = actual == SortOrder.ASCENDING ? SortOrder.DESCENDING : SortOrder.ASCENDING;
                setSortKeys(List.of(new RowSorter.SortKey(col, next)));
            }
        };
        sorter.setSortable(9, false);
        sorter.setSortable(10, false);
        // Orden por defecto: Fecha descendente (más reciente arriba)
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.DESCENDING)));
        tabla.setRowSorter(sorter);

        // Header con flechas ▼ (ASC) y ▲ (DESC)
        JTableHeader th = tabla.getTableHeader();
        th.setPreferredSize(new Dimension(0, 36));
        th.setReorderingAllowed(false);
        th.setDefaultRenderer(new DefaultTableCellRenderer() {
            { setOpaque(true); setHorizontalAlignment(JLabel.LEFT); }
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, f, r, c);
                setBackground(VERDE_OSCURO);
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                String arrow = "";
                if (t.getRowSorter() != null)
                    for (RowSorter.SortKey sk : t.getRowSorter().getSortKeys())
                        if (sk.getColumn() == c) {
                            arrow = sk.getSortOrder() == SortOrder.ASCENDING ? " ▼" : " ▲";
                            break;
                        }
                String txt = v == null ? "" : v.toString().trim();
                setText(txt.isEmpty() ? "" : txt + arrow);
                return this;
            }
        });

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (sel) { setBackground(new Color(200, 230, 180)); setForeground(TEXTO_OSCURO); }
                else      { setBackground(row % 2 == 0 ? BEIGE : Color.WHITE); setForeground(TEXTO_OSCURO); }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                setHorizontalAlignment(col >= 4 && col <= 8 ? JLabel.RIGHT : JLabel.LEFT);
                return this;
            }
        });

        int[] anchos = {88, 52, 65, 115, 75, 70, 55, 80, 65, 70, 75};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        Color cEdit = new Color(79, 119, 45);
        Color cDel  = new Color(180, 60, 60);
        tabla.getColumnModel().getColumn(9).setCellRenderer(new BtnRenderer("Editar",   cEdit, iconoEditar()));
        tabla.getColumnModel().getColumn(10).setCellRenderer(new BtnRenderer("Eliminar", cDel,  iconoEliminar()));

        JButton bEdit = btnCelda("Editar",   cEdit);
        bEdit.setIcon(iconoEditar());
        bEdit.setHorizontalTextPosition(SwingConstants.RIGHT);
        bEdit.setIconTextGap(4);
        JButton bDel  = btnCelda("Eliminar", cDel);
        bDel.setIcon(iconoEliminar());
        bDel.setHorizontalTextPosition(SwingConstants.RIGHT);
        bDel.setIconTextGap(4);
        tabla.getColumnModel().getColumn(9).setCellEditor(new BtnEditor(bEdit, mRow -> abrirDialogoEditar(mRow)));
        tabla.getColumnModel().getColumn(10).setCellEditor(new BtnEditor(bDel,  mRow -> eliminarRegistro(mRow)));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        right.add(scroll, BorderLayout.CENTER);

        // ═══════════════════════════════════════════════════
        //  EVENTOS
        // ═══════════════════════════════════════════════════

        btnGuardar.addActionListener(e -> {
            String nombre = comboText();

            if (nombre.isEmpty()) { msg("El Nombre del Productor no puede estar vacío."); return; }
            if (!nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) { msg("El Nombre solo puede contener letras."); return; }
            if (txtBruto.getText().trim().isEmpty())  { msg("El Peso Bruto no puede estar vacío."); return; }
            if (txtTara.getText().trim().isEmpty())   { msg("La Tara no puede estar vacía."); return; }
            if (txtRemito.getText().trim().isEmpty()) { msg("El Remito no puede estar vacío."); return; }
            if (!txtRemito.getText().trim().matches("\\d+")) { msg("El Remito solo puede contener números."); return; }
            int remitoVal = Integer.parseInt(txtRemito.getText().trim());
            if (remitoVal <= 0) { msg("El Remito debe ser mayor a 0."); return; }
            if (RegistroDAO.remitoExiste(remitoVal)) { msg("Ya existe un registro con ese número de Remito."); return; }

            try {
                double bruto = Double.parseDouble(txtBruto.getText().replace(",", "."));
                double tara  = Double.parseDouble(txtTara.getText().replace(",", "."));
                double desc  = Double.parseDouble(txtDescuento.getText().replace(",", "."));

                if (bruto <= 0)           { msg("El Peso Bruto debe ser mayor a 0."); return; }
                if (tara < 0)             { msg("La Tara no puede ser negativa."); return; }
                if (tara >= bruto)        { msg("La Tara no puede ser mayor o igual al Peso Bruto."); return; }
                if (desc < 0 || desc > 100) { msg("El Descuento debe estar entre 0 y 100."); return; }

                double neto = Math.round(((bruto - tara) - (bruto - tara) * desc / 100) * 100.0) / 100.0;
                int cupon   = Integer.parseInt(txtCupon.getText());

                // selectedProductorNro = Id del productor si fue seleccionado del combo, "" si es nuevo
                RegistroDAO.insertar(txtFecha.getText(), cupon, selectedProductorNro, nombre, bruto, tara, desc, neto, remitoVal);
                cargarRegistros();

                contadorCupon[0]++;
                txtCupon.setText(String.valueOf(contadorCupon[0]));
                txtFecha.setText(java.time.LocalDate.now().toString());
                selectedProductorNro = "";
                resetCombo();
                limpiarCampo(txtBruto, txtTara, txtRemito);
                txtDescuento.setText("0");
                actualizarCalculo(txtBruto, txtTara, txtDescuento);

            } catch (NumberFormatException ex) {
                msg("Peso Bruto, Tara y Descuento deben ser números.");
            }
        });

        btnProductores.addActionListener(e -> {
            ProductoresUI v = new ProductoresUI();
            v.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent we) { recargarCombo(); }
            });
            v.setVisible(true);
        });

        btnSalir.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "¿Desea salir del sistema?", "Salir",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                Conexion.cerrar();
                System.exit(0);
            }
        });

        add(left);
        add(right);

        cargarRegistros();
        recargarCombo();
    }

    // ─────────────────────────────────────────────────────
    //  Lógica
    // ─────────────────────────────────────────────────────

    private void cargarRegistros() {
        tableModel.setRowCount(0);
        registrosActuales.clear();
        for (Producto p : RegistroDAO.listar()) {
            registrosActuales.add(p);
            tableModel.addRow(new Object[]{
                p.getFecha(), p.getNroCupon(), p.getNroProductor(), p.getNombre(),
                p.getPesoBruto(), p.getTara(), p.getDescuento(), p.getPesoNeto(),
                p.getRemito(), "Editar", "Eliminar"
            });
        }
    }

    private void recargarCombo() {
        mapaProductores.clear();
        cmbNombre.removeAllItems();
        for (Productor p : ProductorDAO.listar()) {
            cmbNombre.addItem(p.getNombre());
            mapaProductores.put(p.getNombre(), String.valueOf(p.getId()));
        }
        resetCombo();
    }

    private void resetCombo() {
        cmbNombre.setSelectedIndex(-1);
        ((JTextField) cmbNombre.getEditor().getEditorComponent()).setText("");
    }

    private String comboText() {
        return ((JTextField) cmbNombre.getEditor().getEditorComponent()).getText().trim();
    }

    private void actualizarCalculo(JTextField b, JTextField t, JTextField d) {
        try {
            double bv = b.getText().isEmpty() ? 0 : Double.parseDouble(b.getText().replace(",","."));
            double tv = t.getText().isEmpty() ? 0 : Double.parseDouble(t.getText().replace(",","."));
            double dv = d.getText().isEmpty() ? 0 : Double.parseDouble(d.getText().replace(",","."));
            double n  = Math.round(((bv-tv)-(bv-tv)*dv/100)*100.0)/100.0;
            lblDetalleCalc.setText(String.format("Bruto: %.2f kg   |   Tara: %.2f kg   |   Descuento: %.1f %%", bv, tv, dv));
            lblNetoCalc.setText(String.format("Peso Neto:  %.2f kg", n));
            lblNetoCalc.setForeground(n >= 0 ? VERDE_OSCURO : new Color(180,60,60));
        } catch (NumberFormatException ex) {
            lblDetalleCalc.setText("Ingresá valores numéricos.");
            lblNetoCalc.setText("Peso Neto:  — kg");
            lblNetoCalc.setForeground(VERDE_OSCURO);
        }
    }

    private void abrirDialogoEditar(int mRow) {
        if (mRow < 0 || mRow >= registrosActuales.size()) return;
        Producto p = registrosActuales.get(mRow);

        JDialog dlg = new JDialog(this, "Editar — Cupón " + p.getNroCupon(), true);
        dlg.setSize(400, 510);
        dlg.setLayout(null);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BEIGE);

        int dy = 14;

        // Fecha
        JTextField tFecha = dlgCampo(dlg, "Fecha", dy, p.getFecha()); dy += 60;

        // ── Nombre del Productor: combo editable igual que el form principal ──
        JLabel lblNom = crearLabel("Nombre del Productor");
        lblNom.setBounds(20, dy, 350, 18);
        dlg.add(lblNom);

        JComboBox<String> cmbDlg = new JComboBox<>();
        cmbDlg.setEditable(true);
        cmbDlg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbDlg.setBounds(20, dy + 21, 350, 28);
        cmbDlg.setBackground(Color.WHITE);
        for (Productor prod : ProductorDAO.listar()) cmbDlg.addItem(prod.getNombre());
        dlg.add(cmbDlg);

        // Pre-cargar el nombre actual; si coincide con un productor registrado lo selecciona
        String nombreActual = p.getNombre();
        if (mapaProductores.containsKey(nombreActual)) {
            cmbDlg.setSelectedItem(nombreActual);
        } else {
            cmbDlg.setSelectedIndex(-1);
            ((JTextField) cmbDlg.getEditor().getEditorComponent()).setText(nombreActual);
        }
        dy += 60;

        JTextField tBruto  = dlgCampo(dlg, "Peso Bruto (kg)", dy, String.valueOf(p.getPesoBruto())); dy += 60;
        JTextField tTara   = dlgCampo(dlg, "Tara (kg)",       dy, String.valueOf(p.getTara()));       dy += 60;
        JTextField tDesc   = dlgCampo(dlg, "Descuento (%)",   dy, String.valueOf(p.getDescuento()));  dy += 60;
        JTextField tRemito = dlgCampo(dlg, "Remito",          dy, String.valueOf(p.getRemito()));     dy += 68;

        JButton ok  = btn("Guardar",  VERDE_MEDIO,            Color.WHITE);
        JButton can = btn("Cancelar", new Color(120,120,120), Color.WHITE);
        ok.setBounds(20, dy, 165, 36); can.setBounds(195, dy, 165, 36);
        dlg.add(ok); dlg.add(can);
        can.addActionListener(ev -> dlg.dispose());

        ok.addActionListener(ev -> {
            // El nombre viene del editor del combo (permite texto libre)
            String nombre = ((JTextField) cmbDlg.getEditor().getEditorComponent()).getText().trim();
            if (nombre.isEmpty()) { msg("El Nombre no puede estar vacío."); return; }

            // Si el nombre coincide con un productor registrado, se usa su Id como Nro_Productor
            // (consistencia automática; si es libre queda "")
            String nroProd = mapaProductores.getOrDefault(nombre, "");

            try {
                double br = Double.parseDouble(tBruto.getText().replace(",", "."));
                double ta = Double.parseDouble(tTara.getText().replace(",", "."));
                double de = Double.parseDouble(tDesc.getText().replace(",", "."));
                int    re = Integer.parseInt(tRemito.getText().trim());
                if (br <= 0 || ta < 0 || ta >= br) { msg("Valores de peso inválidos."); return; }
                if (RegistroDAO.actualizar(p.getNroCupon(), nroProd, nombre, br, ta, de, re,
                        tFecha.getText().trim())) {
                    dlg.dispose(); cargarRegistros();
                } else { msg("No se pudo actualizar."); }
            } catch (NumberFormatException ex) { msg("Revise los campos numéricos."); }
        });

        dlg.setVisible(true);
    }

    private void eliminarRegistro(int mRow) {
        if (mRow < 0 || mRow >= registrosActuales.size()) return;
        Producto p = registrosActuales.get(mRow);
        if (JOptionPane.showConfirmDialog(this,
                "¿Eliminar cupón " + p.getNroCupon() + " de " + p.getNombre() + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            if (RegistroDAO.eliminar(p.getId())) {
                tableModel.removeRow(mRow);
                registrosActuales.remove(mRow);
            } else { msg("No se pudo eliminar."); }
        }
    }

    private void msg(String m) { JOptionPane.showMessageDialog(this, m); }

    private void limpiarCampo(JTextField... fields) {
        for (JTextField f : fields) f.setText("");
    }

    // ─────────────────────────────────────────────────────
    //  Helpers UI  (accesibles desde otras clases)
    // ─────────────────────────────────────────────────────

    /** Label estilizado con posición */
    static JLabel lbl(String texto, int x, int y) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(VERDE_OSCURO);
        l.setBounds(x, y, 300, 18);
        return l;
    }

    /** Label estilizado sin posición (para usar en null layout con setBounds propio) */
    static JLabel crearLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(VERDE_OSCURO);
        return l;
    }

    /** Campo de texto estilizado, agregado al panel */
    private JTextField campo(JPanel p, int x, int y, int w, int h) {
        JTextField tf = crearCampo();
        tf.setBounds(x, y, w, h);
        p.add(tf);
        return tf;
    }

    /** Label + campo en un JDialog */
    private JTextField dlgCampo(JDialog dlg, String titulo, int y, String val) {
        JLabel l = new JLabel(titulo);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(VERDE_OSCURO);
        l.setBounds(20, y, 350, 18);
        dlg.add(l);
        JTextField tf = crearCampo();
        tf.setBounds(20, y + 21, 350, 28);
        tf.setText(val);
        dlg.add(tf);
        return tf;
    }

    /** Botón en celda de tabla */
    private JButton btnCelda(String texto, Color bg) {
        JButton b = new JButton(texto);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setOpaque(true); b.setBorderPainted(false); b.setFocusPainted(false);
        return b;
    }

    static JTextField crearCampo() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(TEXTO_OSCURO);
        tf.setBackground(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_CAMPO, 1),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        return tf;
    }

    static JButton crearBoton(String t, Color bg, Color fg) { return btn(t, bg, fg); }

    static JButton btn(String t, Color bg, Color fg) {
        JButton b = new JButton(t);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg); b.setForeground(fg);
        b.setOpaque(true); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ─────────────────────────────────────────────────────
    //  Clases internas — botones en JTable
    // ─────────────────────────────────────────────────────

    static class BtnRenderer extends JButton implements TableCellRenderer {
        private final Color base;
        BtnRenderer(String t, Color bg, Icon icon) {
            setText(t); setIcon(icon);
            base = bg; setOpaque(true); setBackground(bg);
            setForeground(Color.WHITE); setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setHorizontalTextPosition(SwingConstants.RIGHT);
            setIconTextGap(4);
        }
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean f, int r, int c) {
            setBackground(sel ? base.darker() : base); return this;
        }
    }

    class BtnEditor extends DefaultCellEditor {
        private final JButton btn;
        private int mRow;
        BtnEditor(JButton b, Consumer<Integer> onClick) {
            super(new JCheckBox());
            btn = b; setClickCountToStart(1);
            b.addActionListener(e -> { fireEditingStopped(); onClick.accept(mRow); });
        }
        @Override public Component getTableCellEditorComponent(
                JTable t, Object v, boolean sel, int row, int col) {
            mRow = t.convertRowIndexToModel(row); return btn;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }

    // ─────────────────────────────────────────────────────
    //  Iconos programáticos (sin archivos externos)
    // ─────────────────────────────────────────────────────

    /** Dibuja en un BufferedImage transparente usando Graphics2D. */
    private static BufferedImage mkImg(int w, int h, Consumer<Graphics2D> fn) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        fn.accept(g);
        g.dispose();
        return img;
    }

    /** Lápiz blanco — trazos simples, sin relleno */
    static ImageIcon iconoEditar() {
        return new ImageIcon(mkImg(15, 15, g -> {
            g.setColor(Color.WHITE);
            BasicStroke s = new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g.setStroke(s);
            // Cuerpo diagonal del lápiz
            g.drawLine(3, 12, 11, 4);
            g.drawLine(5, 14, 12, 6);
            g.drawLine(11, 4, 12, 6);   // tope
            g.drawLine(3, 12, 5, 14);   // base
            // Punta (triángulo pequeño)
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(1, 14, 3, 12);
            g.drawLine(1, 14, 5, 14);
        }));
    }

    /** Basurero blanco — cuerpo + tapa + asa */
    static ImageIcon iconoEliminar() {
        return new ImageIcon(mkImg(15, 15, g -> {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Cuerpo del basurero (trapecio simplificado como rect)
            g.drawRect(2, 5, 11, 9);
            // Tapa
            g.drawLine(1, 4, 14, 4);
            // Asa
            g.drawLine(5, 4, 5, 2);
            g.drawLine(10, 4, 10, 2);
            g.drawLine(5, 2, 10, 2);
            // Líneas internas
            g.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(5, 7, 5, 12);
            g.drawLine(7, 7, 7, 12);
            g.drawLine(10, 7, 10, 12);
        }));
    }

    /**
     * Icono de la app: círculo verde oscuro con una balanza blanca.
     * Usado en el JFrame (taskbar) y en el JLabel del header.
     */
    static BufferedImage iconoApp(int size) {
        return mkImg(size, size, g -> {
            // Fondo circular verde
            g.setColor(VERDE_OSCURO);
            g.fillOval(0, 0, size, size);
            g.setColor(Color.WHITE);
            float sw = Math.max(1.2f, size / 14f);
            g.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int p = size / 7;         // padding interior
            int cx = size / 2;
            int topY  = p + 1;
            int botY  = size - p - 1;
            int barW  = size / 2 - p; // semi-ancho de la barra horizontal
            int platY = size / 2 + 2; // altura de los platillos

            // Mástil central
            g.drawLine(cx, topY, cx, botY);
            // Barra horizontal
            g.drawLine(cx - barW, topY + 2, cx + barW, topY + 2);
            // Cadenas izq y der
            g.drawLine(cx - barW, topY + 2, cx - barW + 3, platY);
            g.drawLine(cx + barW, topY + 2, cx + barW - 3, platY);
            // Platillos (arco abierto hacia abajo)
            int pw = barW - 2;
            g.drawArc(cx - barW + 3 - pw/2, platY, pw, pw / 2, 0, -180);
            g.drawArc(cx + barW - 3 - pw/2, platY, pw, pw / 2, 0, -180);
            // Base
            g.drawLine(cx - barW / 2, botY, cx + barW / 2, botY);
        });
    }
}
