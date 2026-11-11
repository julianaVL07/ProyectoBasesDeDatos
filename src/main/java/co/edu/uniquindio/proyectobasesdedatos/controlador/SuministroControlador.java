package co.edu.uniquindio.proyectobasesdedatos.controlador;

import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.ProductoDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.ProveedorDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.SuministroDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.ProductoDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.ProveedorDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.SuministroDTO;
import co.edu.uniquindio.proyectobasesdedatos.util.ErroresBD;
import co.edu.uniquindio.proyectobasesdedatos.util.Sesion;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de la transacción Suministro (asignar productos a proveedores).
 *
 * <p>Valida los datos antes de llamar al {@link SuministroDAO} y convierte los
 * errores de Oracle en {@link ControladorException} con mensajes amigables.
 * Todas las operaciones exigen {@link Sesion#puedeEditar()}, porque la ventana
 * está en el menú Transacciones (Administrador y Tradicional).</p>
 *
 * <p>Reglas (según sql/01_crear_tablas.sql):</p>
 * <ul>
 *   <li>Proveedor y producto obligatorios; el proveedor debe estar activo.</li>
 *   <li>Precio NUMBER(12,2): obligatorio, &ge; 0, máximo 10 enteros y 2 decimales.</li>
 *   <li>Días de entrega NUMBER(3): entero entre 0 y 999.</li>
 *   <li>Un proveedor no puede tener dos veces el mismo producto (UNIQUE(PCodigo, PrCodigo)).</li>
 * </ul>
 */
public class SuministroControlador {

    private static final int PRECIO_ENTEROS_MAX = 10;   // NUMBER(12,2) -> 12 - 2
    private static final int PRECIO_DECIMALES_MAX = 2;
    private static final int DIAS_MAX = 999;            // NUMBER(3)

    /** ORA-00001: se violó UNIQUE(PCodigo, PrCodigo). */
    private static final int ORA_DATO_UNICO = 1;
    /** ORA-02291: el proveedor o el producto ya no existe. */
    private static final int ORA_PADRE_NO_EXISTE = 2291;

    private final SuministroDAO suministroDAO = new SuministroDAO();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    // ---------------------------------------------------------------- Lectura

    /** @return todos los suministros ordenados por proveedor y producto */
    public List<SuministroDTO> listar() throws ControladorException {
        verificarPermiso();
        try {
            return suministroDAO.listar();
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    /** @return los productos que suministra el proveedor */
    public List<SuministroDTO> listarPorProveedor(int codigoProveedor) throws ControladorException {
        verificarPermiso();
        try {
            return suministroDAO.listarPorProveedor(codigoProveedor);
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    /** @return los proveedores que suministran el producto, del más barato al más caro */
    public List<SuministroDTO> listarPorProducto(int codigoProducto) throws ControladorException {
        verificarPermiso();
        try {
            return suministroDAO.listarPorProducto(codigoProducto);
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    /**
     * Proveedores para el combo de la ventana: solo los activos, porque a un
     * proveedor inactivo no se le pueden asignar productos.
     */
    public List<ProveedorDTO> listarProveedoresActivos() throws ControladorException {
        verificarPermiso();
        try {
            List<ProveedorDTO> activos = new ArrayList<>();
            for (ProveedorDTO p : proveedorDAO.listar()) {
                if (p.isActivo()) {
                    activos.add(p);
                }
            }
            return activos;
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    /** @return todos los productos (para el combo de la ventana) */
    public List<ProductoDTO> listarProductos() throws ControladorException {
        verificarPermiso();
        try {
            return productoDAO.listar();
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    // ---------------------------------------------------------------- Escritura

    /**
     * Asigna un producto a un proveedor; al terminar el DTO trae el código generado.
     *
     * @throws ControladorException si los datos no son válidos, el proveedor está
     *                              inactivo, la pareja ya existe o falla la BD
     */
    public void guardar(SuministroDTO s) throws ControladorException {
        verificarPermiso();
        validar(s);
        try {
            verificarProveedorActivo(s.getCodigoProveedor());
            suministroDAO.insertar(s);
        } catch (SQLException e) {
            throw new ControladorException(traducir(e), e);
        }
    }

    /**
     * Modifica un suministro existente (precio, días, proveedor o producto).
     *
     * @throws ControladorException si no hay suministro seleccionado, los datos no
     *                              son válidos, la pareja ya existe o falla la BD
     */
    public void actualizar(SuministroDTO s) throws ControladorException {
        verificarPermiso();
        verificarSeleccion(s == null ? 0 : s.getCodigo());
        validar(s);
        try {
            verificarProveedorActivo(s.getCodigoProveedor());
            if (!suministroDAO.actualizar(s)) {
                throw new ControladorException("El suministro ya no existe.");
            }
        } catch (SQLException e) {
            throw new ControladorException(traducir(e), e);
        }
    }

    /**
     * Quita un producto de un proveedor.
     *
     * @param codigo SCodigo del suministro
     */
    public void eliminar(int codigo) throws ControladorException {
        verificarPermiso();
        verificarSeleccion(codigo);
        try {
            if (!suministroDAO.eliminar(codigo)) {
                throw new ControladorException("El suministro ya no existe.");
            }
        } catch (SQLException e) {
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
            throw new ControladorException("Ingrese el precio de suministro.");
        }
        BigDecimal precio;
        try {
            precio = new BigDecimal(normalizarDecimal(limpio));
        } catch (NumberFormatException e) {
            throw new ControladorException(
                    "El precio debe ser un número (por ejemplo 28900 o 28.900,50).");
        }
        validarPrecio(precio);
        return precio;
    }

    /**
     * Convierte el texto del campo Días de entrega en entero.
     *
     * @throws ControladorException si está vacío, no es entero o está fuera de 0–999
     */
    public int convertirDias(String texto) throws ControladorException {
        String limpio = texto == null ? "" : texto.replace(" ", "").trim();
        if (limpio.isEmpty()) {
            throw new ControladorException("Ingrese los días de entrega.");
        }
        long dias;
        try {
            dias = Long.parseLong(limpio);
        } catch (NumberFormatException e) {
            throw new ControladorException("Los días de entrega deben ser un número entero.");
        }
        validarDias(dias);
        return (int) dias;
    }

    /**
     * Pasa un número escrito por el usuario al formato de {@link BigDecimal}
     * (punto decimal, sin separador de miles). Misma regla que en Productos.
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

    private void verificarPermiso() throws ControladorException {
        if (!Sesion.puedeEditar()) {
            throw new ControladorException("Su rol no tiene permiso para gestionar suministros.");
        }
    }

    private void verificarSeleccion(int codigo) throws ControladorException {
        if (codigo <= 0) {
            throw new ControladorException("Seleccione un suministro de la tabla.");
        }
    }

    /** Valida obligatorios y rangos antes de ir a la base de datos. */
    private void validar(SuministroDTO s) throws ControladorException {
        if (s == null) {
            throw new ControladorException("No hay datos del suministro.");
        }
        if (s.getCodigoProveedor() <= 0) {
            throw new ControladorException("Seleccione el proveedor.");
        }
        if (s.getCodigoProducto() <= 0) {
            throw new ControladorException("Seleccione el producto.");
        }
        if (s.getPrecio() == null) {
            throw new ControladorException("Ingrese el precio de suministro.");
        }
        validarPrecio(s.getPrecio());
        validarDias(s.getDiasEntrega());
    }

    private void validarPrecio(BigDecimal precio) throws ControladorException {
        if (precio.signum() < 0) {
            throw new ControladorException("El precio no puede ser negativo.");
        }
        if (precio.stripTrailingZeros().scale() > PRECIO_DECIMALES_MAX) {
            throw new ControladorException("El precio admite máximo 2 decimales.");
        }
        if (precio.precision() - precio.scale() > PRECIO_ENTEROS_MAX) {
            throw new ControladorException("El precio es demasiado grande.");
        }
    }

    private void validarDias(long dias) throws ControladorException {
        if (dias < 0 || dias > DIAS_MAX) {
            throw new ControladorException("Los días de entrega deben estar entre 0 y " + DIAS_MAX + ".");
        }
    }

    /** El proveedor debe existir y estar activo para asignarle productos. */
    private void verificarProveedorActivo(int codigoProveedor)
            throws SQLException, ControladorException {
        ProveedorDTO proveedor = proveedorDAO.buscarPorCodigo(codigoProveedor);
        if (proveedor == null) {
            throw new ControladorException("El proveedor seleccionado ya no existe. Recargue la ventana.");
        }
        if (!proveedor.isActivo()) {
            throw new ControladorException("El proveedor «" + proveedor.getNombre()
                    + "» está inactivo; no se le pueden asignar productos.");
        }
    }

    /** Mensajes específicos para la pareja repetida y para registros borrados. */
    private String traducir(SQLException e) {
        if (e.getErrorCode() == ORA_DATO_UNICO) {
            return "Ese proveedor ya suministra el producto seleccionado. "
                    + "Selecciónelo en la tabla si desea modificar su precio o días de entrega.";
        }
        if (e.getErrorCode() == ORA_PADRE_NO_EXISTE) {
            return "El proveedor o el producto seleccionado ya no existe. Recargue la ventana.";
        }
        return ErroresBD.traducir(e);
    }
}
