package co.edu.uniquindio.proyectobasesdedatos.controlador;

import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.CategoriaDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.CategoriaDTO;
import co.edu.uniquindio.proyectobasesdedatos.util.ErroresBD;
import co.edu.uniquindio.proyectobasesdedatos.util.Sesion;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador del CRUD de Categorías.
 *
 * <p>Valida los datos antes de llamar al {@link CategoriaDAO} y convierte los
 * errores de Oracle en {@link ControladorException} con mensajes amigables.
 * Las operaciones de escritura exigen {@link Sesion#puedeEditar()}; las de
 * lectura solo exigen tener sesión (se usan también en combos de consultas
 * y reportes).</p>
 */
public class CategoriaControlador {

    // Longitudes máximas según sql/01_crear_tablas.sql
    private static final int NOMBRE_MAX = 60;
    private static final int DESCRIPCION_MAX = 200;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    // ---------------------------------------------------------------- Lectura

    /** @return todas las categorías ordenadas por nombre */
    public List<CategoriaDTO> listar() throws ControladorException {
        verificarConsulta();
        try {
            return categoriaDAO.listar();
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    /**
     * Busca por texto en nombre o descripción. Si el texto está vacío lista todo.
     */
    public List<CategoriaDTO> buscar(String texto) throws ControladorException {
        verificarConsulta();
        try {
            if (texto == null || texto.isBlank()) {
                return categoriaDAO.listar();
            }
            return categoriaDAO.buscarPorTexto(texto);
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    // ---------------------------------------------------------------- Escritura

    /**
     * Inserta una categoría nueva; al terminar el DTO trae el código generado.
     *
     * @throws ControladorException si los datos no son válidos o falla la BD
     */
    public void guardar(CategoriaDTO c) throws ControladorException {
        verificarEdicion();
        validar(c);
        try {
            categoriaDAO.insertar(c);
        } catch (SQLException e) {
            throw new ControladorException(traducir(e), e);
        }
    }

    /**
     * Actualiza nombre y descripción de una categoría existente.
     *
     * @throws ControladorException si no hay categoría seleccionada, los datos
     *                              no son válidos o falla la BD
     */
    public void actualizar(CategoriaDTO c) throws ControladorException {
        verificarEdicion();
        verificarSeleccion(c == null ? 0 : c.getCodigo());
        validar(c);
        try {
            if (!categoriaDAO.actualizar(c)) {
                throw new ControladorException("La categoría ya no existe.");
            }
        } catch (SQLException e) {
            throw new ControladorException(traducir(e), e);
        }
    }

    /**
     * Elimina una categoría. Si tiene productos, Oracle lo impide (ORA-02292).
     *
     * @param codigo CaCodigo de la categoría
     */
    public void eliminar(int codigo) throws ControladorException {
        verificarEdicion();
        verificarSeleccion(codigo);
        try {
            if (!categoriaDAO.eliminar(codigo)) {
                throw new ControladorException("La categoría ya no existe.");
            }
        } catch (SQLException e) {
            if (ErroresBD.tieneRegistrosAsociados(e)) {
                throw new ControladorException(
                        "No se puede eliminar la categoría porque tiene productos asociados.", e);
            }
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    // ------------------------------------------------------- Apoyo interno

    private void verificarConsulta() throws ControladorException {
        if (!Sesion.puedeConsultar()) {
            throw new ControladorException("Debe iniciar sesión.");
        }
    }

    private void verificarEdicion() throws ControladorException {
        if (!Sesion.puedeEditar()) {
            throw new ControladorException("Su rol no tiene permiso para modificar categorías.");
        }
    }

    private void verificarSeleccion(int codigo) throws ControladorException {
        if (codigo <= 0) {
            throw new ControladorException("Seleccione una categoría de la tabla.");
        }
    }

    /** Valida obligatorios y longitudes; además quita espacios sobrantes. */
    private void validar(CategoriaDTO c) throws ControladorException {
        if (c == null) {
            throw new ControladorException("No hay datos de la categoría.");
        }
        String nombre = c.getNombre() == null ? "" : c.getNombre().trim();
        if (nombre.isEmpty()) {
            throw new ControladorException("Ingrese el nombre de la categoría.");
        }
        if (nombre.length() > NOMBRE_MAX) {
            throw new ControladorException("El nombre no puede superar " + NOMBRE_MAX + " caracteres.");
        }
        String descripcion = c.getDescripcion() == null ? "" : c.getDescripcion().trim();
        if (descripcion.length() > DESCRIPCION_MAX) {
            throw new ControladorException("La descripción no puede superar "
                    + DESCRIPCION_MAX + " caracteres.");
        }
        c.setNombre(nombre);
        c.setDescripcion(descripcion.isEmpty() ? null : descripcion);
    }

    /** El único dato único de la tabla es el nombre: se da un mensaje específico. */
    private String traducir(SQLException e) {
        String detalle = e.getMessage() == null ? "" : e.getMessage().toUpperCase();
        if (detalle.contains("UQ_CATEGORIA_NOMBRE")) {
            return "Ya existe una categoría con ese nombre.";
        }
        return ErroresBD.traducir(e);
    }
}
