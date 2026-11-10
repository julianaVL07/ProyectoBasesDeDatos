package co.edu.uniquindio.proyectobasesdedatos.modelo.dao;

import co.edu.uniquindio.proyectobasesdedatos.config.ConexionOracle;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.RolDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.UsuarioDTO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la tabla TUsuario (con JOIN a TRol para traer el nombre del rol).
 *
 * <p>La contraseña que recibe y devuelve este DAO es SIEMPRE el hash SHA-256
 * (lo calcula el controlador con {@code Seguridad.hashear}); aquí nunca se
 * maneja la clave en texto plano.</p>
 *
 * <p>Solo puede existir un Administrador: si se intenta guardar un segundo,
 * Oracle lanza ORA-00001 por el índice único {@code ux_usuario_unico_admin}.</p>
 */
public class UsuarioDAO {

    // ---------------------------------------------------------------- INSERT

    /**
     * Inserta un usuario. UCodigo lo genera Oracle (IDENTITY) y UFechaCreacion
     * toma el DEFAULT SYSDATE; el código generado queda asignado en el DTO.
     *
     * @param u usuario con {@code contrasena} = hash SHA-256 ya calculado
     * @return true si se insertó la fila
     * @throws SQLException p. ej. ORA-00001 (usuario repetido o segundo administrador)
     */
    public boolean insertar(UsuarioDTO u) throws SQLException {
        String sql = "INSERT INTO TUsuario "
                + "(UNombreUsuario, UContrasena, UActivo, UNombreCompleto, RCodigo) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"UCODIGO"})) {

            ps.setString(1, u.getNombreUsuario());
            ps.setString(2, u.getContrasena());
            ps.setInt(3, u.isActivo() ? 1 : 0);
            ps.setString(4, u.getNombreCompleto());
            ps.setInt(5, u.getCodigoRol());

            boolean insertado = ps.executeUpdate() > 0;

            if (insertado) {
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (claves.next()) {
                        u.setCodigo(claves.getInt(1));
                    }
                }
            }
            return insertado;
        }
    }

    // ---------------------------------------------------------------- SELECT

    /**
     * Busca un usuario por su código.
     *
     * @return el usuario (con el hash de su clave), o null si no existe
     */
    public UsuarioDTO buscarPorCodigo(int codigo) throws SQLException {
        String sql = SELECT_BASE + " WHERE u.UCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Busca un usuario por su nombre de usuario (lo usa el Login). La búsqueda
     * es exacta, distingue mayúsculas, igual que la restricción UNIQUE.
     *
     * @return el usuario (con el hash de su clave), o null si no existe
     */
    public UsuarioDTO buscarPorNombreUsuario(String nombreUsuario) throws SQLException {
        String sql = SELECT_BASE + " WHERE u.UNombreUsuario = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Lista todos los usuarios ordenados por nombre de usuario. Por seguridad,
     * los DTO de la lista NO llevan el hash de la clave (contrasena = null).
     *
     * @return lista (vacía si no hay registros, nunca null)
     */
    public List<UsuarioDTO> listar() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY u.UNombreUsuario";
        List<UsuarioDTO> lista = new ArrayList<>();

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UsuarioDTO u = mapear(rs);
                u.setContrasena(null);
                lista.add(u);
            }
        }
        return lista;
    }

    /**
     * Indica si ya existe un usuario con rol Administrador.
     *
     * @return true si hay al menos uno (por el índice único, como máximo habrá uno)
     */
    public boolean existeAdministrador() throws SQLException {
        String sql = "SELECT COUNT(*) FROM TUsuario WHERE RCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, RolDTO.ADMINISTRADOR);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ---------------------------------------------------------------- UPDATE

    /**
     * Actualiza nombre de usuario, nombre completo, estado y rol. NO cambia la
     * contraseña (para eso está {@link #cambiarClave(int, String)}).
     *
     * @return true si se modificó alguna fila (false si el código no existe)
     */
    public boolean actualizar(UsuarioDTO u) throws SQLException {
        String sql = "UPDATE TUsuario SET "
                + "UNombreUsuario = ?, UNombreCompleto = ?, UActivo = ?, RCodigo = ? "
                + "WHERE UCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u.getNombreUsuario());
            ps.setString(2, u.getNombreCompleto());
            ps.setInt(3, u.isActivo() ? 1 : 0);
            ps.setInt(4, u.getCodigoRol());
            ps.setInt(5, u.getCodigo());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cambia la contraseña de un usuario.
     *
     * @param codigo  UCodigo del usuario
     * @param hash    hash SHA-256 de la nueva clave
     * @return true si se modificó la fila
     */
    public boolean cambiarClave(int codigo, String hash) throws SQLException {
        String sql = "UPDATE TUsuario SET UContrasena = ? WHERE UCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, hash);
            ps.setInt(2, codigo);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Activa o desactiva un usuario (en lugar de borrarlo, así se conserva su
     * historial en la Bitácora).
     *
     * @return true si se modificó la fila
     */
    public boolean cambiarEstado(int codigo, boolean activo) throws SQLException {
        String sql = "UPDATE TUsuario SET UActivo = ? WHERE UCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, activo ? 1 : 0);
            ps.setInt(2, codigo);
            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------- Apoyo interno

    /** SELECT común: trae el usuario y el nombre de su rol (JOIN con TRol). */
    private static final String SELECT_BASE =
            "SELECT u.UCodigo, u.UNombreUsuario, u.UContrasena, u.UActivo, "
            + "u.UFechaCreacion, u.UNombreCompleto, u.RCodigo, r.RNombre "
            + "FROM TUsuario u "
            + "JOIN TRol r ON r.RCodigo = u.RCodigo";

    /** Convierte la fila actual del ResultSet en un UsuarioDTO. */
    private UsuarioDTO mapear(ResultSet rs) throws SQLException {
        UsuarioDTO u = new UsuarioDTO();
        u.setCodigo(rs.getInt("UCodigo"));
        u.setNombreUsuario(rs.getString("UNombreUsuario"));
        u.setContrasena(rs.getString("UContrasena"));
        u.setActivo(rs.getInt("UActivo") == 1);
        Date fecha = rs.getDate("UFechaCreacion");
        u.setFechaCreacion(fecha != null ? fecha.toLocalDate() : null);
        u.setNombreCompleto(rs.getString("UNombreCompleto"));
        u.setCodigoRol(rs.getInt("RCodigo"));
        u.setNombreRol(rs.getString("RNombre"));
        return u;
    }
}
