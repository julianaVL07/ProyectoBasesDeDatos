package co.edu.uniquindio.proyectobasesdedatos.controlador;

import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.CiudadDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.CiudadDTO;
import co.edu.uniquindio.proyectobasesdedatos.util.ErroresBD;
import co.edu.uniquindio.proyectobasesdedatos.util.Sesion;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador del CRUD de Ciudades.
 *
 * <p>Valida los datos antes de llamar al {@link CiudadDAO} y convierte los
 * errores de Oracle en {@link ControladorException} con mensajes amigables.
 * Las operaciones de escritura exigen {@link Sesion#puedeEditar()}; las de
 * lectura solo exigen tener sesión (se usan también en los combos de
 * Proveedores y Reportes).</p>
 */
public class CiudadControlador {

    // Longitud máxima según sql/01_crear_tablas.sql
    private static final int NOMBRE_MAX = 60;

    /** Código de Oracle: la llave foránea apunta a un registro que no existe. */
    private static final int ORA_PADRE_NO_EXISTE = 2291;

    private final CiudadDAO ciudadDAO = new CiudadDAO();

    // ---------------------------------------------------------------- Lectura

    /** @return todas las ciudades ordenadas por departamento y nombre */
    public List<CiudadDTO> listar() throws ControladorException {
        verificarConsulta();
        try {
            return ciudadDAO.listar();
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    /**
     * Ciudades de un departamento (para combos dependientes).
     *
     * @param codigoDepartamento DCodigo del departamento
     */
    public List<CiudadDTO> listarPorDepartamento(int codigoDepartamento) throws ControladorException {
        verificarConsulta();
        try {
            return ciudadDAO.listarPorDepartamento(codigoDepartamento);
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    /**
     * Busca por texto en el nombre de la ciudad o del departamento. Si el texto
     * está vacío lista todo.
     */
    public List<CiudadDTO> buscar(String texto) throws ControladorException {
        verificarConsulta();
        try {
            if (texto == null || texto.isBlank()) {
                return ciudadDAO.listar();
            }
            return ciudadDAO.buscarPorTexto(texto);
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    // ---------------------------------------------------------------- Escritura

    /**
     * Inserta una ciudad nueva; al terminar el DTO trae el código generado.
     *
     * @throws ControladorException si los datos no son válidos o falla la BD
     */
    public void guardar(CiudadDTO c) throws ControladorException {
        verificarEdicion();
        validar(c);
        try {
            ciudadDAO.insertar(c);
        } catch (SQLException e) {
            throw new ControladorException(traducir(e), e);
        }
    }

    /**
     * Actualiza nombre y departamento de una ciudad existente.
     *
     * @throws ControladorException si no hay ciudad seleccionada, los datos no
     *                              son válidos o falla la BD
     */
    public void actualizar(CiudadDTO c) throws ControladorException {
        verificarEdicion();
        verificarSeleccion(c == null ? 0 : c.getCodigo());
        validar(c);
        try {
            if (!ciudadDAO.actualizar(c)) {
                throw new ControladorException("La ciudad ya no existe.");
            }
        } catch (SQLException e) {
            throw new ControladorException(traducir(e), e);
        }
    }

    /**
     * Elimina una ciudad. Si tiene proveedores, Oracle lo impide (ORA-02292).
     *
     * @param codigo CCodigo de la ciudad
     */
    public void eliminar(int codigo) throws ControladorException {
        verificarEdicion();
        verificarSeleccion(codigo);
        try {
            if (!ciudadDAO.eliminar(codigo)) {
                throw new ControladorException("La ciudad ya no existe.");
            }
        } catch (SQLException e) {
            if (ErroresBD.tieneRegistrosAsociados(e)) {
                throw new ControladorException(
                        "No se puede eliminar la ciudad porque tiene proveedores asociados.", e);
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
            throw new ControladorException("Su rol no tiene permiso para modificar ciudades.");
        }
    }

    private void verificarSeleccion(int codigo) throws ControladorException {
        if (codigo <= 0) {
            throw new ControladorException("Seleccione una ciudad de la tabla.");
        }
    }

    /** Valida obligatorios y longitud; además quita espacios sobrantes. */
    private void validar(CiudadDTO c) throws ControladorException {
        if (c == null) {
            throw new ControladorException("No hay datos de la ciudad.");
        }
        String nombre = c.getNombre() == null ? "" : c.getNombre().trim();
        if (nombre.isEmpty()) {
            throw new ControladorException("Ingrese el nombre de la ciudad.");
        }
        if (nombre.length() > NOMBRE_MAX) {
            throw new ControladorException("El nombre no puede superar " + NOMBRE_MAX + " caracteres.");
        }
        if (c.getCodigoDepartamento() <= 0) {
            throw new ControladorException("Seleccione el departamento de la ciudad.");
        }
        c.setNombre(nombre);
    }

    /** Si el departamento fue borrado mientras tanto se da un mensaje específico. */
    private String traducir(SQLException e) {
        if (e.getErrorCode() == ORA_PADRE_NO_EXISTE) {
            return "El departamento seleccionado ya no existe. Recargue la ventana.";
        }
        return ErroresBD.traducir(e);
    }
}
