package co.edu.uniquindio.proyectobasesdedatos.controlador;

import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.BitacoraDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.BitacoraDTO;
import co.edu.uniquindio.proyectobasesdedatos.util.ErroresBD;
import co.edu.uniquindio.proyectobasesdedatos.util.Sesion;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador de la Bitácora de entradas y salidas (módulo Auditoría).
 * Solo el Administrador puede consultarla.
 */
public class BitacoraControlador {

    private final BitacoraDAO dao = new BitacoraDAO();

    /**
     * Lista los registros de la bitácora con filtros opcionales (null = sin filtro).
     *
     * @param desde      fecha de ingreso inicial (inclusive) o null
     * @param hasta      fecha de ingreso final (inclusive) o null
     * @param codUsuario UCodigo del usuario o null para todos
     * @return registros del más reciente al más antiguo
     * @throws ControladorException si no hay permiso, el rango de fechas no es
     *                              válido o falla la BD
     */
    public List<BitacoraDTO> listar(LocalDate desde, LocalDate hasta, Integer codUsuario)
            throws ControladorException {
        if (!Sesion.puedeVerBitacora()) {
            throw new ControladorException("Solo el administrador puede ver la bitácora.");
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ControladorException("La fecha \"desde\" no puede ser posterior a la fecha \"hasta\".");
        }
        try {
            return dao.listar(desde, hasta, codUsuario);
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }
}
