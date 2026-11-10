package co.edu.uniquindio.proyectobasesdedatos.modelo.dao;

import co.edu.uniquindio.proyectobasesdedatos.config.ConexionOracle;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.ResultadoConsulta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO de las consultas del módulo "Reportes/Consultas".
 *
 * <p>Cada consulta tiene su propio método y su SQL está en una constante con
 * Javadoc que explica qué tablas usa y cómo se relacionan. Todas devuelven un
 * {@link ResultadoConsulta} (columnas + filas) para que la vista las muestre
 * en una tabla sin conocer el SQL.</p>
 *
 * <p>Igual que en los demás DAO: {@link PreparedStatement}, try-with-resources
 * y sin mensajes al usuario (los errores se lanzan como {@link SQLException}).</p>
 */
public class ConsultaDAO {

    // ============================================================ JOIN (3+ tablas)

    /**
     * (a) JOIN de 3 tablas: proveedores con su ciudad y departamento.
     *
     * <p>TProveedor se une con TCiudad por CCodigo (cada proveedor está en una
     * ciudad) y TCiudad con TDepartamento por DCodigo (cada ciudad pertenece a un
     * departamento). Así se ve la ubicación completa del proveedor.</p>
     */
    private static final String SQL_PROVEEDORES_UBICACION =
            "SELECT p.PNit, p.PNombre, p.PTelefono, p.PCorreo, "
            + "c.CNombre, d.DNombre, "
            + "CASE p.PActivo WHEN 1 THEN 'Sí' ELSE 'No' END AS Activo "
            + "FROM TProveedor p "
            + "JOIN TCiudad c ON c.CCodigo = p.CCodigo "
            + "JOIN TDepartamento d ON d.DCodigo = c.DCodigo "
            + "ORDER BY d.DNombre, c.CNombre, p.PNombre";

    /**
     * (b) JOIN de 3 tablas: productos con los proveedores que los suministran.
     *
     * <p>TSuministro es la entidad asociativa entre TProducto y TProveedor: se
     * une con TProducto por PrCodigo y con TProveedor por PCodigo. Se compara el
     * precio base del producto con el precio que cobra cada proveedor
     * (diferencia = precio suministro − precio base).</p>
     */
    private static final String SQL_PRODUCTOS_PROVEEDORES =
            "SELECT pr.PrNombre, pr.PrPrecioBase, p.PNombre, s.SPrecio, "
            + "s.SDiasEntrega, (s.SPrecio - pr.PrPrecioBase) AS Diferencia "
            + "FROM TProducto pr "
            + "JOIN TSuministro s ON s.PrCodigo = pr.PrCodigo "
            + "JOIN TProveedor p ON p.PCodigo = s.PCodigo "
            + "ORDER BY pr.PrNombre, s.SPrecio";

    /**
     * (c) JOIN de 4 tablas: productos con su categoría y sus proveedores.
     *
     * <p>TProducto se une con TCategoria por CaCodigo (cada producto tiene una
     * categoría), con TSuministro por PrCodigo y TSuministro con TProveedor por
     * PCodigo. Muestra, por categoría, el stock de cada producto y quién lo
     * suministra con cuántos días de entrega.</p>
     */
    private static final String SQL_PRODUCTOS_CATEGORIA_PROVEEDOR =
            "SELECT ca.CaNombre, pr.PrNombre, pr.PrStock, p.PNombre, s.SDiasEntrega "
            + "FROM TProducto pr "
            + "JOIN TCategoria ca ON ca.CaCodigo = pr.CaCodigo "
            + "JOIN TSuministro s ON s.PrCodigo = pr.PrCodigo "
            + "JOIN TProveedor p ON p.PCodigo = s.PCodigo "
            + "ORDER BY ca.CaNombre, pr.PrNombre, s.SDiasEntrega";

    /** (a) Proveedores con ciudad y departamento. */
    public ResultadoConsulta proveedoresConUbicacion() throws SQLException {
        return ejecutar(SQL_PROVEEDORES_UBICACION,
                "NIT", "Proveedor", "Teléfono", "Correo", "Ciudad", "Departamento", "Activo");
    }

