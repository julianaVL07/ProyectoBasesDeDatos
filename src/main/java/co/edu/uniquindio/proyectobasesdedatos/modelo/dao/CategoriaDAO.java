package co.edu.uniquindio.proyectobasesdedatos.modelo.dao;

import co.edu.uniquindio.proyectobasesdedatos.config.ConexionOracle;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.CategoriaDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) de la tabla TCategoria: CRUD con JDBC puro.
 *
 * <p>Mismas reglas que {@link ProveedorDAO}: {@link PreparedStatement} con "?",
 * try-with-resources y sin mensajes al usuario (se lanza {@link SQLException}).</p>
 */
public class CategoriaDAO {

    // ---------------------------------------------------------------- INSERT

    /**
     * Inserta una categoría. El código (CaCodigo) lo genera Oracle (IDENTITY) y
     * se devuelve ya asignado en el mismo DTO.
     *
     * @return true si se insertó la fila
     * @throws SQLException p. ej. ORA-00001 (nombre repetido)
     */
    public boolean insertar(CategoriaDTO c) throws SQLException {
        String sql = "INSERT INTO TCategoria (CaNombre, CaDescripcion) VALUES (?, ?)";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"CACODIGO"})) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());

            boolean insertado = ps.executeUpdate() > 0;

            if (insertado) {
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (claves.next()) {
                        c.setCodigo(claves.getInt(1));
                    }
                }
            }
            return insertado;
        }
    }

    // ---------------------------------------------------------------- SELECT

    /**
     * Busca una categoría por su código.
     *
     * @return la categoría, o null si no existe
     */
    public CategoriaDTO buscarPorCodigo(int codigo) throws SQLException {
        String sql = SELECT_BASE + " WHERE CaCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Lista todas las categorías ordenadas por nombre.
     *
     * @return lista (vacía si no hay registros, nunca null)
     */
    public List<CategoriaDTO> listar() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY CaNombre";
        List<CategoriaDTO> lista = new ArrayList<>();

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
     * Filtra categorías por texto en el nombre o la descripción (búsqueda
     * parcial, sin distinguir mayúsculas).
     */
    public List<CategoriaDTO> buscarPorTexto(String texto) throws SQLException {
        String sql = SELECT_BASE
                + " WHERE UPPER(CaNombre) LIKE ? OR UPPER(CaDescripcion) LIKE ?"
                + " ORDER BY CaNombre";
        List<CategoriaDTO> lista = new ArrayList<>();
        String patron = "%" + texto.trim().toUpperCase() + "%";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    // ---------------------------------------------------------------- UPDATE

    /**
     * Actualiza nombre y descripción de la categoría identificada por su código.
     *
     * @return true si se modificó alguna fila (false si el código no existe)
     */
    public boolean actualizar(CategoriaDTO c) throws SQLException {
        String sql = "UPDATE TCategoria SET CaNombre = ?, CaDescripcion = ? WHERE CaCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDescripcion());
            ps.setInt(3, c.getCodigo());

            return ps.executeUpdate() > 0;
        }
    }

    // ---------------------------------------------------------------- DELETE

    /**
     * Elimina una categoría.
     *
     * <p>Si la categoría tiene productos, Oracle lanza ORA-02292 (llave foránea).</p>
     *
     * @return true si se eliminó la fila
     */
    public boolean eliminar(int codigo) throws SQLException {
        String sql = "DELETE FROM TCategoria WHERE CaCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);
            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------- Apoyo interno

    /** SELECT común de la tabla. */
    private static final String SELECT_BASE =
            "SELECT CaCodigo, CaNombre, CaDescripcion FROM TCategoria";

    /** Convierte la fila actual del ResultSet en un CategoriaDTO. */
    private CategoriaDTO mapear(ResultSet rs) throws SQLException {
        CategoriaDTO c = new CategoriaDTO();
        c.setCodigo(rs.getInt("CaCodigo"));
        c.setNombre(rs.getString("CaNombre"));
        c.setDescripcion(rs.getString("CaDescripcion"));
        return c;
    }
}
