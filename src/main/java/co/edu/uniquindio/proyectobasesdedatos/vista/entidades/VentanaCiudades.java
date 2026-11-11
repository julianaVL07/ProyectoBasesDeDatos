package co.edu.uniquindio.proyectobasesdedatos.vista.entidades;

import co.edu.uniquindio.proyectobasesdedatos.controlador.CiudadControlador;
import co.edu.uniquindio.proyectobasesdedatos.controlador.ControladorException;
import co.edu.uniquindio.proyectobasesdedatos.controlador.DepartamentoControlador;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.CiudadDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.DepartamentoDTO;
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
 * CRUD de Ciudades (tabla TCiudad) con combo de departamentos. No tiene SQL:
 * todo pasa por {@link CiudadControlador} y {@link DepartamentoControlador}.
 */
public class VentanaCiudades extends VentanaCrud<CiudadDTO> {

    private final CiudadControlador controlador = new CiudadControlador();
    private final DepartamentoControlador departamentoControlador = new DepartamentoControlador();

    private final JTextField txtCodigo = new JTextField(8);
    private final JTextField txtNombre = new JTextField(25);
    private final JComboBox<DepartamentoDTO> cmbDepartamento = new JComboBox<>();

    public VentanaCiudades() {
        super("Ciudades", "Código", "Ciudad", "Departamento");
        setSize(650, 500);
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
        panel.add(new JLabel("Departamento:"), celda(0, 2));
        panel.add(cmbDepartamento, celda(1, 2));
        return panel;
    }

    @Override
    protected void configurarColumnas(JTable tabla) {
        tabla.getColumnModel().getColumn(0).setMaxWidth(70);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(250);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(250);
    }

    /** Antes de recargar la tabla se refresca el combo (pueden haber creado departamentos). */
    @Override
    protected void recargar() {
        cargarDepartamentos();
        super.recargar();
    }

    private void cargarDepartamentos() {
        try {
            List<DepartamentoDTO> departamentos = departamentoControlador.listar();
            cmbDepartamento.setModel(new DefaultComboBoxModel<>(
                    departamentos.toArray(new DepartamentoDTO[0])));
            cmbDepartamento.setSelectedIndex(-1);
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        }
    }

    @Override
    protected List<CiudadDTO> buscar(String texto) throws ControladorException {
        return controlador.buscar(texto);
    }

    @Override
    protected Object[] aFila(CiudadDTO c) {
        return new Object[]{c.getCodigo(), c.getNombre(), c.getNombreDepartamento()};
    }

    @Override
    protected void cargarFormulario(CiudadDTO c) {
        txtCodigo.setText(String.valueOf(c.getCodigo()));
        txtNombre.setText(c.getNombre());
        seleccionarDepartamento(c.getCodigoDepartamento());
    }

    /** Selecciona en el combo el departamento con ese código (el DTO no define equals). */
    private void seleccionarDepartamento(int codigo) {
        cmbDepartamento.setSelectedIndex(-1);
        for (int i = 0; i < cmbDepartamento.getItemCount(); i++) {
            if (cmbDepartamento.getItemAt(i).getCodigo() == codigo) {
                cmbDepartamento.setSelectedIndex(i);
                return;
            }
        }
    }

    @Override
    protected void limpiarFormulario() {
        txtCodigo.setText("");
        txtNombre.setText("");
        cmbDepartamento.setSelectedIndex(-1);
        txtNombre.requestFocusInWindow();
    }

    @Override
    protected CiudadDTO leerFormulario() {
        CiudadDTO c = new CiudadDTO();
        if (getSeleccionado() != null) {
            c.setCodigo(getSeleccionado().getCodigo());
        }
        c.setNombre(txtNombre.getText());
        DepartamentoDTO departamento = (DepartamentoDTO) cmbDepartamento.getSelectedItem();
        c.setCodigoDepartamento(departamento == null ? 0 : departamento.getCodigo());
        return c;
    }

    @Override
    protected void guardar(CiudadDTO c) throws ControladorException {
        controlador.guardar(c);
    }

    @Override
    protected void actualizar(CiudadDTO c) throws ControladorException {
        controlador.actualizar(c);
    }

    @Override
    protected void eliminar(CiudadDTO c) throws ControladorException {
        controlador.eliminar(c.getCodigo());
    }

    @Override
    protected String describir(CiudadDTO c) {
        return "la ciudad «" + c.getNombre() + "» (" + c.getNombreDepartamento() + ")";
    }
}
