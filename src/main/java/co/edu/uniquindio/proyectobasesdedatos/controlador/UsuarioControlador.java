package co.edu.uniquindio.proyectobasesdedatos.controlador;

import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.RolDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.UsuarioDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.RolDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.UsuarioDTO;
import co.edu.uniquindio.proyectobasesdedatos.util.ErroresBD;
import co.edu.uniquindio.proyectobasesdedatos.util.Seguridad;
import co.edu.uniquindio.proyectobasesdedatos.util.Sesion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Controlador de la Gestión de Usuarios (módulo Auditoría).
 *
 * <p>Todas las operaciones exigen {@link Sesion#puedeGestionarUsuarios()}, es
 * decir, solo las puede hacer el Administrador. Reglas que se validan:</p>
 * <ul>
 *   <li>Nombre de usuario de 4 a 30 caracteres y sin espacios.</li>
 *   <li>Contraseña de al menos 6 caracteres (se guarda su hash SHA-256).</li>
 *   <li>Solo puede existir un Administrador.</li>
 *   <li>El Administrador no puede desactivarse ni quitarse el rol a sí mismo.</li>
 * </ul>
 */
public class UsuarioControlador {

    private static final int USUARIO_MIN = 4;
    private static final int USUARIO_MAX = 30;
    private static final int NOMBRE_MAX = 100;
    private static final int CLAVE_MIN = 6;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();

    // ---------------------------------------------------------------- Lectura

    /** @return todos los usuarios (sin el hash de la clave) */
    public List<UsuarioDTO> listar() throws ControladorException {
        verificarPermiso();
        try {
            return usuarioDAO.listar();
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    /** @return los roles disponibles para el combo de la vista */
    public List<RolDTO> listarRoles() throws ControladorException {
        verificarPermiso();
        try {
            return rolDAO.listar();
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    /** @return true si ya hay un Administrador (la vista no ofrece ese rol en ese caso) */
    public boolean existeAdministrador() throws ControladorException {
        verificarPermiso();
        try {
            return usuarioDAO.existeAdministrador();
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    // ---------------------------------------------------------------- Escritura

    /**
     * Crea un usuario nuevo (queda activo). La clave se borra de memoria al terminar.
     *
     * @param u     datos del usuario (nombre de usuario, nombre completo y rol)
     * @param clave contraseña en texto plano
     * @throws ControladorException si algún dato no es válido o falla la BD
     */
    public void crear(UsuarioDTO u, char[] clave) throws ControladorException {
        try {
            verificarPermiso();
            validarDatos(u);
            validarClave(clave);
            if (u.getCodigoRol() == RolDTO.ADMINISTRADOR && usuarioDAO.existeAdministrador()) {
                throw new ControladorException("Ya existe un administrador; solo puede haber uno.");
            }
            u.setActivo(true);
            u.setContrasena(Seguridad.hashear(new String(clave)));
            usuarioDAO.insertar(u);
        } catch (SQLException e) {
            throw new ControladorException(traducir(e), e);
        } finally {
            if (u != null) {
                u.setContrasena(null); // el hash no vuelve a la vista
            }
            limpiar(clave);
        }
    }

    /**
     * Actualiza nombre de usuario, nombre completo, rol y estado (no la clave).
     *
     * @throws ControladorException si algún dato no es válido, se rompe la regla
     *                              del único administrador o falla la BD
     */
    public void actualizar(UsuarioDTO u) throws ControladorException {
        verificarPermiso();
        validarDatos(u);
        try {
            UsuarioDTO actual = usuarioDAO.buscarPorCodigo(u.getCodigo());
            if (actual == null) {
                throw new ControladorException("El usuario no existe.");
            }
            boolean eraAdmin = actual.getCodigoRol() == RolDTO.ADMINISTRADOR;
            boolean seraAdmin = u.getCodigoRol() == RolDTO.ADMINISTRADOR;

            if (esUsuarioEnSesion(u.getCodigo())) {
                if (eraAdmin && !seraAdmin) {
                    throw new ControladorException("No puede quitarse a sí mismo el rol de administrador.");
                }
                if (!u.isActivo()) {
                    throw new ControladorException("No puede desactivar su propio usuario.");
                }
            }
            if (seraAdmin && !eraAdmin && usuarioDAO.existeAdministrador()) {
                throw new ControladorException("Ya existe un administrador; solo puede haber uno.");
            }

            if (!usuarioDAO.actualizar(u)) {
                throw new ControladorException("El usuario no existe.");
            }
            if (esUsuarioEnSesion(u.getCodigo())) {
                // Mantiene al día los datos que muestra la barra de estado.
                UsuarioDTO enSesion = Sesion.getUsuario();
                enSesion.setNombreUsuario(u.getNombreUsuario());
                enSesion.setNombreCompleto(u.getNombreCompleto());
            }
        } catch (SQLException e) {
            throw new ControladorException(traducir(e), e);
        }
    }

    /**
     * Cambia la contraseña de un usuario. La clave se borra de memoria al terminar.
     *
     * @param codigo UCodigo del usuario
     * @param clave  nueva contraseña en texto plano
     */
    public void cambiarClave(int codigo, char[] clave) throws ControladorException {
        try {
            verificarPermiso();
            validarClave(clave);
            if (!usuarioDAO.cambiarClave(codigo, Seguridad.hashear(new String(clave)))) {
                throw new ControladorException("El usuario no existe.");
            }
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        } finally {
            limpiar(clave);
        }
    }

    /**
     * Activa o desactiva un usuario. El administrador no puede desactivarse a sí mismo.
     *
     * @param codigo UCodigo del usuario
     * @param activo nuevo estado
     */
    public void cambiarEstado(int codigo, boolean activo) throws ControladorException {
        verificarPermiso();
        if (!activo && esUsuarioEnSesion(codigo)) {
            throw new ControladorException("No puede desactivar su propio usuario.");
        }
        try {
            if (!usuarioDAO.cambiarEstado(codigo, activo)) {
                throw new ControladorException("El usuario no existe.");
            }
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    // ------------------------------------------------------- Apoyo interno

    /**
     * Elimina un usuario. No se permite eliminar al Administrador ni al usuario
     * que tiene la sesión abierta. Si el usuario ya tiene registros en la
     * Bitácora se lanza {@link UsuarioConBitacoraException} para que la vista
     * ofrezca desactivarlo en su lugar.
     *
     * @param codigo UCodigo del usuario
     */
    public void eliminar(int codigo) throws ControladorException {
        verificarPermiso();
        if (codigo <= 0) {
            throw new ControladorException("Seleccione un usuario de la tabla.");
        }
        if (esUsuarioEnSesion(codigo)) {
            throw new ControladorException("No puede eliminar su propio usuario.");
        }
        try {
            UsuarioDTO actual = usuarioDAO.buscarPorCodigo(codigo);
            if (actual == null) {
                throw new ControladorException("El usuario ya no existe.");
            }
            if (actual.getCodigoRol() == RolDTO.ADMINISTRADOR) {
                throw new ControladorException("No se puede eliminar al administrador.");
            }
            if (!usuarioDAO.eliminar(codigo)) {
                throw new ControladorException("El usuario ya no existe.");
            }
        } catch (SQLException e) {
            if (ErroresBD.tieneRegistrosAsociados(e)) {
                throw new UsuarioConBitacoraException(
                        "No se puede eliminar el usuario porque tiene registros en la Bitácora.", e);
            }
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    private void verificarPermiso() throws ControladorException {
        if (!Sesion.puedeGestionarUsuarios()) {
            throw new ControladorException("Solo el administrador puede gestionar usuarios.");
        }
    }

    /** Valida nombre de usuario, nombre completo y rol; además quita espacios sobrantes. */
    private void validarDatos(UsuarioDTO u) throws ControladorException {
        if (u == null) {
            throw new ControladorException("No hay datos del usuario.");
        }
        String usuario = u.getNombreUsuario() == null ? "" : u.getNombreUsuario().trim();
        if (usuario.length() < USUARIO_MIN || usuario.length() > USUARIO_MAX) {
            throw new ControladorException("El nombre de usuario debe tener entre "
                    + USUARIO_MIN + " y " + USUARIO_MAX + " caracteres.");
        }
        if (usuario.chars().anyMatch(Character::isWhitespace)) {
            throw new ControladorException("El nombre de usuario no puede tener espacios.");
        }
        String nombre = u.getNombreCompleto() == null ? "" : u.getNombreCompleto().trim();
        if (nombre.isEmpty()) {
            throw new ControladorException("Ingrese el nombre completo.");
        }
        if (nombre.length() > NOMBRE_MAX) {
            throw new ControladorException("El nombre completo no puede superar "
                    + NOMBRE_MAX + " caracteres.");
        }
        int rol = u.getCodigoRol();
        if (rol != RolDTO.ADMINISTRADOR && rol != RolDTO.TRADICIONAL && rol != RolDTO.ESPORADICO) {
            throw new ControladorException("Seleccione un rol válido.");
        }
        u.setNombreUsuario(usuario);
        u.setNombreCompleto(nombre);
    }

    private void validarClave(char[] clave) throws ControladorException {
        if (clave == null || clave.length < CLAVE_MIN) {
            throw new ControladorException("La contraseña debe tener al menos "
                    + CLAVE_MIN + " caracteres.");
        }
    }

    private boolean esUsuarioEnSesion(int codigo) {
        UsuarioDTO enSesion = Sesion.getUsuario();
        return enSesion != null && enSesion.getCodigo() == codigo;
    }

    private void limpiar(char[] clave) {
        if (clave != null) {
            Arrays.fill(clave, '\0');
        }
    }

    /**
     * Mensaje para los errores de TUsuario: distingue cuál restricción única
     * falló (nombre repetido o segundo administrador); el resto lo traduce ErroresBD.
     */
    private String traducir(SQLException e) {
        String detalle = e.getMessage() == null ? "" : e.getMessage().toUpperCase();
        if (detalle.contains("UX_USUARIO_UNICO_ADMIN")) {
            return "Ya existe un administrador; solo puede haber uno.";
        }
        if (detalle.contains("UQ_USUARIO_NOMBRE")) {
            return "Ya existe un usuario con ese nombre de usuario.";
        }
        return ErroresBD.traducir(e);
    }
}
