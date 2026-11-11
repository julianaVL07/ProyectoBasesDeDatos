package co.edu.uniquindio.proyectobasesdedatos.vista;

import co.edu.uniquindio.proyectobasesdedatos.controlador.ControladorException;
import co.edu.uniquindio.proyectobasesdedatos.controlador.LoginControlador;
import co.edu.uniquindio.proyectobasesdedatos.vista.util.UtilVista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Ventana de ingreso (Login). Es un diálogo modal: {@link #mostrar()} bloquea
 * hasta que el usuario ingresa correctamente, cancela o agota los intentos.
 *
 * <p>Enter en cualquier campo equivale a pulsar <b>Ingresar</b>. Después de
 * {@value #MAX_INTENTOS} intentos fallidos la ventana se cierra.</p>
 */
public class VentanaLogin extends JDialog {

    /** Intentos fallidos permitidos antes de cerrar la ventana. */
    public static final int MAX_INTENTOS = 3;

    private final LoginControlador controlador = new LoginControlador();

    private final JTextField txtUsuario = new JTextField(18);
    private final JPasswordField txtClave = new JPasswordField(18);
    private final JButton btnIngresar = new JButton("Ingresar");
    private final JButton btnCancelar = new JButton("Cancelar");

    private int intentosFallidos;
    private boolean ingreso;

    private VentanaLogin() {
        super((java.awt.Frame) null, "Ingreso al sistema", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(crearEncabezado(), BorderLayout.NORTH);
        getContentPane().add(crearFormulario(), BorderLayout.CENTER);
        getContentPane().add(crearBotones(), BorderLayout.SOUTH);

        btnIngresar.addActionListener(e -> ingresar());
        btnCancelar.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnIngresar); // Enter = Ingresar

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Muestra el Login y espera a que se cierre.
     *
     * @return true si el usuario ingresó (la {@code Sesion} quedó iniciada);
     *         false si canceló o agotó los intentos
     */
    public static boolean mostrar() {
        VentanaLogin login = new VentanaLogin();
        login.setVisible(true); // bloquea por ser modal
        return login.ingreso;
    }

    // ----------------------------------------------------------------- Paneles

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        JLabel titulo = new JLabel("Gestión de Proveedores", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(titulo, BorderLayout.CENTER);
        panel.add(new JLabel("Ingrese sus credenciales", SwingConstants.CENTER), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
        panel.add(new JLabel("Usuario:"), celda(0, 0));
        panel.add(txtUsuario, celda(1, 0));
        panel.add(new JLabel("Contraseña:"), celda(0, 1));
        panel.add(txtClave, celda(1, 1));
        return panel;
    }

    private JPanel crearBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        panel.add(btnIngresar);
        panel.add(btnCancelar);
        return panel;
    }

    private static GridBagConstraints celda(int columna, int fila) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = columna;
        g.gridy = fila;
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        g.fill = columna == 1 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        g.weightx = columna == 1 ? 1.0 : 0.0;
        return g;
    }

    // ---------------------------------------------------------------- Acciones

    private void ingresar() {
        String usuario = txtUsuario.getText();
        char[] clave = txtClave.getPassword();
        // Un intento cuenta solo si se escribieron ambos datos.
        boolean camposLlenos = !usuario.isBlank() && clave.length > 0;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            controlador.ingresar(usuario, clave); // borra el arreglo clave al terminar
            ingreso = true;
            dispose();
        } catch (ControladorException e) {
            setCursor(Cursor.getDefaultCursor());
            if (camposLlenos) {
                intentosFallidos++;
            }
            int restantes = MAX_INTENTOS - intentosFallidos;
            if (restantes <= 0) {
                UtilVista.mostrarError(this, e.getMessage()
                        + "\nSe agotaron los " + MAX_INTENTOS + " intentos. La aplicación se cerrará.");
                dispose();
                return;
            }
            UtilVista.mostrarError(this, camposLlenos
                    ? e.getMessage() + "\nIntentos restantes: " + restantes
                    : e.getMessage());
            txtClave.setText("");
            (usuario.isBlank() ? txtUsuario : txtClave).requestFocusInWindow();
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}
