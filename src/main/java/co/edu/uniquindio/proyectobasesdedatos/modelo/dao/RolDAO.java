package co.edu.uniquindio.proyectobasesdedatos.modelo.dao;

import co.edu.uniquindio.proyectobasesdedatos.config.ConexionOracle;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.RolDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la tabla TRol (solo lectura).
 *
 * <p>Los roles son fijos (los inserta {@code sql/02_datos_iniciales.sql}), por eso
 * no hay métodos para insertar, actualizar ni eliminar.</p>
 */
public class RolDAO {

    /**
     * Lista todos los roles ordenados por código (Administrador, Tradicional, Esporádico).
     *
     * @return lista (vacía si no hay registros, nunca null)
     */
    public List<RolDTO> listar() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY r.RCodigo";
        List<RolDTO> lista = new ArrayList<>();

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Busca un rol por su código.
     *
     * @return el rol, o null si no existe
     */
    public RolDTO buscarPorCodigo(int codigo) throws SQLException {
        String sql = SELECT_BASE + " WHERE r.RCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    // ------------------------------------------------------- Apoyo interno

    /** SELECT común de TRol. */
    private static final String SELECT_BASE =
            "SELECT r.RCodigo, r.RNombre, r.RDescripcion FROM TRol r";

    /** Convierte la fila actual del ResultSet en un RolDTO. */
    private RolDTO mapear(ResultSet rs) throws SQLException {
        return new RolDTO(
                rs.getInt("RCodigo"),
                rs.getString("RNombre"),
                rs.getString("RDescripcion"));
    }
}
