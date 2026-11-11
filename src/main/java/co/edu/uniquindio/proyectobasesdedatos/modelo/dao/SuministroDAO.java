package co.edu.uniquindio.proyectobasesdedatos.modelo.dao;

import co.edu.uniquindio.proyectobasesdedatos.config.ConexionOracle;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.SuministroDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) de la tabla TSuministro: CRUD con JDBC puro.
 *
 * <p>Mismas reglas que {@link ProveedorDAO}: {@link PreparedStatement} con "?",
 * try-with-resources y sin mensajes al usuario (se lanza {@link SQLException}).
 * Las consultas hacen JOIN con TProveedor y TProducto para traer los nombres y
 * el precio base del producto.</p>
 */
public class SuministroDAO {

    // ---------------------------------------------------------------- INSERT

    /**
     * Inserta un suministro (asigna un producto a un proveedor). El código
     * (SCodigo) lo genera Oracle (IDENTITY) y se devuelve en el mismo DTO.
     *
     * @return true si se insertó la fila
     * @throws SQLException p. ej. ORA-00001 (el proveedor ya suministra ese producto)
     *                      u ORA-02291 (proveedor o producto inexistente)
     */
    public boolean insertar(SuministroDTO s) throws SQLException {
        String sql = "INSERT INTO TSuministro "
                + "(SDiasEntrega, SPrecio, PCodigo, PrCodigo) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"SCODIGO"})) {

            ps.setInt(1, s.getDiasEntrega());
            ps.setBigDecimal(2, s.getPrecio());
            ps.setInt(3, s.getCodigoProveedor());
            ps.setInt(4, s.getCodigoProducto());

            boolean insertado = ps.executeUpdate() > 0;

            if (insertado) {
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (claves.next()) {
                        s.setCodigo(claves.getInt(1));
                    }
                }
            }
            return insertado;
        }
    }

    // ---------------------------------------------------------------- SELECT

    /**
     * Busca un suministro por su código.
     *
     * @return el suministro, o null si no existe
     */
    public SuministroDTO buscarPorCodigo(int codigo) throws SQLException {
        String sql = SELECT_BASE + " WHERE s.SCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Lista todos los suministros ordenados por proveedor y producto.
     *
     * @return lista (vacía si no hay registros, nunca null)
     */
    public List<SuministroDTO> listar() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY p.PNombre, pr.PrNombre";
        List<SuministroDTO> lista = new ArrayList<>();

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
     * Productos que suministra un proveedor (ordenados por nombre de producto).
     *
     * @param codigoProveedor PCodigo del proveedor
     */
    public List<SuministroDTO> listarPorProveedor(int codigoProveedor) throws SQLException {
        String sql = SELECT_BASE + " WHERE s.PCodigo = ? ORDER BY pr.PrNombre";
        return listarConParametro(sql, codigoProveedor);
    }

    /**
     * Proveedores que suministran un producto (ordenados por precio, del más
     * barato al más caro).
     *
     * @param codigoProducto PrCodigo del producto
     */
    public List<SuministroDTO> listarPorProducto(int codigoProducto) throws SQLException {
        String sql = SELECT_BASE + " WHERE s.PrCodigo = ? ORDER BY s.SPrecio, p.PNombre";
        return listarConParametro(sql, codigoProducto);
    }

    // ---------------------------------------------------------------- UPDATE

    /**
     * Actualiza el precio, los días de entrega, el proveedor y el producto del
     * suministro identificado por su código.
     *
     * @return true si se modificó alguna fila (false si el código no existe)
     * @throws SQLException p. ej. ORA-00001 si la nueva pareja proveedor-producto ya existe
     */
    public boolean actualizar(SuministroDTO s) throws SQLException {
        String sql = "UPDATE TSuministro SET "
                + "SDiasEntrega = ?, SPrecio = ?, PCodigo = ?, PrCodigo = ? "
                + "WHERE SCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, s.getDiasEntrega());
            ps.setBigDecimal(2, s.getPrecio());
            ps.setInt(3, s.getCodigoProveedor());
            ps.setInt(4, s.getCodigoProducto());
            ps.setInt(5, s.getCodigo());

            return ps.executeUpdate() > 0;
        }
    }

    // ---------------------------------------------------------------- DELETE

    /**
     * Elimina un suministro (el proveedor deja de ofrecer ese producto). Ninguna
     * tabla depende de TSuministro, así que no hay restricción de hijos.
     *
     * @return true si se eliminó la fila
     */
    public boolean eliminar(int codigo) throws SQLException {
        String sql = "DELETE FROM TSuministro WHERE SCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);
            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------- Apoyo interno

    /**
     * SELECT común: trae el suministro con el nombre del proveedor y el nombre y
     * precio base del producto (JOIN de TSuministro con TProveedor y TProducto).
     */
    private static final String SELECT_BASE =
            "SELECT s.SCodigo, s.SDiasEntrega, s.SPrecio, s.PCodigo, s.PrCodigo, "
            + "p.PNombre, pr.PrNombre, pr.PrPrecioBase "
            + "FROM TSuministro s "
            + "JOIN TProveedor p ON p.PCodigo = s.PCodigo "
            + "JOIN TProducto pr ON pr.PrCodigo = s.PrCodigo";

    /** Ejecuta una consulta que recibe un único parámetro entero y mapea las filas. */
    private List<SuministroDTO> listarConParametro(String sql, int valor) throws SQLException {
        List<SuministroDTO> lista = new ArrayList<>();

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Convierte la fila actual del ResultSet en un SuministroDTO. */
    private SuministroDTO mapear(ResultSet rs) throws SQLException {
        SuministroDTO s = new SuministroDTO();
        s.setCodigo(rs.getInt("SCodigo"));
        s.setDiasEntrega(rs.getInt("SDiasEntrega"));
        s.setPrecio(rs.getBigDecimal("SPrecio"));
        s.setCodigoProveedor(rs.getInt("PCodigo"));
        s.setCodigoProducto(rs.getInt("PrCodigo"));
        s.setNombreProveedor(rs.getString("PNombre"));
        s.setNombreProducto(rs.getString("PrNombre"));
        s.setPrecioBaseProducto(rs.getBigDecimal("PrPrecioBase"));
        return s;
    }
}
