package co.edu.uniquindio.proyectobasesdedatos.vista;

import co.edu.uniquindio.proyectobasesdedatos.ProyectoBasesDeDatos;
import co.edu.uniquindio.proyectobasesdedatos.controlador.ControladorException;
import co.edu.uniquindio.proyectobasesdedatos.controlador.LoginControlador;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.RolDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.UsuarioDTO;
import co.edu.uniquindio.proyectobasesdedatos.util.Sesion;
import co.edu.uniquindio.proyectobasesdedatos.vista.auditoria.VentanaBitacora;
import co.edu.uniquindio.proyectobasesdedatos.vista.auditoria.VentanaUsuarios;
import co.edu.uniquindio.proyectobasesdedatos.vista.consultas.VentanaConsultas;
import co.edu.uniquindio.proyectobasesdedatos.vista.entidades.VentanaCategorias;
import co.edu.uniquindio.proyectobasesdedatos.vista.entidades.VentanaCiudades;
import co.edu.uniquindio.proyectobasesdedatos.vista.entidades.VentanaDepartamentos;
import co.edu.uniquindio.proyectobasesdedatos.vista.entidades.VentanaProductos;
import co.edu.uniquindio.proyectobasesdedatos.vista.entidades.VentanaProveedores;
import co.edu.uniquindio.proyectobasesdedatos.vista.transacciones.VentanaSuministros;
import co.edu.uniquindio.proyectobasesdedatos.vista.util.UtilVista;

import javax.swing.BorderFactory;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;

/**
 * Ventana principal de la aplicación (se abre después del Login).
 *
 * <p>Contiene un {@link JDesktopPane} donde se abren los formularios como
 * ventanas internas ({@link JInternalFrame}), la barra de menús y una barra de
 * estado con el usuario y el rol de la {@link Sesion}.</p>
 *
 * <p>Los menús se muestran según el rol (ver tabla de permisos en {@link Sesion}):</p>
 * <ul>
 *   <li><b>Entidades</b> y <b>Transacciones</b>: Administrador y Tradicional.</li>
 *   <li><b>Reportes/Consultas</b>: todos los usuarios.</li>
 *   <li><b>Auditoría</b>: solo Administrador.</li>
 *   <li><b>Sesión</b>: todos.</li>
 * </ul>
 */
public class MenuPrincipal extends JFrame {

    private final JDesktopPane escritorio = new JDesktopPane();
    private final JLabel lblEstado = new JLabel();
    private final LoginControlador loginControlador = new LoginControlador();

