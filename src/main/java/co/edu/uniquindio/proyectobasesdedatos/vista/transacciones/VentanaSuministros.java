package co.edu.uniquindio.proyectobasesdedatos.vista.transacciones;

import co.edu.uniquindio.proyectobasesdedatos.controlador.ControladorException;
import co.edu.uniquindio.proyectobasesdedatos.controlador.SuministroControlador;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.ProductoDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.ProveedorDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.SuministroDTO;
import co.edu.uniquindio.proyectobasesdedatos.vista.util.UtilVista;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana interna de la transacción Suministro: asigna productos a proveedores
 * con su precio y días de entrega (tabla TSuministro). No tiene SQL: todo pasa
 * por {@link SuministroControlador}.
 *
 * <p>Arriba se elige el proveedor y la tabla muestra lo que suministra. Abajo
 * se elige el producto (se muestra su precio base como referencia), se escribe
 * el precio y los días, y se guarda. Al seleccionar una fila de la tabla se
 * carga en el formulario para modificarla o eliminarla.</p>
 */
public class VentanaSuministros extends JInternalFrame {

    private static final int COL_PRECIO_BASE = 2;
    private static final int COL_PRECIO = 3;
    private static final int COL_DIFERENCIA = 4;
    private static final int COL_DIAS = 5;

    private final SuministroControlador controlador = new SuministroControlador();

    private final JComboBox<ProveedorDTO> cmbProveedor = new JComboBox<>();
    private final JComboBox<ProductoDTO> cmbProducto = new JComboBox<>();
    private final JLabel lblPrecioBase = new JLabel(" ");
    private final JTextField txtPrecio = new JTextField(12);
    private final JTextField txtDias = new JTextField(6);

    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnGuardar = new JButton("Asignar");
    private final JButton btnActualizar = new JButton("Actualizar");
    private final JButton btnEliminar = new JButton("Quitar");
    private final JButton btnRecargar = new JButton("Recargar");

    private final JTable tabla = new JTable();
    private final DefaultTableModel modelo = UtilVista.crearModeloNoEditable(
            "Código", "Producto", "Precio base", "Precio suministro", "Diferencia", "Días entrega");
    private final JLabel lblTotal = new JLabel("Productos suministrados: 0");

    /** Filas que se muestran en la tabla (mismo orden que el modelo). */
    private List<SuministroDTO> suministros = new ArrayList<>();
    /** Suministro seleccionado en la tabla, o null si se está creando uno nuevo. */
    private SuministroDTO seleccionado;

    public VentanaSuministros() {
        super("Suministros — asignar productos a proveedores", true, true, true, true);
        setSize(900, 580);

        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(crearPanelProveedor(), BorderLayout.NORTH);
        getContentPane().add(crearPanelTabla(), BorderLayout.CENTER);
        getContentPane().add(crearPanelFormulario(), BorderLayout.SOUTH);

        UtilVista.configurarTabla(tabla, modelo);
        configurarColumnas();
        configurarRenderizadores();
        configurarEventos();

        recargarCombos();
    }

    // ----------------------------------------------------------------- Paneles

