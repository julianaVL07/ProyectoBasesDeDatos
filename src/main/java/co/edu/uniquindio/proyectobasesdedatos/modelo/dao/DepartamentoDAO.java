package co.edu.uniquindio.proyectobasesdedatos.modelo.dao;

import co.edu.uniquindio.proyectobasesdedatos.config.ConexionOracle;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.DepartamentoDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) de la tabla TDepartamento: CRUD con JDBC puro.
 *
 * <p>Mismas reglas que {@link ProveedorDAO}: {@link PreparedStatement} con "?",
 * try-with-resources y sin mensajes al usuario (se lanza {@link SQLException}).</p>
 */
public class DepartamentoDAO {

    // ---------------------------------------------------------------- INSERT

    /**
     * Inserta un departamento. El código (DCodigo) lo genera Oracle (IDENTITY) y
     * se devuelve ya asignado en el mismo DTO.
     *
     * @return true si se insertó la fila
     * @throws SQLException p. ej. ORA-00001 (nombre repetido)
     */
    public boolean insertar(DepartamentoDTO d) throws SQLException {
        String sql = "INSERT INTO TDepartamento (DNombre) VALUES (?)";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"DCODIGO"})) {

            ps.setString(1, d.getNombre());

            boolean insertado = ps.executeUpdate() > 0;

            if (insertado) {
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (claves.next()) {
                        d.setCodigo(claves.getInt(1));
                    }
                }
            }
            return insertado;
        }
    }

    // ---------------------------------------------------------------- SELECT

    /**
     * Busca un departamento por su código.
     *
     * @return el departamento, o null si no existe
     */
    public DepartamentoDTO buscarPorCodigo(int codigo) throws SQLException {
        String sql = SELECT_BASE + " WHERE DCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Lista todos los departamentos ordenados por nombre.
     *
     * @return lista (vacía si no hay registros, nunca null)
     */
    public List<DepartamentoDTO> listar() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY DNombre";
        List<DepartamentoDTO> lista = new ArrayList<>();

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
     * Filtra departamentos por texto en el nombre (búsqueda parcial, sin
     * distinguir mayúsculas).
     */
    public List<DepartamentoDTO> buscarPorTexto(String texto) throws SQLException {
        String sql = SELECT_BASE + " WHERE UPPER(DNombre) LIKE ? ORDER BY DNombre";
        List<DepartamentoDTO> lista = new ArrayList<>();
        String patron = "%" + texto.trim().toUpperCase() + "%";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, patron);
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
     * Actualiza el nombre del departamento identificado por su código.
     *
     * @return true si se modificó alguna fila (false si el código no existe)
     */
    public boolean actualizar(DepartamentoDTO d) throws SQLException {
        String sql = "UPDATE TDepartamento SET DNombre = ? WHERE DCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, d.getNombre());
            ps.setInt(2, d.getCodigo());

            return ps.executeUpdate() > 0;
        }
    }

    // ---------------------------------------------------------------- DELETE

    /**
     * Elimina un departamento.
     *
     * <p>Si el departamento tiene ciudades, Oracle lanza ORA-02292 (llave foránea).</p>
     *
     * @return true si se eliminó la fila
     */
    public boolean eliminar(int codigo) throws SQLException {
        String sql = "DELETE FROM TDepartamento WHERE DCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);
            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------- Apoyo interno

    /** SELECT común de la tabla. */
    private static final String SELECT_BASE = "SELECT DCodigo, DNombre FROM TDepartamento";

    /** Convierte la fila actual del ResultSet en un DepartamentoDTO. */
    private DepartamentoDTO mapear(ResultSet rs) throws SQLException {
        DepartamentoDTO d = new DepartamentoDTO();
        d.setCodigo(rs.getInt("DCodigo"));
        d.setNombre(rs.getString("DNombre"));
        return d;
    }
}