    /** Crea la ventana principal con los menús que permite el rol del usuario en sesión. */
    public MenuPrincipal() {
        super("Proyecto Final Bases de Datos I — Gestión de Proveedores");
        // La X de la ventana pasa por salir() para registrar la salida en la Bitácora.
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });
        setMinimumSize(new Dimension(900, 600));
        setSize(1200, 750);
        setLocationRelativeTo(null);

        escritorio.setBackground(new Color(236, 240, 245));

        setJMenuBar(crearBarraMenu());
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(escritorio, BorderLayout.CENTER);
        getContentPane().add(crearBarraEstado(), BorderLayout.SOUTH);
        actualizarBarraEstado();
    }

    // ------------------------------------------------------------------ Menús

    private JMenuBar crearBarraMenu() {
        JMenuBar barra = new JMenuBar();

        if (Sesion.puedeEditar()) {
            barra.add(crearMenuEntidades());
            barra.add(crearMenuTransacciones());
        }
        barra.add(crearMenuReportes());
        if (Sesion.esAdmin()) {
            barra.add(crearMenuAuditoria());
        }
        barra.add(crearMenuSesion());
        return barra;
    }

    private JMenu crearMenuEntidades() {
        JMenu menu = new JMenu("Entidades");

        menu.add(crearItem("Departamentos", () -> abrirVentana(new VentanaDepartamentos())));
        menu.add(crearItem("Ciudades", () -> abrirVentana(new VentanaCiudades())));
        menu.add(new JSeparator());
        menu.add(crearItem("Categorías", () -> abrirVentana(new VentanaCategorias())));
        menu.add(crearItem("Productos", () -> abrirVentana(new VentanaProductos())));
        menu.add(new JSeparator());
        menu.add(crearItem("Proveedores", () -> abrirVentana(new VentanaProveedores())));
        return menu;
    }

    private JMenu crearMenuTransacciones() {
        JMenu menu = new JMenu("Transacciones");
        menu.add(crearItem("Suministros", () -> abrirVentana(new VentanaSuministros())));
        return menu;
    }

    private JMenu crearMenuReportes() {
        JMenu menu = new JMenu("Reportes/Consultas");
        menu.add(crearItem("Consultas", () -> abrirVentana(new VentanaConsultas())));
        // TODO Sara: abrir la ventana de Reportes (Fase 5).
        menu.add(crearItem("Reportes", this::enConstruccion));
        return menu;
    }

    private JMenu crearMenuAuditoria() {
        JMenu menu = new JMenu("Auditoría");
        menu.add(crearItem("Gestión de usuarios", () -> abrirVentana(new VentanaUsuarios())));
        menu.add(crearItem("Bitácora", () -> abrirVentana(new VentanaBitacora())));
        return menu;
    }

    private JMenu crearMenuSesion() {
        JMenu menu = new JMenu("Sesión");
        menu.add(crearItem("Cerrar sesión", this::cerrarSesion));
        menu.add(new JSeparator());
        menu.add(crearItem("Salir", this::salir));
        return menu;
    }

    /** Crea una opción de menú que ejecuta la acción dada. */
    private JMenuItem crearItem(String texto, Runnable accion) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(e -> accion.run());
        return item;
    }

    // --------------------------------------------------------- Barra de estado

    private JPanel crearBarraEstado() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        panel.add(lblEstado);
        return panel;
    }

    /** Muestra en la barra de estado el usuario y el rol de la sesión actual. */
    public void actualizarBarraEstado() {
        UsuarioDTO u = Sesion.getUsuario();
        if (u == null) {
            lblEstado.setText("Sin sesión iniciada");
            return;
        }
        lblEstado.setText("Usuario: " + u.getNombreUsuario()
                + "  |  Nombre: " + u.getNombreCompleto()
                + "  |  Rol: " + nombreRol(u));
    }

    /** Nombre del rol: el del JOIN si viene, si no se deduce del código. */
    private String nombreRol(UsuarioDTO u) {
        if (u.getNombreRol() != null) {
            return u.getNombreRol();
        }
        return switch (u.getCodigoRol()) {
            case RolDTO.ADMINISTRADOR -> "Administrador";
            case RolDTO.TRADICIONAL -> "Tradicional";
            case RolDTO.ESPORADICO -> "Esporádico";
            default -> "Desconocido";
        };
    }

    // ------------------------------------------------------ Ventanas internas

    /**
     * Abre una ventana interna en el escritorio. Si ya hay una ventana abierta de
     * la misma clase, no crea otra: trae al frente la existente.
     *
     * @param ventana ventana a mostrar
     */
    public void abrirVentana(JInternalFrame ventana) {
        for (JInternalFrame abierta : escritorio.getAllFrames()) {
            if (abierta.getClass().equals(ventana.getClass())) {
                activar(abierta);
                ventana.dispose();
                return;
            }
        }
        escritorio.add(ventana);
        if (ventana.getWidth() == 0 || ventana.getHeight() == 0) {
            ventana.pack();
        }
        UtilVista.centrar(ventana, escritorio);
        ventana.setVisible(true);
        activar(ventana);
    }

    /** Trae al frente y selecciona una ventana interna (la restaura si estaba minimizada). */
    private void activar(JInternalFrame ventana) {
        try {
            if (ventana.isIcon()) {
                ventana.setIcon(false);
            }
            ventana.toFront();
            ventana.setSelected(true);
        } catch (PropertyVetoException ignorada) {
            // La ventana rechazó el cambio de estado; basta con haberla traído al frente.
        }
    }

    /** @return el escritorio donde se abren las ventanas internas */
    public JDesktopPane getEscritorio() {
        return escritorio;
    }

    // ---------------------------------------------------------------- Acciones

    private void enConstruccion() {
        UtilVista.mostrarInfo(this, "Esta opción está en construcción.");
    }

    /** Registra la salida en la Bitácora, cierra la ventana y vuelve al Login. */
    private void cerrarSesion() {
        if (UtilVista.confirmar(this, "¿Desea cerrar la sesión?")) {
            registrarSalida();
            dispose();
            ProyectoBasesDeDatos.abrirAplicacion();
        }
    }

    /** Registra la salida en la Bitácora y termina la aplicación. */
    private void salir() {
        if (UtilVista.confirmar(this, "¿Desea salir de la aplicación?")) {
            registrarSalida();
            dispose();
            System.exit(0);
        }
    }

    /**
     * Registra la salida y cierra la {@link Sesion}. Si falla la base de datos
     * solo se avisa: la sesión se cierra de todas formas.
     */
    private void registrarSalida() {
        try {
            loginControlador.salir();
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, "No se pudo registrar la salida en la bitácora.\n"
                    + e.getMessage());
        }
    }
}
