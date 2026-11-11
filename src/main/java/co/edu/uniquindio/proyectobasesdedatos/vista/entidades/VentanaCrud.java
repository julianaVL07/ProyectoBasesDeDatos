package co.edu.uniquindio.proyectobasesdedatos.vista.entidades;

import co.edu.uniquindio.proyectobasesdedatos.controlador.ControladorException;
import co.edu.uniquindio.proyectobasesdedatos.util.Sesion;
import co.edu.uniquindio.proyectobasesdedatos.vista.util.UtilVista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana interna base para los CRUD del módulo Entidades.
 *
 * <p>Arma la estructura común de todas las ventanas:</p>
 * <ul>
 *   <li>Arriba: el formulario (lo define cada subclase) y los botones
 *       <b>Nuevo, Guardar, Actualizar, Eliminar, Limpiar</b>.</li>
 *   <li>Abajo: el campo <b>Buscar</b> y una {@link JTable} no editable.</li>
 * </ul>
 *
 * <p>Comportamiento común: clic en una fila carga el formulario; Eliminar pide
 * confirmación; los errores se muestran con {@code JOptionPane} usando el
 * mensaje de la {@link ControladorException}; y los botones de escritura
 * quedan deshabilitados si el rol no puede editar ({@link Sesion#puedeEditar()}).</p>
 *
 * <p>Las subclases construyen sus campos y al final de su constructor llaman
 * {@link #inicializar()} (no se puede hacer desde aquí porque los campos de
 * la subclase aún no existen mientras corre el constructor de esta clase).</p>
 *
 * @param <T> DTO que se edita en la ventana
 */
public abstract class VentanaCrud<T> extends JInternalFrame {

    protected final JButton btnNuevo = new JButton("Nuevo");
    protected final JButton btnGuardar = new JButton("Guardar");
    protected final JButton btnActualizar = new JButton("Actualizar");
    protected final JButton btnEliminar = new JButton("Eliminar");
    protected final JButton btnLimpiar = new JButton("Limpiar");

    private final JTextField txtBuscar = new JTextField(25);
    private final JButton btnBuscar = new JButton("Buscar");

    private final JTable tabla = new JTable();
    private final DefaultTableModel modelo;
    private final JLabel lblTotal = new JLabel("Registros: 0");

    /** Filas que se muestran en la tabla (mismo orden que el modelo). */
    private List<T> datos = new ArrayList<>();

    /** Registro cargado en el formulario; null cuando se está creando uno nuevo. */
    private T seleccionado;

    /**
     * @param titulo   título de la ventana
     * @param columnas títulos de las columnas de la tabla
     */
    protected VentanaCrud(String titulo, String... columnas) {
        super(titulo, true, true, true, true);
        modelo = UtilVista.crearModeloNoEditable(columnas);
    }

    // ------------------------------------------------- Lo que define cada CRUD

    /** Panel con los campos del formulario (normalmente con {@code GridBagLayout}). */
    protected abstract JPanel crearFormulario();

    /** Consulta los registros que coinciden con el texto (vacío = todos). */
    protected abstract List<T> buscar(String texto) throws ControladorException;

    /** Valores de la fila de la tabla para un registro. */
    protected abstract Object[] aFila(T registro);

    /** Pasa los datos del registro a los campos del formulario. */
    protected abstract void cargarFormulario(T registro);

    /** Deja los campos del formulario vacíos. */
    protected abstract void limpiarFormulario();

    /**
     * Arma un DTO con lo escrito en el formulario. Si hay un registro
     * seleccionado debe conservar su código (para Actualizar).
     *
     * @throws ControladorException si un campo numérico no se puede convertir
     */
    protected abstract T leerFormulario() throws ControladorException;

    protected abstract void guardar(T registro) throws ControladorException;

    protected abstract void actualizar(T registro) throws ControladorException;

    protected abstract void eliminar(T registro) throws ControladorException;

    /** Texto para la confirmación de borrado, p. ej. "la categoría «Aseo»". */
    protected abstract String describir(T registro);

    // ------------------------------------------------------------ Construcción

    /** Arma la ventana y carga la tabla. Las subclases lo llaman al final de su constructor. */
    protected void inicializar() {
        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(crearPanelSuperior(), BorderLayout.NORTH);
        getContentPane().add(crearPanelTabla(), BorderLayout.CENTER);

        UtilVista.configurarTabla(tabla, modelo);
        configurarColumnas(tabla);

        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> accionGuardar());
        btnActualizar.addActionListener(e -> accionActualizar());
        btnEliminar.addActionListener(e -> accionEliminar());
        btnLimpiar.addActionListener(e -> limpiarTodo());
        btnBuscar.addActionListener(e -> recargar());
        txtBuscar.addActionListener(e -> recargar()); // Enter en el campo Buscar

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFila();
            }
        });

        if (getWidth() == 0) {
            pack();
        }
        actualizarBotones();
        recargar();
    }

    /** Punto de extensión para ajustar anchos o alineación de columnas. */
    protected void configurarColumnas(JTable tabla) {
        // Por defecto no se cambia nada.
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        JPanel formulario = crearFormulario();
        formulario.setBorder(BorderFactory.createTitledBorder("Datos"));
        panel.add(formulario, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        JPanel buscar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buscar.add(new JLabel("Buscar:"));
        buscar.add(txtBuscar);
        buscar.add(btnBuscar);
        panel.add(buscar, BorderLayout.NORTH);

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(lblTotal, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Restricciones de {@code GridBagLayout} para la celda (columna, fila) del
     * formulario. Las columnas impares (los campos) crecen a lo ancho.
     */
    protected static GridBagConstraints celda(int columna, int fila) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = columna;
        g.gridy = fila;
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        boolean esCampo = columna % 2 == 1;
        g.fill = esCampo ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        g.weightx = esCampo ? 1.0 : 0.0;
        return g;
    }

    // ---------------------------------------------------------------- Acciones

    /** Vuelve a consultar la tabla con el texto del campo Buscar. */
    protected void recargar() {
        try {
            datos = buscar(txtBuscar.getText());
        } catch (ControladorException e) {
            datos = new ArrayList<>();
            UtilVista.mostrarError(this, e.getMessage());
        }
        UtilVista.limpiarTabla(modelo);
        for (T registro : datos) {
            modelo.addRow(aFila(registro));
        }
        lblTotal.setText("Registros: " + datos.size());
        nuevo();
    }

    /** Deja el formulario listo para crear un registro nuevo. */
    protected void nuevo() {
        tabla.clearSelection();
        seleccionado = null;
        limpiarFormulario();
        actualizarBotones();
    }

    private void limpiarTodo() {
        txtBuscar.setText("");
        recargar();
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= datos.size()) {
            return;
        }
        seleccionado = datos.get(fila);
        cargarFormulario(seleccionado);
        actualizarBotones();
    }

    private void accionGuardar() {
        try {
            T registro = leerFormulario();
            guardar(registro);
            UtilVista.mostrarInfo(this, "Registro guardado correctamente.");
            recargar();
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        }
    }

    private void accionActualizar() {
        if (seleccionado == null) {
            UtilVista.mostrarError(this, "Seleccione un registro de la tabla.");
            return;
        }
        try {
            T registro = leerFormulario();
            actualizar(registro);
            UtilVista.mostrarInfo(this, "Registro actualizado correctamente.");
            recargar();
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        }
    }

    private void accionEliminar() {
        if (seleccionado == null) {
            UtilVista.mostrarError(this, "Seleccione un registro de la tabla.");
            return;
        }
        if (!UtilVista.confirmar(this, "¿Está seguro de eliminar " + describir(seleccionado) + "?")) {
            return;
        }
        try {
            ejecutarEliminacion(seleccionado);
            recargar();
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        }
    }

    /**
     * Elimina el registro (ya confirmado) y avisa al usuario. Las subclases lo
     * sobrescriben si el borrado puede terminar de otra forma (p. ej. Proveedores
     * ofrece desactivar en lugar de eliminar).
     */
    protected void ejecutarEliminacion(T registro) throws ControladorException {
        eliminar(registro);
        UtilVista.mostrarInfo(this, "Registro eliminado correctamente.");
    }

    /**
     * Habilita los botones según el rol y el estado del formulario: Guardar
     * solo para registros nuevos; Actualizar y Eliminar solo con una fila
     * seleccionada. Sin permiso de edición, los tres quedan deshabilitados.
     */
    protected void actualizarBotones() {
        boolean puedeEditar = Sesion.puedeEditar();
        boolean hayFila = seleccionado != null;
        btnNuevo.setEnabled(puedeEditar);
        btnGuardar.setEnabled(puedeEditar && !hayFila);
        btnActualizar.setEnabled(puedeEditar && hayFila);
        btnEliminar.setEnabled(puedeEditar && hayFila);
    }

    /** @return el registro cargado en el formulario, o null si es uno nuevo */
    protected T getSeleccionado() {
        return seleccionado;
    }
}
