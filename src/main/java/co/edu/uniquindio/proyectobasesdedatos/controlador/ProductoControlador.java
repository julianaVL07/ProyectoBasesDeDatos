package co.edu.uniquindio.proyectobasesdedatos.controlador;

import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.ProductoDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.ProductoDTO;
import co.edu.uniquindio.proyectobasesdedatos.util.ErroresBD;
import co.edu.uniquindio.proyectobasesdedatos.util.Sesion;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Controlador del CRUD de Productos.
 *
 * <p>Valida los datos antes de llamar al {@link ProductoDAO} y convierte los
 * errores de Oracle en {@link ControladorException} con mensajes amigables.
 * Las operaciones de escritura exigen {@link Sesion#puedeEditar()}; las de
 * lectura solo exigen tener sesión.</p>
 *
 * <p>Reglas (según sql/01_crear_tablas.sql):</p>
 * <ul>
 *   <li>Nombre obligatorio, máximo 100 caracteres; descripción máximo 250.</li>
 *   <li>Precio base NUMBER(12,2): obligatorio, &ge; 0, máximo 10 enteros y 2 decimales.</li>
 *   <li>Stock NUMBER(8): entero &ge; 0, máximo 99.999.999.</li>
 *   <li>Categoría obligatoria.</li>
 * </ul>
 */
public class ProductoControlador {

    private static final int NOMBRE_MAX = 100;
    private static final int DESCRIPCION_MAX = 250;
    private static final int PRECIO_ENTEROS_MAX = 10;   // NUMBER(12,2) -> 12 - 2
    private static final int PRECIO_DECIMALES_MAX = 2;
    private static final int STOCK_MAX = 99_999_999;    // NUMBER(8)

    private final ProductoDAO productoDAO = new ProductoDAO();

    // ---------------------------------------------------------------- Lectura

    /** @return todos los productos ordenados por nombre */
    public List<ProductoDTO> listar() throws ControladorException {
        verificarConsulta();
        try {
            return productoDAO.listar();
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    /** @return los productos de una categoría */
    public List<ProductoDTO> listarPorCategoria(int codigoCategoria) throws ControladorException {
        verificarConsulta();
        try {
            return productoDAO.listarPorCategoria(codigoCategoria);
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    /**
     * Busca por texto en el nombre del producto o de su categoría. Si el texto
     * está vacío lista todo.
     */
    public List<ProductoDTO> buscar(String texto) throws ControladorException {
        verificarConsulta();
        try {
            if (texto == null || texto.isBlank()) {
                return productoDAO.listar();
            }
            return productoDAO.buscarPorTexto(texto);
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    // ---------------------------------------------------------------- Escritura

    /**
     * Inserta un producto nuevo; al terminar el DTO trae el código generado.
     *
     * @throws ControladorException si los datos no son válidos o falla la BD
     */
    public void guardar(ProductoDTO p) throws ControladorException {
        verificarEdicion();
        validar(p);
        try {
            productoDAO.insertar(p);
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    /**
     * Actualiza un producto existente.
     *
     * @throws ControladorException si no hay producto seleccionado, los datos no
     *                              son válidos o falla la BD
     */
    public void actualizar(ProductoDTO p) throws ControladorException {
        verificarEdicion();
        verificarSeleccion(p == null ? 0 : p.getCodigo());
        validar(p);
        try {
            if (!productoDAO.actualizar(p)) {
                throw new ControladorException("El producto ya no existe.");
            }
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    /**
     * Elimina un producto. Si tiene suministros, Oracle lo impide (ORA-02292).
     *
     * @param codigo PrCodigo del producto
     */
    public void eliminar(int codigo) throws ControladorException {
        verificarEdicion();
        verificarSeleccion(codigo);
        try {
            if (!productoDAO.eliminar(codigo)) {
                throw new ControladorException("El producto ya no existe.");
            }
        } catch (SQLException e) {
            if (ErroresBD.tieneRegistrosAsociados(e)) {
                throw new ControladorException(
                        "No se puede eliminar el producto porque tiene suministros asociados.", e);
            }
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    // ------------------------------------------------- Conversión de los textos

    /**
     * Convierte el texto del campo Precio en número. Acepta el formato
     * colombiano ("28.900,50") y el simple ("28900.50"); ignora "$" y espacios.
     *
     * @throws ControladorException si está vacío, no es un número, es negativo
     *                              o no cabe en NUMBER(12,2)
     */
    public BigDecimal convertirPrecio(String texto) throws ControladorException {
        String limpio = texto == null ? "" : texto.replace("$", "").replace(" ", "").trim();
        if (limpio.isEmpty()) {
            throw new ControladorException("Ingrese el precio base.");
        }
        BigDecimal precio;
        try {
            precio = new BigDecimal(normalizarDecimal(limpio));
        } catch (NumberFormatException e) {
            throw new ControladorException("El precio base debe ser un número (por ejemplo 28900 o 28.900,50).");
        }
        validarPrecio(precio);
        return precio;
    }

    /**
     * Convierte el texto del campo Stock en entero (acepta separador de miles con punto).
     *
     * @throws ControladorException si está vacío, no es entero, es negativo o es muy grande
     */
    public int convertirStock(String texto) throws ControladorException {
        String limpio = texto == null ? "" : texto.replace(" ", "").trim();
        if (limpio.isEmpty()) {
            throw new ControladorException("Ingrese el stock.");
        }
        // El punto solo se acepta como separador de miles ("1.000"), no como decimal ("2.5").
        if (limpio.matches("-?\\d{1,3}(\\.\\d{3})+")) {
            limpio = limpio.replace(".", "");
        }
        long stock;
        try {
            stock = Long.parseLong(limpio);
        } catch (NumberFormatException e) {
            throw new ControladorException("El stock debe ser un número entero.");
        }
        if (stock < 0) {
            throw new ControladorException("El stock no puede ser negativo.");
        }
        if (stock > STOCK_MAX) {
            throw new ControladorException("El stock no puede superar 99.999.999 unidades.");
        }
        return (int) stock;
    }

    /**
     * Pasa un número escrito por el usuario al formato de {@link BigDecimal}
     * (punto decimal, sin separador de miles).
     * <ul>
     *   <li>Con punto y coma ("28.900,50"): el punto es de miles y la coma decimal.</li>
     *   <li>Solo coma ("28900,5"): la coma es decimal.</li>
     *   <li>Solo puntos en grupos de 3 ("28.900" o "1.250.000"): son de miles.</li>
     *   <li>Cualquier otro caso ("28900.50"): el punto es decimal.</li>
     * </ul>
     */
    private String normalizarDecimal(String s) {
        if (s.contains(",")) {
            return s.replace(".", "").replace(",", ".");
        }
        if (s.matches("-?\\d{1,3}(\\.\\d{3})+")) {
            return s.replace(".", "");
        }
        return s;
    }

    // ------------------------------------------------------- Apoyo interno

    private void verificarConsulta() throws ControladorException {
        if (!Sesion.puedeConsultar()) {
            throw new ControladorException("Debe iniciar sesión.");
        }
    }

    private void verificarEdicion() throws ControladorException {
        if (!Sesion.puedeEditar()) {
            throw new ControladorException("Su rol no tiene permiso para modificar productos.");
        }
    }

    private void verificarSeleccion(int codigo) throws ControladorException {
        if (codigo <= 0) {
            throw new ControladorException("Seleccione un producto de la tabla.");
        }
    }

    /** Valida obligatorios, longitudes y rangos; además quita espacios sobrantes. */
    private void validar(ProductoDTO p) throws ControladorException {
        if (p == null) {
            throw new ControladorException("No hay datos del producto.");
        }
        String nombre = p.getNombre() == null ? "" : p.getNombre().trim();
        if (nombre.isEmpty()) {
            throw new ControladorException("Ingrese el nombre del producto.");
        }
        if (nombre.length() > NOMBRE_MAX) {
            throw new ControladorException("El nombre no puede superar " + NOMBRE_MAX + " caracteres.");
        }
        String descripcion = p.getDescripcion() == null ? "" : p.getDescripcion().trim();
        if (descripcion.length() > DESCRIPCION_MAX) {
            throw new ControladorException("La descripción no puede superar "
                    + DESCRIPCION_MAX + " caracteres.");
        }
        if (p.getPrecioBase() == null) {
            throw new ControladorException("Ingrese el precio base.");
        }
        validarPrecio(p.getPrecioBase());
        if (p.getStock() < 0) {
            throw new ControladorException("El stock no puede ser negativo.");
        }
        if (p.getStock() > STOCK_MAX) {
            throw new ControladorException("El stock no puede superar 99.999.999 unidades.");
        }
        if (p.getCodigoCategoria() <= 0) {
            throw new ControladorException("Seleccione la categoría del producto.");
        }
        p.setNombre(nombre);
        p.setDescripcion(descripcion.isEmpty() ? null : descripcion);
    }

    private void validarPrecio(BigDecimal precio) throws ControladorException {
        if (precio.signum() < 0) {
            throw new ControladorException("El precio base no puede ser negativo.");
        }
        BigDecimal sinCeros = precio.stripTrailingZeros();
        if (sinCeros.scale() > PRECIO_DECIMALES_MAX) {
            throw new ControladorException("El precio base admite máximo 2 decimales.");
        }
        if (precio.precision() - precio.scale() > PRECIO_ENTEROS_MAX) {
            throw new ControladorException("El precio base es demasiado grande.");
        }
    }
}
