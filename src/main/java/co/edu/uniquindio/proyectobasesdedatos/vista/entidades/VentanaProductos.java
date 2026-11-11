package co.edu.uniquindio.proyectobasesdedatos.vista.entidades;

import co.edu.uniquindio.proyectobasesdedatos.controlador.CategoriaControlador;
import co.edu.uniquindio.proyectobasesdedatos.controlador.ControladorException;
import co.edu.uniquindio.proyectobasesdedatos.controlador.ProductoControlador;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.CategoriaDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.ProductoDTO;
import co.edu.uniquindio.proyectobasesdedatos.vista.util.UtilVista;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import java.util.List;

/**
 * CRUD de Productos (tabla TProducto) con combo de categorías. No tiene SQL:
 * todo pasa por {@link ProductoControlador} y {@link CategoriaControlador}.
 *
 * <p>El precio se muestra en la tabla con separador de miles ("28.900,00") y
 * en el formulario se puede escribir como "28900", "28.900" o "28.900,50".</p>
 */
public class VentanaProductos extends VentanaCrud<ProductoDTO> {

    private static final int COL_PRECIO = 3;
    private static final int COL_STOCK = 4;

    private final ProductoControlador controlador = new ProductoControlador();
    private final CategoriaControlador categoriaControlador = new CategoriaControlador();

    private final JTextField txtCodigo = new JTextField(8);
    private final JTextField txtNombre = new JTextField(25);
    private final JTextField txtDescripcion = new JTextField(25);
    private final JTextField txtPrecio = new JTextField(12);
    private final JTextField txtStock = new JTextField(12);
    private final JComboBox<CategoriaDTO> cmbCategoria = new JComboBox<>();

    public VentanaProductos() {
        super("Productos", "Código", "Nombre", "Categoría", "Precio base", "Stock", "Descripción");
        setSize(900, 560);
        txtCodigo.setEditable(false);
        inicializar();
    }

    @Override
    protected JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Código:"), celda(0, 0));
        panel.add(txtCodigo, celda(1, 0));
        panel.add(new JLabel("Categoría:"), celda(2, 0));
        panel.add(cmbCategoria, celda(3, 0));

        panel.add(new JLabel("Nombre:"), celda(0, 1));
        panel.add(txtNombre, celda(1, 1));
        panel.add(new JLabel("Precio base:"), celda(2, 1));
        panel.add(txtPrecio, celda(3, 1));

        panel.add(new JLabel("Descripción:"), celda(0, 2));
        panel.add(txtDescripcion, celda(1, 2));
        panel.add(new JLabel("Stock:"), celda(2, 2));
        panel.add(txtStock, celda(3, 2));
        return panel;
    }

    @Override
    protected void configurarColumnas(JTable tabla) {
        tabla.getColumnModel().getColumn(0).setMaxWidth(70);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(250);
        UtilVista.alinearDerecha(tabla, COL_PRECIO);
        UtilVista.alinearDerecha(tabla, COL_STOCK);
    }

    /** Antes de recargar la tabla se refresca el combo (pueden haber creado categorías). */
    @Override
    protected void recargar() {
        cargarCategorias();
        super.recargar();
    }

    private void cargarCategorias() {
        try {
            List<CategoriaDTO> categorias = categoriaControlador.listar();
            cmbCategoria.setModel(new DefaultComboBoxModel<>(categorias.toArray(new CategoriaDTO[0])));
            cmbCategoria.setSelectedIndex(-1);
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        }
    }

    @Override
    protected List<ProductoDTO> buscar(String texto) throws ControladorException {
        return controlador.buscar(texto);
    }

    @Override
    protected Object[] aFila(ProductoDTO p) {
        return new Object[]{
                p.getCodigo(),
                p.getNombre(),
                p.getNombreCategoria(),
                UtilVista.formatearPrecio(p.getPrecioBase()),
                UtilVista.formatearEntero(p.getStock()),
                p.getDescripcion()
        };
    }

    @Override
    protected void cargarFormulario(ProductoDTO p) {
        txtCodigo.setText(String.valueOf(p.getCodigo()));
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion() == null ? "" : p.getDescripcion());
        txtPrecio.setText(UtilVista.formatearParaEditar(p.getPrecioBase()));
        txtStock.setText(String.valueOf(p.getStock()));
        seleccionarCategoria(p.getCodigoCategoria());
    }

    /** Selecciona en el combo la categoría con ese código (el DTO no define equals). */
    private void seleccionarCategoria(int codigo) {
        cmbCategoria.setSelectedIndex(-1);
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            if (cmbCategoria.getItemAt(i).getCodigo() == codigo) {
                cmbCategoria.setSelectedIndex(i);
                return;
            }
        }
    }

    @Override
    protected void limpiarFormulario() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        cmbCategoria.setSelectedIndex(-1);
        txtNombre.requestFocusInWindow();
    }

    @Override
    protected ProductoDTO leerFormulario() throws ControladorException {
        ProductoDTO p = new ProductoDTO();
        if (getSeleccionado() != null) {
            p.setCodigo(getSeleccionado().getCodigo());
        }
        p.setNombre(txtNombre.getText());
        p.setDescripcion(txtDescripcion.getText());
        p.setPrecioBase(controlador.convertirPrecio(txtPrecio.getText()));
        p.setStock(controlador.convertirStock(txtStock.getText()));
        CategoriaDTO categoria = (CategoriaDTO) cmbCategoria.getSelectedItem();
        p.setCodigoCategoria(categoria == null ? 0 : categoria.getCodigo());
        return p;
    }

    @Override
    protected void guardar(ProductoDTO p) throws ControladorException {
        controlador.guardar(p);
    }

    @Override
    protected void actualizar(ProductoDTO p) throws ControladorException {
        controlador.actualizar(p);
    }

    @Override
    protected void eliminar(ProductoDTO p) throws ControladorException {
        controlador.eliminar(p.getCodigo());
    }

    @Override
    protected String describir(ProductoDTO p) {
        return "el producto «" + p.getNombre() + "»";
    }
}
