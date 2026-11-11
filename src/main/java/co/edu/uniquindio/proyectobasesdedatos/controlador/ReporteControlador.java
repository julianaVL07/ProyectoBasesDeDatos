package co.edu.uniquindio.proyectobasesdedatos.controlador;

import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.CategoriaDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.CiudadDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.DepartamentoDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.ProveedorDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.ReporteDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.CategoriaDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.CiudadDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.DatosReporte;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.DepartamentoDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.ProveedorDTO;
import co.edu.uniquindio.proyectobasesdedatos.util.ErroresBD;
import co.edu.uniquindio.proyectobasesdedatos.util.GeneradorPDF;
import co.edu.uniquindio.proyectobasesdedatos.util.Sesion;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de los 5 reportes PDF.
 *
 * <p>Cada método valida los filtros, pide los datos al {@link ReporteDAO},
 * arma el subtítulo con los filtros usados y genera el archivo con
 * {@link GeneradorPDF}. Devuelve el archivo final (con extensión .pdf) para
 * que la vista lo abra en el visor. Cualquier usuario con sesión puede
 * generar reportes ({@link Sesion#puedeConsultar()}).</p>
 */
public class ReporteControlador {

    private static final String EXTENSION = ".pdf";

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final DepartamentoDAO departamentoDAO = new DepartamentoDAO();
    private final CiudadDAO ciudadDAO = new CiudadDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    // ============================================ 1. Proveedores por ubicación

    /**
     * @param codigoDepartamento DCodigo, o null para todos
     * @param codigoCiudad       CCodigo, o null para todas
     * @return el archivo PDF generado
     */
    public File proveedoresPorUbicacion(Integer codigoDepartamento, Integer codigoCiudad, File destino)
            throws ControladorException {
        verificarConsulta();
        File archivo = validarDestino(destino);
        try {
            List<String> filtros = new ArrayList<>();
            DepartamentoDTO departamento = null;
            if (codigoDepartamento != null) {
                departamento = departamentoDAO.buscarPorCodigo(codigoDepartamento);
                if (departamento == null) {
                    throw new ControladorException("El departamento seleccionado ya no existe.");
                }
            }
            if (codigoCiudad != null) {
                CiudadDTO ciudad = ciudadDAO.buscarPorCodigo(codigoCiudad);
                if (ciudad == null) {
                    throw new ControladorException("La ciudad seleccionada ya no existe.");
                }
                if (departamento != null && ciudad.getCodigoDepartamento() != departamento.getCodigo()) {
                    throw new ControladorException("La ciudad seleccionada no pertenece al departamento.");
                }
                filtros.add("Departamento: " + ciudad.getNombreDepartamento());
                filtros.add("Ciudad: " + ciudad.getNombre());
            } else {
                filtros.add("Departamento: " + (departamento == null ? "Todos" : departamento.getNombre()));
                filtros.add("Ciudad: Todas");
            }

            DatosReporte datos = reporteDAO.proveedoresPorUbicacion(codigoDepartamento, codigoCiudad);
            datos.setSubtitulo(unir(filtros) + "  ·  " + cantidad(datos, "proveedor", "proveedores"));
            return generar(datos, archivo);
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    // ===================================== 2. Productos por precio y/o stock

    /**
     * Cualquier límite puede ser null (sin límite). Los que vengan deben ser
     * mayores o iguales a 0 y el mínimo no puede superar al máximo.
     *
     * @return el archivo PDF generado
     */
    public File productosPorPrecioYStock(BigDecimal precioMin, BigDecimal precioMax,
                                         Integer stockMin, Integer stockMax, File destino)
            throws ControladorException {
        verificarConsulta();
        File archivo = validarDestino(destino);
        validarNoNegativo(precioMin, "El precio mínimo");
        validarNoNegativo(precioMax, "El precio máximo");
        validarNoNegativo(stockMin == null ? null : BigDecimal.valueOf(stockMin), "El stock mínimo");
        validarNoNegativo(stockMax == null ? null : BigDecimal.valueOf(stockMax), "El stock máximo");
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            throw new ControladorException("El precio mínimo no puede ser mayor que el precio máximo.");
        }
        if (stockMin != null && stockMax != null && stockMin > stockMax) {
            throw new ControladorException("El stock mínimo no puede ser mayor que el stock máximo.");
        }

        try {
            DatosReporte datos = reporteDAO.productosPorPrecioYStock(precioMin, precioMax, stockMin, stockMax);
            datos.setSubtitulo("Precio base: " + rango(precioMin, precioMax, "$ ")
                    + "  ·  Stock: " + rango(stockMin == null ? null : BigDecimal.valueOf(stockMin),
                    stockMax == null ? null : BigDecimal.valueOf(stockMax), "")
                    + "  ·  " + cantidad(datos, "producto", "productos"));
            return generar(datos, archivo);
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    // ======================================== 3. Productos de un proveedor

    /**
     * @param codigoProveedor PCodigo del proveedor (obligatorio)
     * @return el archivo PDF generado
     */
    public File productosDeProveedor(int codigoProveedor, File destino) throws ControladorException {
        verificarConsulta();
        if (codigoProveedor <= 0) {
            throw new ControladorException("Seleccione un proveedor.");
        }
        File archivo = validarDestino(destino);
        try {
            ProveedorDTO proveedor = proveedorDAO.buscarPorCodigo(codigoProveedor);
            if (proveedor == null) {
                throw new ControladorException("El proveedor seleccionado ya no existe.");
            }
            DatosReporte datos = reporteDAO.productosDeProveedor(codigoProveedor);
            datos.setSubtitulo("Proveedor: " + proveedor.getNombre() + " (NIT " + proveedor.getNit() + ")"
                    + "  ·  Ciudad: " + proveedor.getNombreCiudad()
                    + (proveedor.isActivo() ? "" : "  ·  INACTIVO"));
            return generar(datos, archivo);
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    // ======================================== 4. Productos de una categoría

    /**
     * @param codigoCategoria CaCodigo de la categoría (obligatorio)
     * @return el archivo PDF generado
     */
    public File productosPorCategoria(int codigoCategoria, File destino) throws ControladorException {
        verificarConsulta();
        if (codigoCategoria <= 0) {
            throw new ControladorException("Seleccione una categoría.");
        }
        File archivo = validarDestino(destino);
        try {
            CategoriaDTO categoria = buscarCategoria(codigoCategoria);
            DatosReporte datos = reporteDAO.productosPorCategoria(codigoCategoria);
            datos.setSubtitulo("Categoría: " + categoria.getNombre()
                    + "  ·  " + cantidad(datos, "producto", "productos"));
            return generar(datos, archivo);
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    // =========================================== 5. Resumen por categoría

    /**
     * @param codigoCategoria CaCodigo, o null para todas las categorías
     * @return el archivo PDF generado
     */
    public File resumenPorCategoria(Integer codigoCategoria, File destino) throws ControladorException {
        verificarConsulta();
        File archivo = validarDestino(destino);
        try {
            String nombre = "Todas";
            if (codigoCategoria != null) {
                nombre = buscarCategoria(codigoCategoria).getNombre();
            }
            DatosReporte datos = reporteDAO.resumenPorCategoria(codigoCategoria);
            datos.setSubtitulo("Categoría: " + nombre);
            return generar(datos, archivo);
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    // ------------------------------------------------------- Apoyo interno

    private void verificarConsulta() throws ControladorException {
        if (!Sesion.puedeConsultar()) {
            throw new ControladorException("Debe iniciar sesión para generar reportes.");
        }
    }

    /**
     * Verifica que haya un archivo destino y le agrega ".pdf" si no la tiene.
     *
     * @return el archivo que realmente se va a escribir
     */
    private File validarDestino(File destino) throws ControladorException {
        if (destino == null || destino.getName().isBlank()) {
            throw new ControladorException("Seleccione dónde guardar el reporte.");
        }
        if (destino.getName().toLowerCase().endsWith(EXTENSION)) {
            return destino;
        }
        return new File(destino.getParentFile(), destino.getName() + EXTENSION);
    }

    private static void validarNoNegativo(BigDecimal valor, String campo) throws ControladorException {
        if (valor != null && valor.signum() < 0) {
            throw new ControladorException(campo + " no puede ser negativo.");
        }
    }

    private CategoriaDTO buscarCategoria(int codigo) throws SQLException, ControladorException {
        CategoriaDTO categoria = categoriaDAO.buscarPorCodigo(codigo);
        if (categoria == null) {
            throw new ControladorException("La categoría seleccionada ya no existe.");
        }
        return categoria;
    }

    /** Escribe el PDF; los errores de disco se traducen a un mensaje amigable. */
    private File generar(DatosReporte datos, File archivo) throws ControladorException {
        try {
            GeneradorPDF.generar(datos, archivo);
            return archivo;
        } catch (IOException e) {
            throw new ControladorException("No se pudo guardar el PDF en «" + archivo.getAbsolutePath()
                    + "». Verifique que la carpeta exista y que el archivo no esté abierto en otro programa.", e);
        }
    }

    /** "desde $ 10.000 hasta $ 50.000", "desde 5", "hasta 20" o "todos". */
    private static String rango(BigDecimal min, BigDecimal max, String prefijo) {
        if (min == null && max == null) {
            return "todos";
        }
        StringBuilder sb = new StringBuilder();
        if (min != null) {
            sb.append("desde ").append(prefijo).append(formatear(min));
        }
        if (max != null) {
            sb.append(sb.isEmpty() ? "" : " ").append("hasta ").append(prefijo).append(formatear(max));
        }
        return sb.toString();
    }

    /** Número con punto de miles y coma decimal, sin decimales sobrantes. */
    private static String formatear(BigDecimal valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        return new DecimalFormat("#,##0.##", simbolos).format(valor);
    }

    private static String cantidad(DatosReporte datos, String singular, String plural) {
        int n = datos.getFilas().size();
        return n + " " + (n == 1 ? singular : plural);
    }

    private static String unir(List<String> partes) {
        return String.join("  ·  ", partes);
    }
}
