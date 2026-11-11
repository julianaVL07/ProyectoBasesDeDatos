package co.edu.uniquindio.proyectobasesdedatos;

import co.edu.uniquindio.proyectobasesdedatos.config.ConexionOracle;
import co.edu.uniquindio.proyectobasesdedatos.vista.MenuPrincipal;
import co.edu.uniquindio.proyectobasesdedatos.vista.VentanaLogin;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada de la aplicación.
 *
 * <p>Flujo: aplica el Look and Feel del sistema operativo, verifica la conexión
 * a Oracle, muestra el Login y, si el usuario ingresa, abre el
 * {@link MenuPrincipal}.</p>
 *
 * @author julia
 */
public class ProyectoBasesDeDatos {

    public static void main(String[] args) {
        usarLookAndFeelDelSistema();

        // Toda la interfaz Swing se crea en el hilo de eventos (EDT).
        SwingUtilities.invokeLater(() -> {
            if (!ConexionOracle.probarConexion()) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo conectar a la base de datos.\n"
                        + "Verifique que Oracle esté activo y revise db.properties.",
                        "Error de conexión", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            abrirAplicacion();
        });
    }

    /**
     * Muestra el Login y, si el usuario ingresa, abre el menú principal. Si
     * cancela o agota los intentos, la aplicación termina.
     *
     * <p>También se usa al cerrar sesión para volver al Login.</p>
     */
    public static void abrirAplicacion() {
        if (VentanaLogin.mostrar()) {
            new MenuPrincipal().setVisible(true);
        } else {
            System.exit(0);
        }
    }

    /** Usa la apariencia nativa (Windows, macOS, GTK); si falla, deja la de Java. */
    private static void usarLookAndFeelDelSistema() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo aplicar el Look and Feel del sistema: " + e.getMessage());
        }
    }
}
