package co.edu.uniquindio.proyectobasesdedatos.vista.auditoria;

import co.edu.uniquindio.proyectobasesdedatos.controlador.ControladorException;
import co.edu.uniquindio.proyectobasesdedatos.controlador.UsuarioControlador;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.RolDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.UsuarioDTO;
import co.edu.uniquindio.proyectobasesdedatos.util.Sesion;
import co.edu.uniquindio.proyectobasesdedatos.vista.util.UtilVista;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ventana interna de Gestión de Usuarios (módulo Auditoría, solo Administrador).
 *
 * <p>Permite crear usuarios (con contraseña y confirmación), editar sus datos,
 * cambiarles la contraseña y activarlos o desactivarlos. No hay botón de
 * eliminar: un usuario con registros en la Bitácora se desactiva para
 * conservar su historial. Si ya existe un Administrador, ese rol no se ofrece
 * en el combo.</p>
 */
public class VentanaUsuarios extends JInternalFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UsuarioControlador controlador = new UsuarioControlador();

    // Formulario
    private final JTextField txtUsuario = new JTextField(20);
    private final JTextField txtNombre = new JTextField(30);
    private final JComboBox<RolDTO> cmbRol = new JComboBox<>();
    private final JPasswordField txtClave = new JPasswordField(20);
    private final JPasswordField txtConfirmar = new JPasswordField(20);
    private final JLabel lblEstado = new JLabel("—");

    // Botones
    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnActualizar = new JButton("Actualizar");
    private final JButton btnCambiarClave = new JButton("Cambiar contraseña");
    private final JButton btnEstado = new JButton("Desactivar");
    private final JButton btnLimpiar = new JButton("Limpiar");

    // Tabla
    private final JTable tabla = new JTable();
    private final DefaultTableModel modelo = UtilVista.crearModeloNoEditable(
            "Usuario", "Nombre completo", "Rol", "Activo", "Fecha de creación");
    private final JLabel lblTotal = new JLabel("Usuarios: 0");

    private List<UsuarioDTO> datos = new ArrayList<>();
    private List<RolDTO> roles = new ArrayList<>();
    private boolean existeAdministrador;

    /** Usuario cargado en el formulario; null cuando se está creando uno nuevo. */
    private UsuarioDTO seleccionado;

    public VentanaUsuarios() {
        super("Gestión de usuarios", true, true, true, true);
        setSize(820, 560);

        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(crearPanelSuperior(), BorderLayout.NORTH);
        getContentPane().add(crearPanelTabla(), BorderLayout.CENTER);

        UtilVista.configurarTabla(tabla, modelo);

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnCambiarClave.addActionListener(e -> cambiarClave());
        btnEstado.addActionListener(e -> cambiarEstado());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarFila();
            }
        });

        cargarDatos();
        limpiarFormulario();
    }

    // ----------------------------------------------------------------- Paneles

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Datos del usuario"));
        form.add(new JLabel("Usuario:"), celda(0, 0));
        form.add(txtUsuario, celda(1, 0));
        form.add(new JLabel("Rol:"), celda(2, 0));
        form.add(cmbRol, celda(3, 0));
        form.add(new JLabel("Nombre completo:"), celda(0, 1));
        GridBagConstraints nombre = celda(1, 1);
        nombre.gridwidth = 3;
        form.add(txtNombre, nombre);
        form.add(new JLabel("Contraseña:"), celda(0, 2));
        form.add(txtClave, celda(1, 2));
        form.add(new JLabel("Confirmar:"), celda(2, 2));
        form.add(txtConfirmar, celda(3, 2));
        form.add(new JLabel("Estado:"), celda(0, 3));
        form.add(lblEstado, celda(1, 3));
        panel.add(form, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnActualizar);
        botones.add(btnCambiarClave);
        botones.add(btnEstado);
        botones.add(btnLimpiar);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(lblTotal, BorderLayout.SOUTH);
        return panel;
    }

    private static GridBagConstraints celda(int columna, int fila) {
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

    // ------------------------------------------------------------------- Datos

    /** Consulta usuarios, roles y si ya existe el Administrador, y llena la tabla. */
    private void cargarDatos() {
        try {
            if (roles.isEmpty()) {
                roles = controlador.listarRoles();
            }
            datos = controlador.listar();
            existeAdministrador = controlador.existeAdministrador();
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
            datos = new ArrayList<>();
        }

        UtilVista.limpiarTabla(modelo);
        for (UsuarioDTO u : datos) {
            modelo.addRow(new Object[]{
                    u.getNombreUsuario(),
                    u.getNombreCompleto(),
                    u.getNombreRol(),
                    u.isActivo() ? "Sí" : "No",
                    u.getFechaCreacion() == null ? "" : u.getFechaCreacion().format(FORMATO_FECHA)
            });
        }
        lblTotal.setText("Usuarios: " + datos.size());
    }

    /**
     * Llena el combo de roles. El rol Administrador solo se ofrece si todavía no
     * existe, o si el usuario seleccionado es justamente el administrador.
     */
    private void cargarRoles() {
        boolean seleccionadoEsAdmin = seleccionado != null
                && seleccionado.getCodigoRol() == RolDTO.ADMINISTRADOR;

        DefaultComboBoxModel<RolDTO> modeloRoles = new DefaultComboBoxModel<>();
        for (RolDTO r : roles) {
            boolean esAdmin = r.getCodigo() == RolDTO.ADMINISTRADOR;
            if (!esAdmin || !existeAdministrador || seleccionadoEsAdmin) {
                modeloRoles.addElement(r);
            }
        }
        cmbRol.setModel(modeloRoles);

        if (seleccionado != null) {
            for (int i = 0; i < modeloRoles.getSize(); i++) {
                if (modeloRoles.getElementAt(i).getCodigo() == seleccionado.getCodigoRol()) {
                    cmbRol.setSelectedIndex(i);
                }
            }
        } else {
            seleccionarRolPorDefecto(modeloRoles);
        }
        // El administrador no puede cambiarse de rol (sería quedarse sin administrador).
        cmbRol.setEnabled(!seleccionadoEsAdmin);
    }

    /** Para un usuario nuevo propone el rol Tradicional. */
    private void seleccionarRolPorDefecto(DefaultComboBoxModel<RolDTO> modeloRoles) {
        for (int i = 0; i < modeloRoles.getSize(); i++) {
            if (modeloRoles.getElementAt(i).getCodigo() == RolDTO.TRADICIONAL) {
                cmbRol.setSelectedIndex(i);
                return;
            }
        }
    }

    // --------------------------------------------------------------- Formulario

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= datos.size()) {
            return;
        }
        seleccionado = datos.get(fila);
        txtUsuario.setText(seleccionado.getNombreUsuario());
        txtNombre.setText(seleccionado.getNombreCompleto());
        txtClave.setText("");
        txtConfirmar.setText("");
        lblEstado.setText(seleccionado.isActivo() ? "Activo" : "Inactivo");
        cargarRoles();
        actualizarControles();
    }

    /** Deja el formulario listo para crear un usuario nuevo. */
    private void limpiarFormulario() {
        seleccionado = null;
        tabla.clearSelection();
        txtUsuario.setText("");
        txtNombre.setText("");
        txtClave.setText("");
        txtConfirmar.setText("");
        lblEstado.setText("Nuevo (quedará activo)");
        cargarRoles();
        actualizarControles();
        txtUsuario.requestFocusInWindow();
    }

    /**
     * Habilita botones y campos según el modo: crear (sin selección) o editar.
     * La contraseña solo se escribe al crear; para cambiarla está su botón.
     */
    private void actualizarControles() {
        boolean editando = seleccionado != null;
        boolean esYo = editando && Sesion.getUsuario() != null
                && Sesion.getUsuario().getCodigo() == seleccionado.getCodigo();

        btnGuardar.setEnabled(!editando);
        txtClave.setEnabled(!editando);
        txtConfirmar.setEnabled(!editando);
        btnActualizar.setEnabled(editando);
        btnCambiarClave.setEnabled(editando);
        btnEstado.setEnabled(editando && !esYo);
        btnEstado.setText(editando && !seleccionado.isActivo() ? "Activar" : "Desactivar");
    }

    /** Arma un DTO con lo escrito en el formulario (conserva código y estado si se edita). */
    private UsuarioDTO leerFormulario() {
        UsuarioDTO u = new UsuarioDTO();
        if (seleccionado != null) {
            u.setCodigo(seleccionado.getCodigo());
            u.setActivo(seleccionado.isActivo());
        }
        u.setNombreUsuario(txtUsuario.getText());
        u.setNombreCompleto(txtNombre.getText());
        RolDTO rol = (RolDTO) cmbRol.getSelectedItem();
        u.setCodigoRol(rol == null ? 0 : rol.getCodigo());
        return u;
    }

    // ---------------------------------------------------------------- Acciones

    private void guardar() {
        char[] clave = txtClave.getPassword();
        char[] confirmacion = txtConfirmar.getPassword();
        try {
            if (!Arrays.equals(clave, confirmacion)) {
                Arrays.fill(clave, '\0');
                UtilVista.mostrarError(this, "La contraseña y su confirmación no coinciden.");
                return;
            }
            controlador.crear(leerFormulario(), clave); // borra la clave al terminar
            UtilVista.mostrarInfo(this, "Usuario creado correctamente.");
            cargarDatos();
            limpiarFormulario();
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        } finally {
            Arrays.fill(confirmacion, '\0');
        }
    }

    private void actualizar() {
        if (seleccionado == null) {
            return;
        }
        try {
            controlador.actualizar(leerFormulario());
            UtilVista.mostrarInfo(this, "Usuario actualizado correctamente.");
            cargarDatos();
            limpiarFormulario();
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        }
    }

    /** Pide la nueva contraseña dos veces en un cuadro de diálogo. */
    private void cambiarClave() {
        if (seleccionado == null) {
            return;
        }
        JPasswordField nueva = new JPasswordField(18);
        JPasswordField confirmar = new JPasswordField(18);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Nueva contraseña:"), celda(0, 0));
        panel.add(nueva, celda(1, 0));
        panel.add(new JLabel("Confirmar:"), celda(0, 1));
        panel.add(confirmar, celda(1, 1));

        int opcion = JOptionPane.showConfirmDialog(this, panel,
                "Cambiar contraseña de «" + seleccionado.getNombreUsuario() + "»",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        char[] clave = nueva.getPassword();
        char[] confirmacion = confirmar.getPassword();
        try {
            if (opcion != JOptionPane.OK_OPTION) {
                return;
            }
            if (!Arrays.equals(clave, confirmacion)) {
                UtilVista.mostrarError(this, "La contraseña y su confirmación no coinciden.");
                return;
            }
            controlador.cambiarClave(seleccionado.getCodigo(), clave);
            UtilVista.mostrarInfo(this, "Contraseña actualizada correctamente.");
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        } finally {
            Arrays.fill(clave, '\0');
            Arrays.fill(confirmacion, '\0');
        }
    }

    private void cambiarEstado() {
        if (seleccionado == null) {
            return;
        }
        boolean activar = !seleccionado.isActivo();
        String accion = activar ? "activar" : "desactivar";
        if (!UtilVista.confirmar(this, "¿Desea " + accion + " al usuario «"
                + seleccionado.getNombreUsuario() + "»?")) {
            return;
        }
        try {
            controlador.cambiarEstado(seleccionado.getCodigo(), activar);
            UtilVista.mostrarInfo(this, "Usuario " + (activar ? "activado" : "desactivado") + " correctamente.");
            cargarDatos();
            limpiarFormulario();
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        }
    }
}
