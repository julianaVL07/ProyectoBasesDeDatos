package co.edu.uniquindio.proyectobasesdedatos;

import co.edu.uniquindio.proyectobasesdedatos.config.ConexionOracle;

/**
 * Punto de entrada de la aplicación.
 * Por ahora solo verifica la conexión a Oracle; luego abrirá la ventana de Login.
 *
 * @author julia
 */
public class ProyectoBasesDeDatos {

    public static void main(String[] args) {
        if (ConexionOracle.probarConexion()) {
            System.out.println("Conexión a Oracle exitosa.");
        } else {
            System.out.println("No se pudo conectar. Revisa db.properties y que Oracle esté activo.");
        }
    }
}
