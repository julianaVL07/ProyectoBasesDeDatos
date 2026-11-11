package co.edu.uniquindio.proyectobasesdedatos.modelo.dao;

import co.edu.uniquindio.proyectobasesdedatos.config.ConexionOracle;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.CiudadDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) de la tabla TCiudad: CRUD con JDBC puro.
 *
 * <p>Mismas reglas que {@link ProveedorDAO}: {@link PreparedStatement} con "?",
 * try-with-resources y sin mensajes al usuario (se lanza {@link SQLException}).
 * Las consultas hacen JOIN con TDepartamento para traer el nombre del departamento.</p>
 */
public class CiudadDAO {

    // ---------------------------------------------------------------- INSERT

    /**
     * Inserta una ciudad. El código (CCodigo) lo genera Oracle (IDENTITY) y se
     * devuelve ya asignado en el mismo DTO.
     *
     * @return true si se insertó la fila
     * @throws SQLException p. ej. ORA-02291 (el departamento no existe)
     */
    public boolean insertar(CiudadDTO c) throws SQLException {
        String sql = "INSERT INTO TCiudad (CNombre, DCodigo) VALUES (?, ?)";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"CCODIGO"})) {

            ps.setString(1, c.getNombre());
            ps.setInt(2, c.getCodigoDepartamento());

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
     * Busca una ciudad por su código.
     *
     * @return la ciudad, o null si no existe
     */
    public CiudadDTO buscarPorCodigo(int codigo) throws SQLException {
        String sql = SELECT_BASE + " WHERE c.CCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Lista todas las ciudades ordenadas por departamento y nombre.
     *
     * @return lista (vacía si no hay registros, nunca null)
     */
    public List<CiudadDTO> listar() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY d.DNombre, c.CNombre";
        List<CiudadDTO> lista = new ArrayList<>();

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
     * Lista las ciudades de un departamento (para el combo dependiente de
     * Proveedores y los filtros de Reportes).
     *
     * @param codigoDepartamento DCodigo del departamento
     */
    public List<CiudadDTO> listarPorDepartamento(int codigoDepartamento) throws SQLException {
        String sql = SELECT_BASE + " WHERE c.DCodigo = ? ORDER BY c.CNombre";
        List<CiudadDTO> lista = new ArrayList<>();

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigoDepartamento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Filtra ciudades por texto en el nombre de la ciudad o del departamento
     * (búsqueda parcial, sin distinguir mayúsculas).
     */
    public List<CiudadDTO> buscarPorTexto(String texto) throws SQLException {
        String sql = SELECT_BASE
                + " WHERE UPPER(c.CNombre) LIKE ? OR UPPER(d.DNombre) LIKE ?"
                + " ORDER BY d.DNombre, c.CNombre";
        List<CiudadDTO> lista = new ArrayList<>();
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
     * Actualiza nombre y departamento de la ciudad identificada por su código.
     *
     * @return true si se modificó alguna fila (false si el código no existe)
     */
    public boolean actualizar(CiudadDTO c) throws SQLException {
        String sql = "UPDATE TCiudad SET CNombre = ?, DCodigo = ? WHERE CCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setInt(2, c.getCodigoDepartamento());
            ps.setInt(3, c.getCodigo());

            return ps.executeUpdate() > 0;
        }
    }

    // ---------------------------------------------------------------- DELETE

    /**
     * Elimina una ciudad.
     *
     * <p>Si la ciudad tiene proveedores, Oracle lanza ORA-02292 (llave foránea).</p>
     *
     * @return true si se eliminó la fila
     */
    public boolean eliminar(int codigo) throws SQLException {
        String sql = "DELETE FROM TCiudad WHERE CCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);
            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------- Apoyo interno

    /** SELECT común: trae la ciudad y el nombre de su departamento (JOIN con TDepartamento). */
    private static final String SELECT_BASE =
            "SELECT c.CCodigo, c.CNombre, c.DCodigo, d.DNombre "
            + "FROM TCiudad c "
            + "JOIN TDepartamento d ON d.DCodigo = c.DCodigo";

    /** Convierte la fila actual del ResultSet en un CiudadDTO. */
    private CiudadDTO mapear(ResultSet rs) throws SQLException {
        CiudadDTO c = new CiudadDTO();
        c.setCodigo(rs.getInt("CCodigo"));
        c.setNombre(rs.getString("CNombre"));
        c.setCodigoDepartamento(rs.getInt("DCodigo"));
        c.setNombreDepartamento(rs.getString("DNombre"));
        return c;
    }
}
