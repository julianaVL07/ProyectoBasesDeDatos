package co.edu.uniquindio.proyectobasesdedatos.vista.entidades;

import co.edu.uniquindio.proyectobasesdedatos.controlador.CiudadControlador;
import co.edu.uniquindio.proyectobasesdedatos.controlador.ControladorException;
import co.edu.uniquindio.proyectobasesdedatos.controlador.DepartamentoControlador;
import co.edu.uniquindio.proyectobasesdedatos.controlador.ProveedorControlador;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.CiudadDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.DepartamentoDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.ProveedorDTO;
import co.edu.uniquindio.proyectobasesdedatos.vista.util.UtilVista;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD de Proveedores (tabla TProveedor). El combo de departamento filtra el
 * de ciudades y el checkbox indica si el proveedor está activo. No tiene SQL:
 * todo pasa por los controladores.
 */
public class VentanaProveedores extends VentanaCrud<ProveedorDTO> {

    private final ProveedorControlador controlador = new ProveedorControlador();
    private final DepartamentoControlador departamentoControlador = new DepartamentoControlador();
    private final CiudadControlador ciudadControlador = new CiudadControlador();

    private final JTextField txtCodigo = new JTextField(8);
    private final JTextField txtNit = new JTextField(15);
    private final JTextField txtNombre = new JTextField(25);
    private final JTextField txtDireccion = new JTextField(25);
    private final JTextField txtCorreo = new JTextField(20);
    private final JTextField txtTelefono = new JTextField(15);
    private final JComboBox<DepartamentoDTO> cmbDepartamento = new JComboBox<>();
    private final JComboBox<CiudadDTO> cmbCiudad = new JComboBox<>();
    private final JCheckBox chkActivo = new JCheckBox("Activo", true);

    /** Todas las ciudades; se filtran en memoria según el departamento elegido. */
    private List<CiudadDTO> ciudades = new ArrayList<>();

    public VentanaProveedores() {
        super("Proveedores", "Código", "NIT", "Nombre", "Ciudad", "Teléfono", "Correo", "Activo");
        setSize(900, 600);
        txtCodigo.setEditable(false);
        cmbDepartamento.addActionListener(e -> filtrarCiudades());
        inicializar();
    }

    @Override
    protected JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Código:"), celda(0, 0));
        panel.add(txtCodigo, celda(1, 0));
        panel.add(new JLabel("NIT:"), celda(2, 0));
        panel.add(txtNit, celda(3, 0));

        panel.add(new JLabel("Nombre:"), celda(0, 1));
        panel.add(txtNombre, celda(1, 1));
        panel.add(new JLabel("Dirección:"), celda(2, 1));
        panel.add(txtDireccion, celda(3, 1));

        panel.add(new JLabel("Correo:"), celda(0, 2));
        panel.add(txtCorreo, celda(1, 2));
        panel.add(new JLabel("Teléfono:"), celda(2, 2));
        panel.add(txtTelefono, celda(3, 2));

        panel.add(new JLabel("Departamento:"), celda(0, 3));
        panel.add(cmbDepartamento, celda(1, 3));
        panel.add(new JLabel("Ciudad:"), celda(2, 3));
        panel.add(cmbCiudad, celda(3, 3));

