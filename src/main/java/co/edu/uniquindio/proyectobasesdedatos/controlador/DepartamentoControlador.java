package co.edu.uniquindio.proyectobasesdedatos.controlador;

import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.DepartamentoDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.DepartamentoDTO;
import co.edu.uniquindio.proyectobasesdedatos.util.ErroresBD;
import co.edu.uniquindio.proyectobasesdedatos.util.Sesion;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador del CRUD de Departamentos.
 *
 * <p>Valida los datos antes de llamar al {@link DepartamentoDAO} y convierte los
 * errores de Oracle en {@link ControladorException} con mensajes amigables.
 * Las operaciones de escritura exigen {@link Sesion#puedeEditar()}; las de
 * lectura solo exigen tener sesión (se usan también en combos de Ciudades,
 * Proveedores y Reportes).</p>
 */
public class DepartamentoControlador {

    // Longitud máxima según sql/01_crear_tablas.sql
    private static final int NOMBRE_MAX = 60;

    private final DepartamentoDAO departamentoDAO = new DepartamentoDAO();

    // ---------------------------------------------------------------- Lectura

    /** @return todos los departamentos ordenados por nombre */
    public List<DepartamentoDTO> listar() throws ControladorException {
        verificarConsulta();
        try {
            return departamentoDAO.listar();
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    /** Busca por texto en el nombre. Si el texto está vacío lista todo. */
    public List<DepartamentoDTO> buscar(String texto) throws ControladorException {
        verificarConsulta();
        try {
            if (texto == null || texto.isBlank()) {
                return departamentoDAO.listar();
            }
            return departamentoDAO.buscarPorTexto(texto);
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    // ---------------------------------------------------------------- Escritura

    /**
     * Inserta un departamento nuevo; al terminar el DTO trae el código generado.
     *
     * @throws ControladorException si los datos no son válidos o falla la BD
     */
    public void guardar(DepartamentoDTO d) throws ControladorException {
        verificarEdicion();
        validar(d);
        try {
            departamentoDAO.insertar(d);
        } catch (SQLException e) {
            throw new ControladorException(traducir(e), e);
        }
    }

    /**
     * Actualiza el nombre de un departamento existente.
     *
     * @throws ControladorException si no hay departamento seleccionado, los
     *                              datos no son válidos o falla la BD
     */
    public void actualizar(DepartamentoDTO d) throws ControladorException {
        verificarEdicion();
        verificarSeleccion(d == null ? 0 : d.getCodigo());
        validar(d);
        try {
            if (!departamentoDAO.actualizar(d)) {
                throw new ControladorException("El departamento ya no existe.");
            }
        } catch (SQLException e) {
            throw new ControladorException(traducir(e), e);
        }
    }

    /**
     * Elimina un departamento. Si tiene ciudades, Oracle lo impide (ORA-02292).
     *
     * @param codigo DCodigo del departamento
     */
    public void eliminar(int codigo) throws ControladorException {
        verificarEdicion();
        verificarSeleccion(codigo);
        try {
            if (!departamentoDAO.eliminar(codigo)) {
                throw new ControladorException("El departamento ya no existe.");
            }
        } catch (SQLException e) {
            if (ErroresBD.tieneRegistrosAsociados(e)) {
                throw new ControladorException(
                        "No se puede eliminar el departamento porque tiene ciudades asociadas.", e);
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
            throw new ControladorException("Su rol no tiene permiso para modificar departamentos.");
        }
    }

    private void verificarSeleccion(int codigo) throws ControladorException {
        if (codigo <= 0) {
            throw new ControladorException("Seleccione un departamento de la tabla.");
        }
    }

    /** Valida obligatorio y longitud del nombre; además quita espacios sobrantes. */
    private void validar(DepartamentoDTO d) throws ControladorException {
        if (d == null) {
            throw new ControladorException("No hay datos del departamento.");
        }
        String nombre = d.getNombre() == null ? "" : d.getNombre().trim();
        if (nombre.isEmpty()) {
            throw new ControladorException("Ingrese el nombre del departamento.");
        }
        if (nombre.length() > NOMBRE_MAX) {
            throw new ControladorException("El nombre no puede superar " + NOMBRE_MAX + " caracteres.");
        }
        d.setNombre(nombre);
    }

    /** El único dato único de la tabla es el nombre: se da un mensaje específico. */
    private String traducir(SQLException e) {
        String detalle = e.getMessage() == null ? "" : e.getMessage().toUpperCase();
        if (detalle.contains("UQ_DEPARTAMENTO_NOMBRE")) {
            return "Ya existe un departamento con ese nombre.";
        }
        return ErroresBD.traducir(e);
    }
}
