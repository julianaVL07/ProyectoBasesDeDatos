package co.edu.uniquindio.proyectobasesdedatos.util;

import java.sql.SQLException;

/**
 * Traduce los errores de Oracle (códigos ORA-xxxxx) a mensajes en español que
 * se le pueden mostrar al usuario final.
 *
 * <p>Los controladores la usan así:</p>
 * <pre>
 * } catch (SQLException e) {
 *     throw new ControladorException(ErroresBD.traducir(e), e);
 * }
 * </pre>
 */
public final class ErroresBD {

    // Códigos de error de Oracle más comunes (SQLException.getErrorCode()).
    private static final int ORA_DATO_UNICO = 1;              // ORA-00001 unique constraint violated
    private static final int ORA_NULO = 1400;                 // ORA-01400 cannot insert NULL
    private static final int ORA_CHECK = 2290;                // ORA-02290 check constraint violated
    private static final int ORA_PADRE_NO_EXISTE = 2291;      // ORA-02291 parent key not found
    private static final int ORA_TIENE_HIJOS = 2292;          // ORA-02292 child record found
    private static final int ORA_TEXTO_LARGO = 12899;         // ORA-12899 value too large for column
    private static final int ORA_SIN_CONEXION = 17002;        // IO Error (red / listener caído)
    private static final int ORA_CONEXION_CERRADA = 17008;    // Closed Connection

    // Clase de utilidad: no se instancia.
    private ErroresBD() {
    }

    /**
     * Devuelve un mensaje amigable según el código de error de Oracle.
     *
     * @param e excepción lanzada por el DAO
     * @return mensaje en español listo para mostrar en un JOptionPane
     */
    public static String traducir(SQLException e) {
        if (e == null) {
            return "Ocurrió un error desconocido en la base de datos.";
        }
        return switch (e.getErrorCode()) {
            case ORA_DATO_UNICO ->
                    "Ya existe un registro con ese dato único (por ejemplo, nombre o NIT repetido).";
            case ORA_PADRE_NO_EXISTE ->
                    "El registro relacionado no existe (verifique la ciudad, categoría, proveedor o producto seleccionado).";
            case ORA_TIENE_HIJOS ->
                    "No se puede eliminar porque tiene registros asociados.";
            case ORA_CHECK ->
                    "Un valor no cumple las reglas de la base de datos (por ejemplo, un número negativo).";
            case ORA_NULO ->
                    "Falta un dato obligatorio.";
            case ORA_TEXTO_LARGO ->
                    "Uno de los textos es demasiado largo para el campo.";
            case ORA_SIN_CONEXION, ORA_CONEXION_CERRADA ->
                    "No hay conexión con la base de datos. Verifique que Oracle esté activo.";
            default ->
                    "Ocurrió un error en la base de datos (ORA-"
                    + String.format("%05d", e.getErrorCode()) + ").";
        };
    }

    /**
     * Indica si el error es porque el registro tiene hijos (ORA-02292). Útil,
     * por ejemplo, para sugerir desactivar un proveedor en lugar de borrarlo.
     */
    public static boolean tieneRegistrosAsociados(SQLException e) {
        return e != null && e.getErrorCode() == ORA_TIENE_HIJOS;
    }
}