        panel.add(chkActivo, celda(1, 4));
        return panel;
    }

    @Override
    protected void configurarColumnas(JTable tabla) {
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(180);
        tabla.getColumnModel().getColumn(6).setMaxWidth(60);
    }

    /** Antes de recargar la tabla se refrescan los combos (pueden haber creado ciudades). */
    @Override
    protected void recargar() {
        cargarCombos();
        super.recargar();
    }

    private void cargarCombos() {
        try {
            ciudades = ciudadControlador.listar();
            List<DepartamentoDTO> departamentos = departamentoControlador.listar();
            cmbDepartamento.setModel(new DefaultComboBoxModel<>(
                    departamentos.toArray(new DepartamentoDTO[0])));
            cmbDepartamento.setSelectedIndex(-1);
            filtrarCiudades();
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        }
    }

    /** Llena el combo de ciudades solo con las del departamento seleccionado. */
    private void filtrarCiudades() {
        DepartamentoDTO departamento = (DepartamentoDTO) cmbDepartamento.getSelectedItem();
        DefaultComboBoxModel<CiudadDTO> modelo = new DefaultComboBoxModel<>();
        if (departamento != null) {
            for (CiudadDTO c : ciudades) {
                if (c.getCodigoDepartamento() == departamento.getCodigo()) {
                    modelo.addElement(c);
                }
            }
        }
        cmbCiudad.setModel(modelo);
        cmbCiudad.setSelectedIndex(-1);
    }

    @Override
    protected List<ProveedorDTO> buscar(String texto) throws ControladorException {
        return controlador.buscar(texto);
    }

    @Override
    protected Object[] aFila(ProveedorDTO p) {
        return new Object[]{p.getCodigo(), p.getNit(), p.getNombre(), p.getNombreCiudad(),
                p.getTelefono(), p.getCorreo(), p.isActivo() ? "Sí" : "No"};
    }

    @Override
    protected void cargarFormulario(ProveedorDTO p) {
        txtCodigo.setText(String.valueOf(p.getCodigo()));
        txtNit.setText(p.getNit());
        txtNombre.setText(p.getNombre());
        txtDireccion.setText(p.getDireccion() == null ? "" : p.getDireccion());
        txtCorreo.setText(p.getCorreo() == null ? "" : p.getCorreo());
        txtTelefono.setText(p.getTelefono() == null ? "" : p.getTelefono());
        chkActivo.setSelected(p.isActivo());
        seleccionarCiudad(p.getCodigoCiudad());
    }

    /**
     * Selecciona primero el departamento de la ciudad (eso filtra el combo de
     * ciudades) y luego la ciudad. Los DTO no definen equals, por eso se compara
     * por código.
     */
    private void seleccionarCiudad(int codigoCiudad) {
        int codigoDepartamento = 0;
        for (CiudadDTO c : ciudades) {
            if (c.getCodigo() == codigoCiudad) {
                codigoDepartamento = c.getCodigoDepartamento();
                break;
            }
        }
        cmbDepartamento.setSelectedIndex(-1);
        for (int i = 0; i < cmbDepartamento.getItemCount(); i++) {
            if (cmbDepartamento.getItemAt(i).getCodigo() == codigoDepartamento) {
                cmbDepartamento.setSelectedIndex(i); // dispara filtrarCiudades()
                break;
            }
        }
        for (int i = 0; i < cmbCiudad.getItemCount(); i++) {
            if (cmbCiudad.getItemAt(i).getCodigo() == codigoCiudad) {
                cmbCiudad.setSelectedIndex(i);
                return;
            }
        }
    }

    @Override
    protected void limpiarFormulario() {
        txtCodigo.setText("");
        txtNit.setText("");
        txtNombre.setText("");
        txtDireccion.setText("");
        txtCorreo.setText("");
        txtTelefono.setText("");
        chkActivo.setSelected(true);
        cmbDepartamento.setSelectedIndex(-1); // también vacía el combo de ciudades
        txtNit.requestFocusInWindow();
    }

    @Override
    protected ProveedorDTO leerFormulario() {
        ProveedorDTO p = new ProveedorDTO();
        if (getSeleccionado() != null) {
            p.setCodigo(getSeleccionado().getCodigo());
        }
        p.setNit(txtNit.getText());
        p.setNombre(txtNombre.getText());
        p.setDireccion(txtDireccion.getText());
        p.setCorreo(txtCorreo.getText());
        p.setTelefono(txtTelefono.getText());
        p.setActivo(chkActivo.isSelected());
        CiudadDTO ciudad = (CiudadDTO) cmbCiudad.getSelectedItem();
        p.setCodigoCiudad(ciudad == null ? 0 : ciudad.getCodigo());
        return p;
    }

    @Override
    protected void guardar(ProveedorDTO p) throws ControladorException {
        controlador.guardar(p);
    }

    @Override
    protected void actualizar(ProveedorDTO p) throws ControladorException {
        controlador.actualizar(p);
    }

    @Override
    protected void eliminar(ProveedorDTO p) throws ControladorException {
        controlador.eliminar(p.getCodigo());
    }

    @Override
    protected String describir(ProveedorDTO p) {
        return "el proveedor «" + p.getNombre() + "» (NIT " + p.getNit() + ")";
    }
}
