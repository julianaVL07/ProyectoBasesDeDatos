package co.edu.uniquindio.proyectobasesdedatos.util;

import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.RolDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.UsuarioDTO;

/**
 * Guarda quién está usando la aplicación en este momento y qué puede hacer.
 *
 * <p>Es una app de escritorio con un solo usuario a la vez, así que basta con
 * datos estáticos. Flujo esperado:</p>
 * <ol>
 *   <li>El LoginControlador valida usuario/clave, registra la entrada en la
 *       Bitácora y llama {@link #iniciar(UsuarioDTO, int)}.</li>
 *   <li>El MenuPrincipal y los controladores consultan {@link #esAdmin()},
 *       {@link #puedeEditar()}, etc. para habilitar opciones.</li>
 *   <li>Al cerrar sesión se usa {@link #getCodigoBitacora()} para registrar la
 *       salida y luego {@link #cerrar()}.</li>
 * </ol>
 *
 * <p>Permisos (según el enunciado):</p>
 * <pre>
 *                      Admin  Tradicional  Esporádico
 * Gestionar usuarios     sí       no          no
 * Ver bitácora           sí       no          no
 * CRUD (entidades/trx)   sí       sí          no
 * Consultas y reportes   sí       sí          sí
 * </pre>
 */
public final class Sesion {

    private static UsuarioDTO usuario;
    private static int codigoBitacora;

    private Sesion() {
    }

    /**
     * Abre la sesión del usuario que acaba de ingresar.
     *
     * @param usuarioLogueado usuario validado (no null)
     * @param codigoBitacora  BCodigo del registro de ingreso en TBitacora
     */
    public static void iniciar(UsuarioDTO usuarioLogueado, int codigoBitacora) {
        if (usuarioLogueado == null) {
            throw new IllegalArgumentException("El usuario no puede ser null");
        }
        Sesion.usuario = usuarioLogueado;
        Sesion.codigoBitacora = codigoBitacora;
    }

    /** Cierra la sesión (registrar antes la salida en la Bitácora). */
    public static void cerrar() {
        usuario = null;
        codigoBitacora = 0;
    }

    public static boolean haySesion() {
        return usuario != null;
    }

    /** @return el usuario logueado, o null si no hay sesión */
    public static UsuarioDTO getUsuario() {
        return usuario;
    }

    /** @return BCodigo del ingreso actual en la Bitácora (0 si no hay sesión) */
    public static int getCodigoBitacora() {
        return codigoBitacora;
    }

    // ---------------------------------------------------------------- Permisos

    public static boolean esAdmin() {
        return tieneRol(RolDTO.ADMINISTRADOR);
    }

    /** Puede crear, editar y borrar usuarios. Solo el Administrador. */
    public static boolean puedeGestionarUsuarios() {
        return esAdmin();
    }

    /** Puede ver la Bitácora de entradas y salidas. Solo el Administrador. */
    public static boolean puedeVerBitacora() {
        return esAdmin();
    }

    /** Puede ejecutar los CRUD de entidades y transacciones. */
    public static boolean puedeEditar() {
        return tieneRol(RolDTO.ADMINISTRADOR) || tieneRol(RolDTO.TRADICIONAL);
    }

    /** Puede ejecutar consultas y reportes. Cualquier usuario con sesión. */
    public static boolean puedeConsultar() {
        return haySesion();
    }

    private static boolean tieneRol(int codigoRol) {
        return usuario != null && usuario.getCodigoRol() == codigoRol;
    }
}
