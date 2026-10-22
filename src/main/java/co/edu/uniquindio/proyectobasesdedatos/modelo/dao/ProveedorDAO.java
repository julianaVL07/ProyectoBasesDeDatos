package co.edu.uniquindio.proyectobasesdedatos.modelo.dao;

import co.edu.uniquindio.proyectobasesdedatos.config.ConexionOracle;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.ProveedorDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) de la tabla TProveedor: CRUD con JDBC puro.
 *
 * <p>Reglas que se siguen en todos los métodos:</p>
 * <ul>
 *   <li>SIEMPRE {@link PreparedStatement} con parámetros "?" (evita SQL Injection).</li>
 *   <li>try-with-resources: conexión, statement y resultset se cierran solos.</li>
 *   <li>El DAO no muestra mensajes al usuario: lanza {@link SQLException} y el
 *       controlador decide qué mostrar en la vista.</li>
 * </ul>
 */
public class ProveedorDAO {

    // ---------------------------------------------------------------- INSERT

    /**
     * Inserta un proveedor. El código (PCodigo) lo genera Oracle (IDENTITY) y
     * se devuelve ya asignado en el mismo DTO.
     *
     * @return true si se insertó la fila
     * @throws SQLException p. ej. ORA-00001 (NIT repetido) u ORA-02291 (ciudad inexistente)
     */
    public boolean insertar(ProveedorDTO p) throws SQLException {
        String sql = "INSERT INTO TProveedor "
                + "(PNit, PNombre, PDireccion, PCorreo, PTelefono, PActivo, CCodigo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionOracle.getConexion();
             // El 2.º argumento pide a Oracle que devuelva la columna PCODIGO generada.
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"PCODIGO"})) {

            ps.setString(1, p.getNit());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getDireccion());
            ps.setString(4, p.getCorreo());
            ps.setString(5, p.getTelefono());
            ps.setInt(6, p.isActivo() ? 1 : 0);
            ps.setInt(7, p.getCodigoCiudad());

            boolean insertado = ps.executeUpdate() > 0;

            if (insertado) {
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (claves.next()) {
                        p.setCodigo(claves.getInt(1));
                    }
                }
            }
            return insertado;
        }
    }

    // ---------------------------------------------------------------- SELECT

    /**
     * Busca un proveedor por su código.
     *
     * @return el proveedor, o null si no existe
     */
    public ProveedorDTO buscarPorCodigo(int codigo) throws SQLException {
        String sql = SELECT_BASE + " WHERE p.PCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Lista todos los proveedores ordenados por nombre.
     *
     * @return lista (vacía si no hay registros, nunca null)
     */
    public List<ProveedorDTO> listar() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY p.PNombre";
        List<ProveedorDTO> lista = new ArrayList<>();

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
     * Filtra proveedores por texto en el nombre o el NIT (búsqueda parcial, sin
     * distinguir mayúsculas). Sirve para el cuadro de búsqueda de la ventana.
     */
    public List<ProveedorDTO> buscarPorTexto(String texto) throws SQLException {
        String sql = SELECT_BASE
                + " WHERE UPPER(p.PNombre) LIKE ? OR p.PNit LIKE ?"
                + " ORDER BY p.PNombre";
        List<ProveedorDTO> lista = new ArrayList<>();
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
     * Actualiza todos los datos del proveedor identificado por su código.
     *
     * @return true si se modificó alguna fila (false si el código no existe)
     */
    public boolean actualizar(ProveedorDTO p) throws SQLException {
        String sql = "UPDATE TProveedor SET "
                + "PNit = ?, PNombre = ?, PDireccion = ?, PCorreo = ?, "
                + "PTelefono = ?, PActivo = ?, CCodigo = ? "
                + "WHERE PCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNit());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getDireccion());
            ps.setString(4, p.getCorreo());
            ps.setString(5, p.getTelefono());
            ps.setInt(6, p.isActivo() ? 1 : 0);
            ps.setInt(7, p.getCodigoCiudad());
            ps.setInt(8, p.getCodigo());

            return ps.executeUpdate() > 0;
        }
    }

    // ---------------------------------------------------------------- DELETE

    /**
     * Elimina físicamente un proveedor.
     *
     * <p>Si el proveedor ya tiene suministros (TSuministro), Oracle lanza
     * ORA-02292 (restricción de llave foránea). El controlador puede detectarlo con
     * {@code e.getErrorCode() == 2292} y sugerir desactivar el proveedor
     * (PActivo = 0) en lugar de borrarlo.</p>
     *
     * @return true si se eliminó la fila
     */
    public boolean eliminar(int codigo) throws SQLException {
        String sql = "DELETE FROM TProveedor WHERE PCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);
            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------- Apoyo interno

    /** SELECT común: trae el proveedor y el nombre de su ciudad (JOIN con TCiudad). */
    private static final String SELECT_BASE =
            "SELECT p.PCodigo, p.PNit, p.PNombre, p.PDireccion, p.PCorreo, "
            + "p.PTelefono, p.PActivo, p.CCodigo, c.CNombre "
            + "FROM TProveedor p "
            + "JOIN TCiudad c ON c.CCodigo = p.CCodigo";

    /** Convierte la fila actual del ResultSet en un ProveedorDTO. */
    private ProveedorDTO mapear(ResultSet rs) throws SQLException {
        ProveedorDTO p = new ProveedorDTO();
        p.setCodigo(rs.getInt("PCodigo"));
        p.setNit(rs.getString("PNit"));
        p.setNombre(rs.getString("PNombre"));
        p.setDireccion(rs.getString("PDireccion"));
        p.setCorreo(rs.getString("PCorreo"));
        p.setTelefono(rs.getString("PTelefono"));
        p.setActivo(rs.getInt("PActivo") == 1);
        p.setCodigoCiudad(rs.getInt("CCodigo"));
        p.setNombreCiudad(rs.getString("CNombre"));
        return p;
    }
}
