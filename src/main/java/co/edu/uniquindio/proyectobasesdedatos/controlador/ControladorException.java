package co.edu.uniquindio.proyectobasesdedatos.controlador;

/**
 * Error que el controlador lanza hacia la vista con un mensaje ya listo para
 * mostrarse al usuario (en español y sin detalles técnicos).
 *
 * <p>Uso típico en una vista:</p>
 * <pre>
 * try {
 *     controlador.guardar(dto);
 * } catch (ControladorException e) {
 *     JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 * }
 * </pre>
 *
 * <p>Cuando el error viene de la base de datos se conserva la causa original
 * ({@link java.sql.SQLException}) para poder depurarla, pero el mensaje que
 * ve el usuario lo arma {@code ErroresBD.traducir}.</p>
 */
public class ControladorException extends Exception {

    /**
     * Error de validación o de permisos (no hay excepción de origen).
     *
     * @param mensaje texto amigable para el usuario
     */
    public ControladorException(String mensaje) {
        super(mensaje);
    }

    /**
     * Error que proviene de otra excepción (normalmente una SQLException).
     *
     * @param mensaje texto amigable para el usuario
     * @param causa   excepción original, útil para depurar
     */
    public ControladorException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
