package co.edu.uniquindio.proyectobasesdedatos.modelo.dao;

import co.edu.uniquindio.proyectobasesdedatos.config.ConexionOracle;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.DatosReporte;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * DAO de los reportes PDF: ejecuta las consultas y devuelve un
 * {@link DatosReporte} con las filas ya formateadas como texto.
 *
 * <p>Los filtros son opcionales (un parámetro null = sin filtro). El SQL se
 * arma dinámicamente agregando condiciones, pero los valores SIEMPRE van como
 * parámetros "?" de un {@link PreparedStatement} (nunca concatenados).</p>
 *
 * <p>El título lo pone este DAO; el subtítulo con los filtros lo arma el
 * controlador, que conoce los nombres seleccionados.</p>
 */
public class ReporteDAO {

    /** Formato colombiano: punto para miles y coma para decimales. */
    private static final DecimalFormatSymbols SIMBOLOS_CO = DecimalFormatSymbols.getInstance(Locale.of("es", "CO"));

    static {
        SIMBOLOS_CO.setGroupingSeparator('.');
        SIMBOLOS_CO.setDecimalSeparator(',');
    }

    // ============================================ 1. Proveedores por ubicación

    /**
     * Proveedores filtrados por departamento y/o ciudad.
     *
     * @param codigoDepartamento DCodigo, o null para todos
     * @param codigoCiudad       CCodigo, o null para todas
     */
    public DatosReporte proveedoresPorUbicacion(Integer codigoDepartamento, Integer codigoCiudad)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT p.PNit, p.PNombre, c.CNombre, d.DNombre, p.PTelefono, p.PCorreo, p.PActivo "
                + "FROM TProveedor p "
                + "JOIN TCiudad c ON c.CCodigo = p.CCodigo "
                + "JOIN TDepartamento d ON d.DCodigo = c.DCodigo "
                + "WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (codigoDepartamento != null) {
            sql.append(" AND d.DCodigo = ?");
            parametros.add(codigoDepartamento);
        }
        if (codigoCiudad != null) {
            sql.append(" AND c.CCodigo = ?");
            parametros.add(codigoCiudad);
        }
        sql.append(" ORDER BY d.DNombre, c.CNombre, p.PNombre");

        DatosReporte datos = new DatosReporte("Proveedores por departamento y ciudad",
                new String[]{"NIT", "Proveedor", "Ciudad", "Departamento", "Teléfono", "Correo", "Activo"},
                new float[]{2.2f, 4, 2, 2, 2, 4, 1.2f});

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            asignar(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.agregarFila(
                            rs.getString("PNit"),
                            rs.getString("PNombre"),
                            rs.getString("CNombre"),
                            rs.getString("DNombre"),
                            rs.getString("PTelefono"),
                            rs.getString("PCorreo"),
                            rs.getInt("PActivo") == 1 ? "Sí" : "No");
                }
            }
        }
        return datos;
    }

    // ===================================== 2. Productos por precio y/o stock

    /**
     * Productos cuyo precio base y stock están en los rangos dados (límites
     * incluidos). Cualquier límite en null no se aplica.
     */
    public DatosReporte productosPorPrecioYStock(BigDecimal precioMin, BigDecimal precioMax,
                                                 Integer stockMin, Integer stockMax) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT pr.PrNombre, ca.CaNombre, pr.PrPrecioBase, pr.PrStock "
                + "FROM TProducto pr "
                + "JOIN TCategoria ca ON ca.CaCodigo = pr.CaCodigo "
                + "WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (precioMin != null) {
            sql.append(" AND pr.PrPrecioBase >= ?");
            parametros.add(precioMin);
        }
        if (precioMax != null) {
            sql.append(" AND pr.PrPrecioBase <= ?");
            parametros.add(precioMax);
        }
        if (stockMin != null) {
            sql.append(" AND pr.PrStock >= ?");
            parametros.add(stockMin);
        }
        if (stockMax != null) {
            sql.append(" AND pr.PrStock <= ?");
            parametros.add(stockMax);
        }
        sql.append(" ORDER BY ca.CaNombre, pr.PrNombre");

        DatosReporte datos = new DatosReporte("Productos por rango de precio y stock",
                new String[]{"Producto", "Categoría", "Precio base", "Stock"},
                new float[]{5, 3, 2, 1.5f});
        datos.alinearDerecha(2, 3);

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            asignar(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.agregarFila(
                            rs.getString("PrNombre"),
                            rs.getString("CaNombre"),
                            precio(rs.getBigDecimal("PrPrecioBase")),
                            entero(rs.getLong("PrStock")));
                }
            }
        }
        return datos;
    }

    // ======================================== 3. Productos de un proveedor

    /**
     * Productos que suministra un proveedor, con su precio de suministro y
     * días de entrega. Incluye fila de totales.
     *
     * @param codigoProveedor PCodigo del proveedor
     */
    public DatosReporte productosDeProveedor(int codigoProveedor) throws SQLException {
        String sql = "SELECT pr.PrNombre, ca.CaNombre, pr.PrPrecioBase, s.SPrecio, s.SDiasEntrega "
                + "FROM TSuministro s "
                + "JOIN TProducto pr ON pr.PrCodigo = s.PrCodigo "
                + "JOIN TCategoria ca ON ca.CaCodigo = pr.CaCodigo "
                + "WHERE s.PCodigo = ? "
                + "ORDER BY ca.CaNombre, pr.PrNombre";

        DatosReporte datos = new DatosReporte("Productos suministrados por un proveedor",
                new String[]{"Producto", "Categoría", "Precio base", "Precio suministro", "Días de entrega"},
                new float[]{5, 3, 2.2f, 2.2f, 1.8f});
        datos.alinearDerecha(2, 3, 4);

        int cantidad = 0;
        long sumaDias = 0;
        BigDecimal totalBase = BigDecimal.ZERO;
        BigDecimal totalSuministro = BigDecimal.ZERO;

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, codigoProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal base = rs.getBigDecimal("PrPrecioBase");
                    BigDecimal suministro = rs.getBigDecimal("SPrecio");
                    int dias = rs.getInt("SDiasEntrega");
                    datos.agregarFila(
                            rs.getString("PrNombre"),
                            rs.getString("CaNombre"),
                            precio(base),
                            precio(suministro),
                            entero(dias));
                    cantidad++;
                    sumaDias += dias;
                    totalBase = totalBase.add(base);
                    totalSuministro = totalSuministro.add(suministro);
                }
            }
        }

        if (cantidad > 0) {
            BigDecimal promedioDias = BigDecimal.valueOf(sumaDias)
                    .divide(BigDecimal.valueOf(cantidad), 1, RoundingMode.HALF_UP);
            datos.setTotales("TOTAL (" + cantidad + (cantidad == 1 ? " producto)" : " productos)"),
                    "",
                    precio(totalBase),
                    precio(totalSuministro),
                    "Prom. " + decimal(promedioDias));
        }
        return datos;
    }

    // ======================================== 4. Productos de una categoría

    /**
     * Productos de una categoría con la cantidad de proveedores que los
     * suministran (0 si ninguno: por eso se usa LEFT JOIN).
     *
     * @param codigoCategoria CaCodigo de la categoría
     */
    public DatosReporte productosPorCategoria(int codigoCategoria) throws SQLException {
        String sql = "SELECT pr.PrNombre, pr.PrDescripcion, pr.PrPrecioBase, pr.PrStock, "
                + "COUNT(s.PCodigo) AS Proveedores "
                + "FROM TProducto pr "
                + "LEFT JOIN TSuministro s ON s.PrCodigo = pr.PrCodigo "
                + "WHERE pr.CaCodigo = ? "
                + "GROUP BY pr.PrCodigo, pr.PrNombre, pr.PrDescripcion, pr.PrPrecioBase, pr.PrStock "
                + "ORDER BY pr.PrNombre";

        DatosReporte datos = new DatosReporte("Productos por categoría",
                new String[]{"Producto", "Descripción", "Precio base", "Stock", "Proveedores"},
                new float[]{4, 5, 2.2f, 1.4f, 1.6f});
        datos.alinearDerecha(2, 3, 4);

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, codigoCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.agregarFila(
                            rs.getString("PrNombre"),
                            rs.getString("PrDescripcion"),
                            precio(rs.getBigDecimal("PrPrecioBase")),
                            entero(rs.getLong("PrStock")),
                            entero(rs.getLong("Proveedores")));
                }
            }
        }
        return datos;
    }

    // =========================================== 5. Resumen por categoría

    /**
     * Resumen estadístico por categoría: cantidad de productos, precio
     * promedio, mínimo y máximo, y stock total. Las categorías sin productos
     * también aparecen (LEFT JOIN) con cantidad 0. Incluye fila de totales.
     *
     * @param codigoCategoria CaCodigo, o null para todas las categorías
     */
    public DatosReporte resumenPorCategoria(Integer codigoCategoria) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT ca.CaNombre, COUNT(pr.PrCodigo) AS Cantidad, "
                + "ROUND(AVG(pr.PrPrecioBase), 2) AS Promedio, "
                + "MIN(pr.PrPrecioBase) AS Minimo, MAX(pr.PrPrecioBase) AS Maximo, "
                + "NVL(SUM(pr.PrPrecioBase), 0) AS SumaPrecios, "
                + "NVL(SUM(pr.PrStock), 0) AS StockTotal "
                + "FROM TCategoria ca "
                + "LEFT JOIN TProducto pr ON pr.CaCodigo = ca.CaCodigo");
        List<Object> parametros = new ArrayList<>();
        if (codigoCategoria != null) {
            sql.append(" WHERE ca.CaCodigo = ?");
            parametros.add(codigoCategoria);
        }
        sql.append(" GROUP BY ca.CaCodigo, ca.CaNombre ORDER BY ca.CaNombre");

        DatosReporte datos = new DatosReporte("Resumen de productos por categoría",
                new String[]{"Categoría", "Productos", "Precio promedio", "Precio mínimo",
                        "Precio máximo", "Stock total"},
                new float[]{3, 1.5f, 2.3f, 2.3f, 2.3f, 1.7f});
        datos.alinearDerecha(1, 2, 3, 4, 5);

        long totalProductos = 0;
        long totalStock = 0;
        BigDecimal sumaPrecios = BigDecimal.ZERO;
        BigDecimal minimo = null;
        BigDecimal maximo = null;

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            asignar(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long cantidad = rs.getLong("Cantidad");
                    BigDecimal min = rs.getBigDecimal("Minimo");
                    BigDecimal max = rs.getBigDecimal("Maximo");
                    long stock = rs.getLong("StockTotal");
                    datos.agregarFila(
                            rs.getString("CaNombre"),
                            entero(cantidad),
                            precio(rs.getBigDecimal("Promedio")),
                            precio(min),
                            precio(max),
                            entero(stock));

                    totalProductos += cantidad;
                    totalStock += stock;
                    sumaPrecios = sumaPrecios.add(rs.getBigDecimal("SumaPrecios"));
                    if (min != null && (minimo == null || min.compareTo(minimo) < 0)) {
                        minimo = min;
                    }
                    if (max != null && (maximo == null || max.compareTo(maximo) > 0)) {
                        maximo = max;
                    }
                }
            }
        }

        if (!datos.estaVacio()) {
            // El promedio general se calcula sobre todos los productos, no
            // promediando los promedios de cada categoría.
            BigDecimal promedio = totalProductos == 0 ? null
                    : sumaPrecios.divide(BigDecimal.valueOf(totalProductos), 2, RoundingMode.HALF_UP);
            datos.setTotales("TOTAL", entero(totalProductos), precio(promedio),
                    precio(minimo), precio(maximo), entero(totalStock));
        }
        return datos;
    }

    // ------------------------------------------------------- Apoyo interno

    /** Asigna los parámetros en orden según su tipo. */
    private static void asignar(PreparedStatement ps, List<Object> parametros) throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            Object valor = parametros.get(i);
            if (valor instanceof Integer n) {
                ps.setInt(i + 1, n);
            } else if (valor instanceof BigDecimal n) {
                ps.setBigDecimal(i + 1, n);
            } else {
                ps.setString(i + 1, String.valueOf(valor));
            }
        }
    }

    /** 28900.5 -> "$ 28.900,50"; null -> "-" */
    private static String precio(BigDecimal valor) {
        return valor == null ? "-" : "$ " + new DecimalFormat("#,##0.00", SIMBOLOS_CO).format(valor);
    }

    /** 1250 -> "1.250" */
    private static String entero(long valor) {
        return new DecimalFormat("#,##0", SIMBOLOS_CO).format(valor);
    }

    /** 3.5 -> "3,5" */
    private static String decimal(BigDecimal valor) {
        return new DecimalFormat("#,##0.#", SIMBOLOS_CO).format(valor);
    }
}
