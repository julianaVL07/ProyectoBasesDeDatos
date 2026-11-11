package co.edu.uniquindio.proyectobasesdedatos.vista.entidades;

import co.edu.uniquindio.proyectobasesdedatos.controlador.CategoriaControlador;
import co.edu.uniquindio.proyectobasesdedatos.controlador.ControladorException;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.CategoriaDTO;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import java.util.List;

/**
 * CRUD de Categorías (tabla TCategoria). No tiene SQL: todo pasa por
 * {@link CategoriaControlador}.
 */
public class VentanaCategorias extends VentanaCrud<CategoriaDTO> {

    private final CategoriaControlador controlador = new CategoriaControlador();

    private final JTextField txtCodigo = new JTextField(8);
    private final JTextField txtNombre = new JTextField(30);
    private final JTextField txtDescripcion = new JTextField(30);

    public VentanaCategorias() {
        super("Categorías", "Código", "Nombre", "Descripción");
        setSize(700, 500);
        txtCodigo.setEditable(false);
        inicializar();
    }

    @Override
    protected JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Código:"), celda(0, 0));
        panel.add(txtCodigo, celda(1, 0));
        panel.add(new JLabel("Nombre:"), celda(0, 1));
        panel.add(txtNombre, celda(1, 1));
        panel.add(new JLabel("Descripción:"), celda(0, 2));
        panel.add(txtDescripcion, celda(1, 2));
        return panel;
    }

    @Override
    protected void configurarColumnas(JTable tabla) {
        tabla.getColumnModel().getColumn(0).setMaxWidth(70);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(380);
    }

    @Override
    protected List<CategoriaDTO> buscar(String texto) throws ControladorException {
        return controlador.buscar(texto);
    }

    @Override
    protected Object[] aFila(CategoriaDTO c) {
        return new Object[]{c.getCodigo(), c.getNombre(), c.getDescripcion()};
    }

    @Override
    protected void cargarFormulario(CategoriaDTO c) {
        txtCodigo.setText(String.valueOf(c.getCodigo()));
        txtNombre.setText(c.getNombre());
        txtDescripcion.setText(c.getDescripcion() == null ? "" : c.getDescripcion());
    }

    @Override
    protected void limpiarFormulario() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtNombre.requestFocusInWindow();
    }

    @Override
    protected CategoriaDTO leerFormulario() {
        CategoriaDTO c = new CategoriaDTO();
        if (getSeleccionado() != null) {
            c.setCodigo(getSeleccionado().getCodigo());
        }
        c.setNombre(txtNombre.getText());
        c.setDescripcion(txtDescripcion.getText());
        return c;
    }

    @Override
    protected void guardar(CategoriaDTO c) throws ControladorException {
        controlador.guardar(c);
    }

    @Override
    protected void actualizar(CategoriaDTO c) throws ControladorException {
        controlador.actualizar(c);
    }

    @Override
    protected void eliminar(CategoriaDTO c) throws ControladorException {
        controlador.eliminar(c.getCodigo());
    }

    @Override
    protected String describir(CategoriaDTO c) {
        return "la categoría «" + c.getNombre() + "»";
    }
}
