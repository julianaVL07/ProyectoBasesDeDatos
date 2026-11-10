package co.edu.uniquindio.proyectobasesdedatos.vista.consultas;

import co.edu.uniquindio.proyectobasesdedatos.controlador.ConsultaControlador;
import co.edu.uniquindio.proyectobasesdedatos.controlador.ConsultaControlador.Consulta;
import co.edu.uniquindio.proyectobasesdedatos.controlador.ControladorException;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.ResultadoConsulta;
import co.edu.uniquindio.proyectobasesdedatos.vista.util.UtilVista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Ventana interna para ejecutar las consultas (JOIN y subconsultas).
 *
 * <p>El usuario elige la consulta en un combo, lee su descripción, pulsa
 * <b>Ejecutar</b> y ve el resultado en una tabla con el número de filas.
 * La vista no tiene SQL: todo pasa por {@link ConsultaControlador}.</p>
 */
public class VentanaConsultas extends JInternalFrame {

    private static final DecimalFormatSymbols SIMBOLOS =
            DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO"));

    private final ConsultaControlador controlador = new ConsultaControlador();

    private final JComboBox<Consulta> cmbConsulta = new JComboBox<>(Consulta.values());
    private final JTextArea txtDescripcion = new JTextArea(3, 40);
    private final JButton btnEjecutar = new JButton("Ejecutar");
    private final JTable tabla = new JTable();
    private final JLabel lblContador = new JLabel("Filas: 0");

    /** Crea la ventana con la primera consulta seleccionada. */
    public VentanaConsultas() {
        super("Consultas", true, true, true, true);
        setSize(950, 520);

        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(crearPanelSuperior(), BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(tabla), BorderLayout.CENTER);
        getContentPane().add(crearPanelInferior(), BorderLayout.SOUTH);

        UtilVista.configurarTabla(tabla, UtilVista.crearModeloNoEditable());

        cmbConsulta.addActionListener(e -> mostrarDescripcion());
        btnEjecutar.addActionListener(e -> ejecutar());
        mostrarDescripcion();
    }

    // ----------------------------------------------------------------- Paneles

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        fila.add(new JLabel("Consulta:"));
        fila.add(cmbConsulta);
        fila.add(btnEjecutar);
        panel.add(fila, BorderLayout.NORTH);

        txtDescripcion.setEditable(false);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        txtDescripcion.setBackground(UIManager.getColor("Panel.background"));
        txtDescripcion.setBorder(BorderFactory.createTitledBorder("Descripción"));
        panel.add(txtDescripcion, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        panel.add(lblContador);
        return panel;
    }

    // ---------------------------------------------------------------- Acciones

    private void mostrarDescripcion() {
        Consulta consulta = (Consulta) cmbConsulta.getSelectedItem();
        txtDescripcion.setText(consulta == null ? "" : consulta.getDescripcion());
    }

    private void ejecutar() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            ResultadoConsulta resultado =
                    controlador.ejecutar((Consulta) cmbConsulta.getSelectedItem());
            mostrarResultado(resultado);
            if (resultado.estaVacio()) {
                UtilVista.mostrarInfo(this, "La consulta no devolvió resultados.");
            }
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /** Pasa columnas y filas del resultado a la tabla y da formato a los números. */
    private void mostrarResultado(ResultadoConsulta resultado) {
        DefaultTableModel modelo = UtilVista.crearModeloNoEditable(resultado.getColumnas());
        for (Object[] fila : resultado.getFilas()) {
            modelo.addRow(fila);
        }
        UtilVista.configurarTabla(tabla, modelo);

        for (int c = 0; c < modelo.getColumnCount(); c++) {
            if (esColumnaNumerica(modelo, c)) {
                tabla.getColumnModel().getColumn(c)
                        .setCellRenderer(new RenderizadorNumero(tieneDecimales(modelo, c)));
            }
        }
        lblContador.setText("Filas: " + resultado.getCantidadFilas());
    }

    /** Una columna es numérica si todos sus valores no nulos son BigDecimal. */
    private boolean esColumnaNumerica(DefaultTableModel modelo, int columna) {
        boolean hayValores = false;
        for (int f = 0; f < modelo.getRowCount(); f++) {
            Object valor = modelo.getValueAt(f, columna);
            if (valor != null) {
                if (!(valor instanceof BigDecimal)) {
                    return false;
                }
                hayValores = true;
            }
        }
        return hayValores;
    }

    /** Indica si algún valor de la columna tiene parte decimal (p. ej. precios o promedios). */
    private boolean tieneDecimales(DefaultTableModel modelo, int columna) {
        for (int f = 0; f < modelo.getRowCount(); f++) {
            Object valor = modelo.getValueAt(f, columna);
            if (valor instanceof BigDecimal n && n.stripTrailingZeros().scale() > 0) {
                return true;
            }
        }
        return false;
    }

    /** Muestra los números alineados a la derecha y con separador de miles. */
    private static class RenderizadorNumero extends DefaultTableCellRenderer {

        private final DecimalFormat formato;

        RenderizadorNumero(boolean conDecimales) {
            formato = new DecimalFormat(conDecimales ? "#,##0.00" : "#,##0", SIMBOLOS);
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        protected void setValue(Object valor) {
            setText(valor instanceof BigDecimal n ? formato.format(n) : (valor == null ? "" : valor.toString()));
        }
    }
}
