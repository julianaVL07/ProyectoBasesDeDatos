package co.edu.uniquindio.proyectobasesdedatos.controlador;

/**
 * Se lanza al intentar eliminar un proveedor que tiene suministros (ORA-02292).
 *
 * <p>Es una {@link ControladorException} normal (se puede mostrar tal cual),
 * pero al ser un tipo propio la vista puede detectarla y ofrecer desactivar el
 * proveedor ({@code PActivo = 0}) en lugar de borrarlo.</p>
 */
public class ProveedorConSuministrosException extends ControladorException {

    public ProveedorConSuministrosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