    /** (b) Productos con los proveedores que los suministran y su precio. */
    public ResultadoConsulta productosConProveedores() throws SQLException {
        return ejecutar(SQL_PRODUCTOS_PROVEEDORES,
                "Producto", "Precio base", "Proveedor", "Precio suministro",
                "Días de entrega", "Diferencia");
    }

    /** (c) Productos con su categoría y los proveedores que los suministran. */
    public ResultadoConsulta productosPorCategoriaYProveedor() throws SQLException {
        return ejecutar(SQL_PRODUCTOS_CATEGORIA_PROVEEDOR,
                "Categoría", "Producto", "Stock", "Proveedor", "Días de entrega");
    }

    // ============================================================ Subconsultas

    /**
     * (d) Subconsulta correlacionada: productos cuyo precio base supera el
     * promedio de su propia categoría.
     *
     * <p>Para cada producto {@code pr}, la subconsulta del WHERE calcula el
     * promedio de TProducto filtrando por la misma categoría
     * ({@code p2.CaCodigo = pr.CaCodigo}). En el SELECT se repite como
     * subconsulta escalar para mostrar ese promedio redondeado a 2 decimales.
     * El JOIN con TCategoria trae el nombre de la categoría.</p>
     */
    private static final String SQL_PRODUCTOS_SOBRE_PROMEDIO =
            "SELECT ca.CaNombre, pr.PrNombre, pr.PrPrecioBase, "
            + "(SELECT ROUND(AVG(p3.PrPrecioBase), 2) FROM TProducto p3 "
            + " WHERE p3.CaCodigo = pr.CaCodigo) AS PromedioCategoria "
            + "FROM TProducto pr "
            + "JOIN TCategoria ca ON ca.CaCodigo = pr.CaCodigo "
            + "WHERE pr.PrPrecioBase > (SELECT AVG(p2.PrPrecioBase) FROM TProducto p2 "
            + " WHERE p2.CaCodigo = pr.CaCodigo) "
            + "ORDER BY ca.CaNombre, pr.PrPrecioBase DESC";

    /**
     * (e) Subconsulta con NOT EXISTS: proveedores que todavía no suministran
     * ningún producto.
     *
     * <p>Para cada proveedor se busca en TSuministro alguna fila con su PCodigo;
     * si no existe ninguna, el proveedor aparece en el resultado. Sirve para
     * detectar proveedores registrados a los que falta asignarles productos.</p>
     */
    private static final String SQL_PROVEEDORES_SIN_SUMINISTROS =
            "SELECT p.PNit, p.PNombre, p.PTelefono, p.PCorreo, "
            + "CASE p.PActivo WHEN 1 THEN 'Sí' ELSE 'No' END AS Activo "
            + "FROM TProveedor p "
            + "WHERE NOT EXISTS (SELECT 1 FROM TSuministro s WHERE s.PCodigo = p.PCodigo) "
            + "ORDER BY p.PNombre";

    /** (d) Productos con precio base mayor al promedio de su categoría. */
    public ResultadoConsulta productosSobrePromedioCategoria() throws SQLException {
        return ejecutar(SQL_PRODUCTOS_SOBRE_PROMEDIO,
                "Categoría", "Producto", "Precio base", "Promedio de la categoría");
    }

    /** (e) Proveedores que no tienen ningún suministro registrado. */
    public ResultadoConsulta proveedoresSinSuministros() throws SQLException {
        return ejecutar(SQL_PROVEEDORES_SIN_SUMINISTROS,
                "NIT", "Proveedor", "Teléfono", "Correo", "Activo");
    }

    // ------------------------------------------------------- Apoyo interno

    /**
     * Ejecuta un SELECT sin parámetros y copia cada fila en un
     * {@link ResultadoConsulta}. Los NUMBER de Oracle llegan como
     * {@link java.math.BigDecimal} (la vista les da formato).
     *
     * @param sql      consulta a ejecutar
     * @param columnas títulos de las columnas, en el mismo orden del SELECT
     */
    private ResultadoConsulta ejecutar(String sql, String... columnas) throws SQLException {
        ResultadoConsulta resultado = new ResultadoConsulta(columnas);

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Object[] fila = new Object[columnas.length];
                for (int i = 0; i < columnas.length; i++) {
                    fila[i] = rs.getObject(i + 1);
                }
                resultado.agregarFila(fila);
            }
        }
        return resultado;
    }
}