    private JPanel crearPanelProveedor() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 8, 0, 8),
                BorderFactory.createTitledBorder("Proveedor (solo activos)")));
        cmbProveedor.setPrototypeDisplayValue(prototipoProveedor());
        panel.add(new JLabel("Proveedor:"));
        panel.add(cmbProveedor);
        panel.add(btnRecargar);
        return panel;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(lblTotal, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelFormulario() {
        JPanel campos = new JPanel(new GridBagLayout());
        campos.add(new JLabel("Producto:"), celda(0, 0));
        campos.add(cmbProducto, celda(1, 0));
        campos.add(new JLabel("Precio base:"), celda(2, 0));
        campos.add(lblPrecioBase, celda(3, 0));

        campos.add(new JLabel("Precio suministro:"), celda(0, 1));
        campos.add(txtPrecio, celda(1, 1));
        campos.add(new JLabel("Días de entrega:"), celda(2, 1));
        campos.add(txtDias, celda(3, 1));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 8, 8, 8),
                BorderFactory.createTitledBorder("Producto a asignar")));
        panel.add(campos, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private static GridBagConstraints celda(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = x % 2 == 1 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        c.weightx = x % 2 == 1 ? 1 : 0;
        return c;
    }

    private void configurarColumnas() {
        tabla.getColumnModel().getColumn(0).setMaxWidth(70);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(250);
        UtilVista.alinearDerecha(tabla, COL_PRECIO_BASE);
        UtilVista.alinearDerecha(tabla, COL_PRECIO);
        UtilVista.alinearDerecha(tabla, COL_DIFERENCIA);
        UtilVista.alinearDerecha(tabla, COL_DIAS);
    }

    /** El combo de proveedores muestra "Nombre (NIT)" en lugar del toString del DTO. */
    private void configurarRenderizadores() {
        cmbProveedor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                Object texto = valor instanceof ProveedorDTO p
                        ? p.getNombre() + " (NIT " + p.getNit() + ")"
                        : valor;
                return super.getListCellRendererComponent(lista, texto, indice, seleccionado, foco);
            }
        });
    }

    private void configurarEventos() {
        cmbProveedor.addActionListener(e -> cargarSuministros());
        cmbProducto.addActionListener(e -> mostrarPrecioBase());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFila();
            }
        });
        btnRecargar.addActionListener(e -> recargarCombos());
        btnNuevo.addActionListener(e -> {
            tabla.clearSelection();
            limpiarFormulario();
        });
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
    }

    // ---------------------------------------------------------------- Carga

    /** Recarga proveedores y productos, conservando el proveedor elegido si sigue activo. */
    private void recargarCombos() {
        ProveedorDTO anterior = (ProveedorDTO) cmbProveedor.getSelectedItem();
        try {
            List<ProveedorDTO> proveedores = controlador.listarProveedoresActivos();
            List<ProductoDTO> productos = controlador.listarProductos();

            cmbProducto.setModel(new DefaultComboBoxModel<>(productos.toArray(new ProductoDTO[0])));
            cmbProveedor.setModel(new DefaultComboBoxModel<>(proveedores.toArray(new ProveedorDTO[0])));
            cmbProveedor.setSelectedIndex(proveedores.isEmpty() ? -1 : 0);
            if (anterior != null) {
                seleccionarProveedor(anterior.getCodigo());
            }
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        }
        cargarSuministros();
    }

    /** Llena la tabla con los productos que suministra el proveedor elegido. */
    private void cargarSuministros() {
        UtilVista.limpiarTabla(modelo);
        suministros = new ArrayList<>();
        limpiarFormulario();

        ProveedorDTO proveedor = (ProveedorDTO) cmbProveedor.getSelectedItem();
        if (proveedor == null) {
            lblTotal.setText("No hay proveedores activos.");
            return;
        }
        try {
            suministros = controlador.listarPorProveedor(proveedor.getCodigo());
            for (SuministroDTO s : suministros) {
                modelo.addRow(new Object[]{
                        s.getCodigo(),
                        s.getNombreProducto(),
                        UtilVista.formatearPrecio(s.getPrecioBaseProducto()),
                        UtilVista.formatearPrecio(s.getPrecio()),
                        UtilVista.formatearPrecio(diferencia(s)),
                        s.getDiasEntrega()
                });
            }
            lblTotal.setText("Productos suministrados: " + suministros.size());
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        }
    }

    /** Precio de suministro menos el precio base del producto. */
    private static BigDecimal diferencia(SuministroDTO s) {
        if (s.getPrecio() == null || s.getPrecioBaseProducto() == null) {
            return null;
        }
        return s.getPrecio().subtract(s.getPrecioBaseProducto());
    }

    /** Muestra el precio base del producto elegido como referencia para el precio. */
    private void mostrarPrecioBase() {
        ProductoDTO producto = (ProductoDTO) cmbProducto.getSelectedItem();
        lblPrecioBase.setText(producto == null
                ? " "
                : "$ " + UtilVista.formatearPrecio(producto.getPrecioBase()));
    }

    // ---------------------------------------------------------------- Formulario

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= suministros.size()) {
            return;
        }
        seleccionado = suministros.get(tabla.convertRowIndexToModel(fila));
        seleccionarProducto(seleccionado.getCodigoProducto());
        txtPrecio.setText(UtilVista.formatearParaEditar(seleccionado.getPrecio()));
        txtDias.setText(String.valueOf(seleccionado.getDiasEntrega()));
        actualizarBotones();
    }

    private void limpiarFormulario() {
        seleccionado = null;
        cmbProducto.setSelectedIndex(-1);
        txtPrecio.setText("");
        txtDias.setText("");
        actualizarBotones();
    }

    /** Asignar solo con un registro nuevo; Actualizar y Quitar solo con una fila seleccionada. */
    private void actualizarBotones() {
        boolean hayProveedor = cmbProveedor.getSelectedItem() != null;
        btnGuardar.setEnabled(hayProveedor && seleccionado == null);
        btnActualizar.setEnabled(seleccionado != null);
        btnEliminar.setEnabled(seleccionado != null);
    }

    /** Arma el DTO con el proveedor del combo y los datos del formulario. */
    private SuministroDTO leerFormulario() throws ControladorException {
        SuministroDTO s = new SuministroDTO();
        if (seleccionado != null) {
            s.setCodigo(seleccionado.getCodigo());
        }
        ProveedorDTO proveedor = (ProveedorDTO) cmbProveedor.getSelectedItem();
        ProductoDTO producto = (ProductoDTO) cmbProducto.getSelectedItem();
        s.setCodigoProveedor(proveedor == null ? 0 : proveedor.getCodigo());
        s.setCodigoProducto(producto == null ? 0 : producto.getCodigo());
        s.setPrecio(controlador.convertirPrecio(txtPrecio.getText()));
        s.setDiasEntrega(controlador.convertirDias(txtDias.getText()));
        return s;
    }

    // ---------------------------------------------------------------- Acciones

    private void guardar() {
        try {
            controlador.guardar(leerFormulario());
            UtilVista.mostrarInfo(this, "Producto asignado al proveedor.");
            cargarSuministros();
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        }
    }

    private void actualizar() {
        try {
            controlador.actualizar(leerFormulario());
            UtilVista.mostrarInfo(this, "Suministro actualizado.");
            cargarSuministros();
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            return;
        }
        String mensaje = "¿Desea quitar «" + seleccionado.getNombreProducto()
                + "» de los productos de este proveedor?";
        if (!UtilVista.confirmar(this, mensaje)) {
            return;
        }
        try {
            controlador.eliminar(seleccionado.getCodigo());
            UtilVista.mostrarInfo(this, "Suministro eliminado.");
            cargarSuministros();
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        }
    }

    // ------------------------------------------------------- Apoyo interno

    /** Selecciona en el combo el proveedor con ese código (el DTO no define equals). */
    private void seleccionarProveedor(int codigo) {
        for (int i = 0; i < cmbProveedor.getItemCount(); i++) {
            if (cmbProveedor.getItemAt(i).getCodigo() == codigo) {
                cmbProveedor.setSelectedIndex(i);
                return;
            }
        }
    }

    /** Selecciona en el combo el producto con ese código (el DTO no define equals). */
    private void seleccionarProducto(int codigo) {
        cmbProducto.setSelectedIndex(-1);
        for (int i = 0; i < cmbProducto.getItemCount(); i++) {
            if (cmbProducto.getItemAt(i).getCodigo() == codigo) {
                cmbProducto.setSelectedIndex(i);
                return;
            }
        }
    }

    /** Valor de ejemplo para que el combo de proveedores tenga un ancho cómodo. */
    private static ProveedorDTO prototipoProveedor() {
        ProveedorDTO p = new ProveedorDTO();
        p.setNombre("Distribuidora de ejemplo con nombre largo");
        p.setNit("000000000-0");
        return p;
    }
}
