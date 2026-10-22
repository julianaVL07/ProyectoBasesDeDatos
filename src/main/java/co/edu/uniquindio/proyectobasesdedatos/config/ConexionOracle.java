package co.edu.uniquindio.proyectobasesdedatos.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Entrega conexiones JDBC a Oracle.
 *
 * <p>Las credenciales NO están escritas en el código: se leen del archivo
 * {@code src/main/resources/db.properties} (que no se sube a GitHub).
 * Se parte de la plantilla {@code db.properties.example}.</p>
 *
 * <p>Uso típico en un DAO (el try-with-resources cierra la conexión solo):</p>
 * <pre>
 * try (Connection con = ConexionOracle.getConexion()) {
 *     ...
 * }
 * </pre>
 */
public final class ConexionOracle {

    private static final String ARCHIVO_CONFIG = "/db.properties";

    private static final String URL;
    private static final String USUARIO;
    private static final String CLAVE;

    // Bloque estático: se ejecuta una sola vez, al cargar la clase.
    static {
        Properties props = new Properties();
        try (InputStream in = ConexionOracle.class.getResourceAsStream(ARCHIVO_CONFIG)) {
            if (in == null) {
                throw new IllegalStateException(
                        "No se encontró db.properties en src/main/resources. "
                        + "Copia db.properties.example como db.properties y edítalo.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Error leyendo db.properties", e);
        }
        URL = props.getProperty("db.url");
        USUARIO = props.getProperty("db.user");
        CLAVE = props.getProperty("db.password");
    }

    // Clase de utilidad: no se instancia.
    private ConexionOracle() {
    }

    /**
     * Abre una nueva conexión a Oracle. Quien la pide es responsable de
     * cerrarla (use try-with-resources).
     *
     * @return conexión abierta con auto-commit activado (comportamiento por defecto de JDBC)
     * @throws SQLException si Oracle no está disponible o las credenciales son incorrectas
     */
    public static Connection getConexion() throws SQLException {
        // Con ojdbc11 no hace falta Class.forName(...): el driver se registra solo (JDBC 4+).
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    /**
     * Prueba rápida de conectividad, útil al iniciar la aplicación.
     *
     * @return true si se pudo abrir y validar una conexión
     */
    public static boolean probarConexion() {
        try (Connection con = getConexion()) {
            return con.isValid(5); // espera máximo 5 segundos
        } catch (SQLException e) {
            System.err.println("No se pudo conectar a Oracle: " + e.getMessage());
            return false;
        }
    }
}
