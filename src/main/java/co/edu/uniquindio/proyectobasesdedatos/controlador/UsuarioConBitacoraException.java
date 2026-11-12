package co.edu.uniquindio.proyectobasesdedatos.controlador;

/**
 * Se lanza al intentar eliminar un usuario que tiene registros en la Bitácora
 * (ORA-02292).
 *
 * <p>Es una {@link ControladorException} normal (se puede mostrar tal cual),
 * pero al ser un tipo propio la vista puede detectarla y ofrecer desactivar el
 * usuario ({@code UActivo = 0}) en lugar de borrarlo, conservando su historial.</p>
 */
public class UsuarioConBitacoraException extends ControladorException {

    public UsuarioConBitacoraException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
