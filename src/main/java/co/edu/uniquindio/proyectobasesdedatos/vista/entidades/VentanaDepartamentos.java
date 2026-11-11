package co.edu.uniquindio.proyectobasesdedatos.vista.entidades;

import co.edu.uniquindio.proyectobasesdedatos.controlador.ControladorException;
import co.edu.uniquindio.proyectobasesdedatos.controlador.DepartamentoControlador;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.DepartamentoDTO;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import java.util.List;

/**
 * CRUD de Departamentos (tabla TDepartamento). No tiene SQL: todo pasa por
 * {@link DepartamentoControlador}.
 */
public class VentanaDepartamentos extends VentanaCrud<DepartamentoDTO> {

    private final DepartamentoControlador controlador = new DepartamentoControlador();

    private final JTextField txtCodigo = new JTextField(8);
    private final JTextField txtNombre = new JTextField(30);

    public VentanaDepartamentos() {
        super("Departamentos", "Código", "Nombre");
        setSize(560, 460);
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
        return panel;
    }

    @Override
    protected void configurarColumnas(JTable tabla) {
        tabla.getColumnModel().getColumn(0).setMaxWidth(70);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(400);
    }

    @Override
    protected List<DepartamentoDTO> buscar(String texto) throws ControladorException {
        return controlador.buscar(texto);
    }

    @Override
    protected Object[] aFila(DepartamentoDTO d) {
        return new Object[]{d.getCodigo(), d.getNombre()};
    }

    @Override
    protected void cargarFormulario(DepartamentoDTO d) {
        txtCodigo.setText(String.valueOf(d.getCodigo()));
        txtNombre.setText(d.getNombre());
    }

    @Override
    protected void limpiarFormulario() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtNombre.requestFocusInWindow();
    }

    @Override
    protected DepartamentoDTO leerFormulario() {
        DepartamentoDTO d = new DepartamentoDTO();
        if (getSeleccionado() != null) {
            d.setCodigo(getSeleccionado().getCodigo());
        }
        d.setNombre(txtNombre.getText());
        return d;
    }

    @Override
    protected void guardar(DepartamentoDTO d) throws ControladorException {
        controlador.guardar(d);
    }

    @Override
    protected void actualizar(DepartamentoDTO d) throws ControladorException {
        controlador.actualizar(d);
    }

    @Override
    protected void eliminar(DepartamentoDTO d) throws ControladorException {
        controlador.eliminar(d.getCodigo());
    }

    @Override
    protected String describir(DepartamentoDTO d) {
        return "el departamento «" + d.getNombre() + "»";
    }
}
