import Bd.Conexion;
import Bd.ProductorDAO;
import Bd.RegistroDAO;
import Models.Registro;
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
    private final List<Registro> registrosActuales = new ArrayList<>();

    // ── Combo productores ────────────────────────────────
    private JComboBox<String>        cmbNombre;
    private Integer                  selectedProductorNro = null; // Id del productor seleccionado (null si no está registrado)
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
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(900, 560));
        setIconImage(iconoApp(32));
        setLayout(new BorderLayout());
        getContentPane().setBackground(BEIGE);

        final int[] contadorCupon = {RegistroDAO.getUltimoCupon() + 1};

        // ═══════════════════════════════════════════════════
        //  PANEL IZQUIERDO  (formulario en 2 columnas)
        // ═══════════════════════════════════════════════════
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(BEIGE);

        // ── Cabecera (se estira automáticamente con BorderLayout.NORTH) ─
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(VERDE_OSCURO);

        JLabel lblTitulo = new JLabel("Balanza — Yerbatera C&M");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setIcon(new ImageIcon(iconoApp(24)));
        lblTitulo.setIconTextGap(10);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(11, 16, 11, 0));

        ImageIcon iconoUsuario = new ImageIcon(mkImg(18, 18, g -> {
            g.setColor(Color.WHITE);
            g.fillOval(5, 1, 8, 8);
            g.fillArc(1, 10, 16, 13, 0, 180);
        }));
        JLabel lblAdmin = new JLabel("Administración", iconoUsuario, JLabel.LEFT);
        lblAdmin.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblAdmin.setForeground(Color.WHITE);
        lblAdmin.setIconTextGap(8);
        lblAdmin.setBorder(BorderFactory.createEmptyBorder(11, 0, 11, 16));

        cabecera.add(lblTitulo, BorderLayout.WEST);
        cabecera.add(lblAdmin, BorderLayout.EAST);
        left.add(cabecera, BorderLayout.NORTH);

        // Panel de formulario con null layout dentro del centro
        JPanel form = new JPanel(null);
        form.setBackground(BEIGE);
        JScrollPane formScroll = new JScrollPane(form,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        left.add(formScroll, BorderLayout.CENTER);

        // Alias para no cambiar todas las llamadas left.add → form.add
        final JPanel lp = form;

        // ── Labels (referencias para layout responsive) ──────
        JLabel lblFecha     = crearLabel("Fecha");
        JLabel lblCupon     = crearLabel("Nro Cupón");
        JLabel lblNombre    = crearLabel("Nombre del Productor");
        JLabel lblBruto     = crearLabel("Peso Bruto (kg)");
        JLabel lblTara      = crearLabel("Tara (kg)");
        JLabel lblDescuento = crearLabel("Descuento (%)");
        JLabel lblRemito    = crearLabel("Nro Remito");

        // ── Campos ──────────────────────────────────────────
        JTextField txtFecha = crearCampo();
        txtFecha.setEditable(false);
        txtFecha.setBackground(VERDE_CLARO);
        txtFecha.setText(java.time.LocalDate.now().toString());

        JTextField txtCupon = crearCampo();
        txtCupon.setEditable(false);
        txtCupon.setBackground(VERDE_CLARO);
        txtCupon.setText(String.valueOf(contadorCupon[0]));

        cmbNombre = new JComboBox<>();
        cmbNombre.setEditable(true);
        cmbNombre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbNombre.setBackground(Color.WHITE);
        cmbNombre.addActionListener(e -> {
            if ("comboBoxChanged".equals(e.getActionCommand())) {
                Object sel = cmbNombre.getSelectedItem();
                String nro = sel != null ? mapaProductores.get(sel.toString()) : null;
                selectedProductorNro = nro != null ? Integer.parseInt(nro) : null;
            }
        });

        JTextField txtBruto     = crearCampo();
        JTextField txtTara      = crearCampo();

        JTextField txtDescuento = crearCampo();
        txtDescuento.setText("0");
        txtDescuento.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if ("0".equals(txtDescuento.getText())) txtDescuento.setText("");
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtDescuento.getText().isEmpty()) txtDescuento.setText("0");
            }
        });

        JTextField txtRemito = crearCampo();

        // ── Panel de cálculo ────────────────────────────────
        JPanel calcPanel = new JPanel(null);
        calcPanel.setBackground(VERDE_CLARO);
        calcPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VERDE_MEDIO, 2),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));

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

        // ── Botones ─────────────────────────────────────────
        JButton btnGuardar     = btn("Guardar",     VERDE_MEDIO,            Color.WHITE);
        JButton btnProductores = btn("Productores", VERDE_OSCURO,           Color.WHITE);
        JButton btnSalir       = btn("Salir",       new Color(155, 50, 50), Color.WHITE);

        // ── Agregar al panel (bounds los asigna el Runnable de layout) ──
        lp.add(lblFecha);     lp.add(txtFecha);
        lp.add(lblCupon);     lp.add(txtCupon);
        lp.add(lblNombre);    lp.add(cmbNombre);
        lp.add(lblBruto);     lp.add(txtBruto);
        lp.add(lblTara);      lp.add(txtTara);
        lp.add(lblDescuento); lp.add(txtDescuento);
        lp.add(lblRemito);    lp.add(txtRemito);
        lp.add(calcPanel);
        lp.add(btnGuardar);
        lp.add(btnProductores);
        lp.add(btnSalir);

        // ── Layout responsive ────────────────────────────────
        // col2 (ancho >= 520): Fecha|Cupón en misma fila, Bruto|Tara, Descuento|Remito
        // col1 (ancho <  520): todos los campos apilados verticalmente
        Runnable aplicarLayout = () -> {
            int w = formScroll.getViewport().getWidth();
            if (w == 0) return;
            boolean col2 = w >= 520;
            int mg = 15;
            int fw = w - mg * 2;
            int yl = 12;

            if (col2) {
                // ── 2 columnas ──────────────────────────────
                lblFecha.setBounds(C1, yl, 200, 18);     txtFecha.setBounds(C1, yl+22, CW, FH);
                lblCupon.setBounds(C2, yl, 200, 18);     txtCupon.setBounds(C2, yl+22, CW, FH);
                yl += RH;
                lblNombre.setBounds(C1, yl, 300, 18);    cmbNombre.setBounds(C1, yl+22, C2+CW-C1, FH);
                yl += RH;
                lblBruto.setBounds(C1, yl, 200, 18);     txtBruto.setBounds(C1, yl+22, CW, FH);
                lblTara.setBounds(C2, yl, 200, 18);      txtTara.setBounds(C2, yl+22, CW, FH);
                yl += RH;
                lblDescuento.setBounds(C1, yl, 200, 18); txtDescuento.setBounds(C1, yl+22, 130, FH);
                lblRemito.setBounds(C2, yl, 200, 18);    txtRemito.setBounds(C2, yl+22, CW, FH);
                yl += RH;
                calcPanel.setBounds(C1, yl, Math.min(fw, 625), 100); yl += 112;
                btnGuardar.setBounds(C1, yl, 180, 38);
                btnProductores.setBounds(205, yl, 180, 38);
                btnSalir.setBounds(395, yl, 140, 38);
            } else {
                // ── 1 columna ───────────────────────────────
                lblFecha.setBounds(mg, yl, fw, 18);     txtFecha.setBounds(mg, yl+22, fw, FH);     yl += RH;
                lblCupon.setBounds(mg, yl, fw, 18);     txtCupon.setBounds(mg, yl+22, fw, FH);     yl += RH;
                lblNombre.setBounds(mg, yl, fw, 18);    cmbNombre.setBounds(mg, yl+22, fw, FH);    yl += RH;
                lblBruto.setBounds(mg, yl, fw, 18);     txtBruto.setBounds(mg, yl+22, fw, FH);     yl += RH;
                lblTara.setBounds(mg, yl, fw, 18);      txtTara.setBounds(mg, yl+22, fw, FH);      yl += RH;
                lblDescuento.setBounds(mg, yl, fw, 18); txtDescuento.setBounds(mg, yl+22, 130, FH); yl += RH;
                lblRemito.setBounds(mg, yl, fw, 18);    txtRemito.setBounds(mg, yl+22, fw, FH);    yl += RH;
                calcPanel.setBounds(mg, yl, fw, 100); yl += 112;
                int bw = (fw - 8) / 3;
                btnGuardar.setBounds(mg, yl, bw, 38);
                btnProductores.setBounds(mg + bw + 4, yl, bw, 38);
                btnSalir.setBounds(mg + (bw + 4) * 2, yl, fw - (bw + 4) * 2, 38);
            }

            form.setPreferredSize(new Dimension(w, yl + 50));
            form.revalidate();
            formScroll.revalidate();
            form.repaint();
        };

        formScroll.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                aplicarLayout.run();
            }
        });
        SwingUtilities.invokeLater(aplicarLayout);

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
            "Bruto (kg)","Tara (kg)","Desc %","Neto (kg)","Remito"," ","  ","   "
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 9 || c == 10 || c == 11; }
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

        int[] anchos = {88, 52, 65, 115, 75, 70, 55, 80, 65, 36, 36, 36};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        Color cEdit  = new Color(79, 119, 45);
        Color cDel   = new Color(180, 60, 60);
        Color cPrint = new Color(60, 100, 160);

        tabla.getColumnModel().getColumn(9).setCellRenderer(new BtnRenderer("", cEdit,  iconoEditar()));
        tabla.getColumnModel().getColumn(10).setCellRenderer(new BtnRenderer("", cDel,   iconoEliminar()));
        tabla.getColumnModel().getColumn(11).setCellRenderer(new BtnRenderer("", cPrint, iconoImprimir()));

        JButton bEdit  = btnCelda("", cEdit);
        bEdit.setIcon(iconoEditar());
        JButton bDel   = btnCelda("", cDel);
        bDel.setIcon(iconoEliminar());
        JButton bPrint = btnCelda("", cPrint);
        bPrint.setIcon(iconoImprimir());

        tabla.getColumnModel().getColumn(9).setCellEditor( new BtnEditor(bEdit,  mRow -> abrirDialogoEditar(mRow)));
        tabla.getColumnModel().getColumn(10).setCellEditor(new BtnEditor(bDel,   mRow -> eliminarRegistro(mRow)));
        tabla.getColumnModel().getColumn(11).setCellEditor(new BtnEditor(bPrint, mRow -> abrirVistaPrevia(mRow)));

        // Columna 11 no ordenable
        ((TableRowSorter<?>) tabla.getRowSorter()).setSortable(11, false);

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
                selectedProductorNro = null;
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

        // Con layout responsive el panel izq puede ser mucho más angosto
        left.setMinimumSize(new Dimension(300, 400));
        right.setMinimumSize(new Dimension(250, 400));

        // JSplitPane con divisor verde — sin tocar el UI para preservar el drag
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setContinuousLayout(true);
        split.setDividerSize(4);
        split.setResizeWeight(0.55);
        SwingUtilities.invokeLater(() -> {
            if (split.getUI() instanceof javax.swing.plaf.basic.BasicSplitPaneUI) {
                ((javax.swing.plaf.basic.BasicSplitPaneUI) split.getUI())
                        .getDivider().setBackground(BORDE_CAMPO);
            }
        });
        add(split, BorderLayout.CENTER);

        cargarRegistros();
        recargarCombo();
    }

    // ─────────────────────────────────────────────────────
    //  Lógica
    // ─────────────────────────────────────────────────────

    private void cargarRegistros() {
        tableModel.setRowCount(0);
        registrosActuales.clear();
        for (Registro p : RegistroDAO.listar()) {
            registrosActuales.add(p);
            tableModel.addRow(new Object[]{
                p.getFecha(), p.getNroCupon(), p.getNroProductor(), p.getNombre(),
                p.getPesoBruto(), p.getTara(), p.getDescuento(), p.getPesoNeto(),
                p.getRemito(), "Editar", "Eliminar", "Recibo"
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
        Registro p = registrosActuales.get(mRow);

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
            // (consistencia automática; si es libre queda null)
            String nroStr = mapaProductores.get(nombre);
            Integer nroProd = nroStr != null ? Integer.parseInt(nroStr) : null;

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

    private void abrirVistaPrevia(int mRow) {
        if (mRow < 0 || mRow >= registrosActuales.size()) return;
        Registro p = registrosActuales.get(mRow);

        JDialog dlg = new JDialog(this, "Vista previa — Recibo de Pesada", true);
        dlg.setSize(380, 580);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(new Color(230, 230, 230));

        // ── Panel del recibo (blanco, estilo ticket) ──────
        JPanel recibo = new JPanel(null);
        recibo.setBackground(Color.WHITE);
        recibo.setPreferredSize(new Dimension(340, 500));
        recibo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        int ry = 18;

        // Logo / Empresa
        JLabel lEmpresa = new JLabel("YERBATERA C&M", JLabel.CENTER);
        lEmpresa.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lEmpresa.setForeground(VERDE_OSCURO);
        lEmpresa.setBounds(0, ry, 340, 26); recibo.add(lEmpresa); ry += 28;

        JLabel lSistema = new JLabel("Sistema de Balanza", JLabel.CENTER);
        lSistema.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lSistema.setForeground(Color.GRAY);
        lSistema.setBounds(0, ry, 340, 16); recibo.add(lSistema); ry += 22;

        ry = sep(recibo, ry);

        JLabel lTitulo = new JLabel("RECIBO DE PESADA", JLabel.CENTER);
        lTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lTitulo.setForeground(TEXTO_OSCURO);
        lTitulo.setBounds(0, ry, 340, 20); recibo.add(lTitulo); ry += 26;

        ry = filaRecibo(recibo, ry, "Cupón N°:",  String.format("%04d", p.getNroCupon()));
        ry = filaRecibo(recibo, ry, "Fecha:",      p.getFecha());
        ry = filaRecibo(recibo, ry, "Remito N°:",  String.valueOf(p.getRemito()));

        ry = sep(recibo, ry);

        JLabel lProd = new JLabel("PRODUCTOR", JLabel.LEFT);
        lProd.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lProd.setForeground(VERDE_OSCURO);
        lProd.setBounds(20, ry, 300, 16); recibo.add(lProd); ry += 20;

        ry = filaRecibo(recibo, ry, "Nombre:", p.getNombre());

        ry = sep(recibo, ry);

        JLabel lDet = new JLabel("DETALLE DE PESADA", JLabel.LEFT);
        lDet.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lDet.setForeground(VERDE_OSCURO);
        lDet.setBounds(20, ry, 300, 16); recibo.add(lDet); ry += 20;

        ry = filaRecibo(recibo, ry, "Peso Bruto:",  String.format("%.2f kg", p.getPesoBruto()));
        ry = filaRecibo(recibo, ry, "Tara:",         String.format("%.2f kg", p.getTara()));
        ry = filaRecibo(recibo, ry, "Descuento:",    String.format("%.1f %%",  p.getDescuento()));

        // Línea divisoria antes del neto
        JSeparator sepNeto = new JSeparator();
        sepNeto.setForeground(VERDE_MEDIO);
        sepNeto.setBounds(20, ry + 2, 300, 2); recibo.add(sepNeto); ry += 12;

        JLabel lNetoLbl = new JLabel("Peso Neto:");
        lNetoLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lNetoLbl.setForeground(VERDE_OSCURO);
        lNetoLbl.setBounds(20, ry, 160, 22); recibo.add(lNetoLbl);
        JLabel lNetoVal = new JLabel(String.format("%.2f kg", p.getPesoNeto()), JLabel.RIGHT);
        lNetoVal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lNetoVal.setForeground(VERDE_OSCURO);
        lNetoVal.setBounds(160, ry, 160, 22); recibo.add(lNetoVal); ry += 30;

        ry = sep(recibo, ry);

        // Firma
        JLabel lFirma = new JLabel("Firma: _________________________");
        lFirma.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lFirma.setForeground(Color.GRAY);
        lFirma.setBounds(20, ry, 300, 18); recibo.add(lFirma); ry += 30;

        // Ajustar altura del panel al contenido
        recibo.setPreferredSize(new Dimension(340, ry + 10));

        // ── Scroll por si el contenido es largo ──────────
        JScrollPane sp = new JScrollPane(recibo);
        sp.setBorder(BorderFactory.createEmptyBorder(12, 20, 8, 20));
        sp.getViewport().setBackground(new Color(230, 230, 230));
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dlg.add(sp, BorderLayout.CENTER);

        // ── Botones ───────────────────────────────────────
        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        panelBtn.setBackground(new Color(230, 230, 230));

        JButton btnImprimir = btn("Imprimir", VERDE_OSCURO, Color.WHITE);
        btnImprimir.setIcon(iconoImprimir());
        btnImprimir.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnImprimir.setIconTextGap(6);
        JButton btnCerrar = btn("Cerrar", new Color(120, 120, 120), Color.WHITE);

        panelBtn.add(btnImprimir);
        panelBtn.add(btnCerrar);
        dlg.add(panelBtn, BorderLayout.SOUTH);

        btnCerrar.addActionListener(e -> dlg.dispose());
        btnImprimir.addActionListener(e -> {
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            job.setPrintable((g, pf, pi) -> {
                if (pi > 0) return java.awt.print.Printable.NO_SUCH_PAGE;
                Graphics2D g2 = (Graphics2D) g;
                g2.translate(pf.getImageableX(), pf.getImageableY());
                // Escalar el recibo para que entre en la hoja
                double scaleX = pf.getImageableWidth()  / recibo.getWidth();
                double scaleY = pf.getImageableHeight() / recibo.getHeight();
                double scale  = Math.min(scaleX, scaleY);
                g2.scale(scale, scale);
                recibo.print(g2);
                return java.awt.print.Printable.PAGE_EXISTS;
            });
            if (job.printDialog()) {
                try { job.print(); }
                catch (java.awt.print.PrinterException ex) { msg("Error al imprimir: " + ex.getMessage()); }
            }
        });

        dlg.setVisible(true);
    }

    /** Fila clave–valor dentro del recibo */
    private int filaRecibo(JPanel p, int y, String clave, String valor) {
        JLabel lClave = new JLabel(clave);
        lClave.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lClave.setForeground(new Color(80, 80, 80));
        lClave.setBounds(20, y, 150, 18);
        p.add(lClave);
        JLabel lValor = new JLabel(valor, JLabel.RIGHT);
        lValor.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lValor.setForeground(TEXTO_OSCURO);
        lValor.setBounds(160, y, 160, 18);
        p.add(lValor);
        return y + 22;
    }

    /** Separador horizontal dentro del recibo */
    private int sep(JPanel p, int y) {
        JSeparator s = new JSeparator();
        s.setForeground(new Color(220, 220, 220));
        s.setBounds(20, y + 4, 300, 1);
        p.add(s);
        return y + 14;
    }

    private void eliminarRegistro(int mRow) {
        if (mRow < 0 || mRow >= registrosActuales.size()) return;
        Registro p = registrosActuales.get(mRow);
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

    /** Impresora blanca — cuerpo + papel saliendo */
    static ImageIcon iconoImprimir() {
        return new ImageIcon(mkImg(15, 15, g -> {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Cuerpo de la impresora
            g.drawRoundRect(1, 5, 13, 7, 2, 2);
            // Papel entrando (arriba)
            g.drawRect(4, 2, 7, 4);
            // Papel saliendo (abajo)
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawRect(4, 10, 7, 4);
            g.drawLine(6, 12, 10, 12);  // línea de texto simulada
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
