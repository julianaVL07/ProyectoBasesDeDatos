package co.edu.uniquindio.proyectobasesdedatos.controlador;

import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.BitacoraDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.UsuarioDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.UsuarioDTO;
import co.edu.uniquindio.proyectobasesdedatos.util.ErroresBD;
import co.edu.uniquindio.proyectobasesdedatos.util.Seguridad;
import co.edu.uniquindio.proyectobasesdedatos.util.Sesion;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * Controlador del ingreso y la salida de la aplicación.
 *
 * <p>Al ingresar valida usuario y clave, registra la entrada en la Bitácora y
 * abre la {@link Sesion}. Al salir registra la salida y cierra la sesión.</p>
 */
public class LoginControlador {

    /** Mismo mensaje para usuario inexistente y clave mala: no revela cuál falló. */
    private static final String MSG_CREDENCIALES = "Usuario o contraseña incorrectos.";

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final BitacoraDAO bitacoraDAO = new BitacoraDAO();

    /**
     * Valida las credenciales e inicia la sesión.
     *
     * <p>Siempre borra el contenido de {@code clave} al terminar (con o sin
     * error), para que la contraseña no quede en memoria.</p>
     *
     * @param usuario nombre de usuario escrito en el Login
     * @param clave   contraseña tal como la entrega el {@code JPasswordField}
     * @return el usuario que ingresó (sin el hash de la clave)
     * @throws ControladorException si faltan datos, las credenciales no son
     *                              válidas, el usuario está inactivo o falla la BD
     */
    public UsuarioDTO ingresar(String usuario, char[] clave) throws ControladorException {
        try {
            if (usuario == null || usuario.isBlank()) {
                throw new ControladorException("Ingrese el nombre de usuario.");
            }
            if (clave == null || clave.length == 0) {
                throw new ControladorException("Ingrese la contraseña.");
            }

            UsuarioDTO u = usuarioDAO.buscarPorNombreUsuario(usuario.trim());
            if (u == null || !Seguridad.verificar(new String(clave), u.getContrasena())) {
                throw new ControladorException(MSG_CREDENCIALES);
            }
            if (!u.isActivo()) {
                throw new ControladorException("Usuario inactivo, contacte al administrador.");
            }

            int codigoBitacora = bitacoraDAO.registrarIngreso(u.getCodigo());
            u.setContrasena(null); // el hash no se guarda en la sesión
            Sesion.iniciar(u, codigoBitacora);
            return u;

        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        } finally {
            if (clave != null) {
                Arrays.fill(clave, '\0');
            }
        }
    }

    /**
     * Registra la salida en la Bitácora y cierra la sesión. Si no hay sesión no
     * hace nada, así que es seguro llamarlo dos veces (p. ej. desde "Salir" y
     * desde el cierre de la ventana).
     *
     * <p>La sesión se cierra aunque falle la base de datos.</p>
     *
     * @throws ControladorException si no se pudo registrar la salida
     */
    public void salir() throws ControladorException {
        if (!Sesion.haySesion()) {
            return;
        }
        int codigoBitacora = Sesion.getCodigoBitacora();
        try {
            if (codigoBitacora > 0) {
                bitacoraDAO.registrarSalida(codigoBitacora);
            }
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        } finally {
            Sesion.cerrar();
        }
    }
}
