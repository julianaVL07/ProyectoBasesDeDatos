package co.edu.uniquindio.proyectobasesdedatos.modelo.dao;

import co.edu.uniquindio.proyectobasesdedatos.config.ConexionOracle;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.ProductoDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) de la tabla TProducto: CRUD con JDBC puro.
 *
 * <p>Mismas reglas que {@link ProveedorDAO}: {@link PreparedStatement} con "?",
 * try-with-resources y sin mensajes al usuario (se lanza {@link SQLException}).
 * Las consultas hacen JOIN con TCategoria para traer el nombre de la categoría.</p>
 */
public class ProductoDAO {

    // ---------------------------------------------------------------- INSERT

    /**
     * Inserta un producto. El código (PrCodigo) lo genera Oracle (IDENTITY) y
     * se devuelve ya asignado en el mismo DTO.
     *
     * @return true si se insertó la fila
     * @throws SQLException p. ej. ORA-02291 (categoría inexistente) u ORA-02290 (precio o stock negativo)
     */
    public boolean insertar(ProductoDTO p) throws SQLException {
        String sql = "INSERT INTO TProducto "
                + "(PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"PRCODIGO"})) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setBigDecimal(3, p.getPrecioBase());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getCodigoCategoria());

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
     * Busca un producto por su código.
     *
     * @return el producto, o null si no existe
     */
    public ProductoDTO buscarPorCodigo(int codigo) throws SQLException {
        String sql = SELECT_BASE + " WHERE pr.PrCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Lista todos los productos ordenados por nombre.
     *
     * @return lista (vacía si no hay registros, nunca null)
     */
    public List<ProductoDTO> listar() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY pr.PrNombre";
        List<ProductoDTO> lista = new ArrayList<>();

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
     * Lista los productos de una categoría ordenados por nombre (útil para
     * combos filtrados, p. ej. en Suministros o Reportes).
     */
    public List<ProductoDTO> listarPorCategoria(int codigoCategoria) throws SQLException {
        String sql = SELECT_BASE + " WHERE pr.CaCodigo = ? ORDER BY pr.PrNombre";
        List<ProductoDTO> lista = new ArrayList<>();

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigoCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Filtra productos por texto en el nombre del producto o de su categoría
     * (búsqueda parcial, sin distinguir mayúsculas).
     */
    public List<ProductoDTO> buscarPorTexto(String texto) throws SQLException {
        String sql = SELECT_BASE
                + " WHERE UPPER(pr.PrNombre) LIKE ? OR UPPER(ca.CaNombre) LIKE ?"
                + " ORDER BY pr.PrNombre";
        List<ProductoDTO> lista = new ArrayList<>();
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
     * Actualiza todos los datos del producto identificado por su código.
     *
     * @return true si se modificó alguna fila (false si el código no existe)
     */
    public boolean actualizar(ProductoDTO p) throws SQLException {
        String sql = "UPDATE TProducto SET "
                + "PrNombre = ?, PrDescripcion = ?, PrPrecioBase = ?, PrStock = ?, CaCodigo = ? "
                + "WHERE PrCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setBigDecimal(3, p.getPrecioBase());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getCodigoCategoria());
            ps.setInt(6, p.getCodigo());

            return ps.executeUpdate() > 0;
        }
    }

    // ---------------------------------------------------------------- DELETE

    /**
     * Elimina un producto.
     *
     * <p>Si el producto tiene suministros, Oracle lanza ORA-02292 (llave foránea).</p>
     *
     * @return true si se eliminó la fila
     */
    public boolean eliminar(int codigo) throws SQLException {
        String sql = "DELETE FROM TProducto WHERE PrCodigo = ?";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codigo);
            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------- Apoyo interno

    /** SELECT común: trae el producto y el nombre de su categoría (JOIN con TCategoria). */
    private static final String SELECT_BASE =
            "SELECT pr.PrCodigo, pr.PrNombre, pr.PrDescripcion, pr.PrPrecioBase, "
            + "pr.PrStock, pr.CaCodigo, ca.CaNombre "
            + "FROM TProducto pr "
            + "JOIN TCategoria ca ON ca.CaCodigo = pr.CaCodigo";

    /** Convierte la fila actual del ResultSet en un ProductoDTO. */
    private ProductoDTO mapear(ResultSet rs) throws SQLException {
        ProductoDTO p = new ProductoDTO();
        p.setCodigo(rs.getInt("PrCodigo"));
        p.setNombre(rs.getString("PrNombre"));
        p.setDescripcion(rs.getString("PrDescripcion"));
        p.setPrecioBase(rs.getBigDecimal("PrPrecioBase"));
        p.setStock(rs.getInt("PrStock"));
        p.setCodigoCategoria(rs.getInt("CaCodigo"));
        p.setNombreCategoria(rs.getString("CaNombre"));
        return p;
    }
}
