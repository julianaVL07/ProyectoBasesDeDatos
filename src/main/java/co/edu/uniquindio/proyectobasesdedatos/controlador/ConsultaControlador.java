package co.edu.uniquindio.proyectobasesdedatos.controlador;

import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.ConsultaDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.ResultadoConsulta;
import co.edu.uniquindio.proyectobasesdedatos.util.ErroresBD;
import co.edu.uniquindio.proyectobasesdedatos.util.Sesion;

import java.sql.SQLException;

/**
 * Controlador del módulo de Consultas: verifica el permiso del usuario en
 * sesión y ejecuta la consulta elegida en la vista a través de {@link ConsultaDAO}.
 */
public class ConsultaControlador {

    /**
     * Consultas disponibles. Cada una tiene un título corto (para el combo de
     * la vista) y una descripción que explica qué muestra y qué tablas usa.
     */
    public enum Consulta {
        PROVEEDORES_UBICACION(
                "Proveedores con ciudad y departamento",
                "JOIN de 3 tablas (TProveedor, TCiudad, TDepartamento): muestra cada "
                + "proveedor con la ciudad y el departamento donde se ubica."),
        PRODUCTOS_PROVEEDORES(
                "Productos con proveedores y precio de suministro",
                "JOIN de 3 tablas (TProducto, TSuministro, TProveedor): compara el precio "
                + "base de cada producto con el precio que cobra cada proveedor."),
        PRODUCTOS_CATEGORIA_PROVEEDOR(
                "Productos por categoría y proveedor",
                "JOIN de 4 tablas (TProducto, TCategoria, TSuministro, TProveedor): "
                + "muestra por categoría el stock de cada producto, quién lo suministra "
                + "y en cuántos días lo entrega."),
        PRODUCTOS_SOBRE_PROMEDIO(
                "Productos con precio mayor al promedio de su categoría",
                "Subconsulta correlacionada: lista los productos cuyo precio base supera "
                + "el promedio de los productos de su misma categoría."),
        PROVEEDORES_SIN_SUMINISTROS(
                "Proveedores sin suministros",
                "Subconsulta con NOT EXISTS: lista los proveedores que aún no tienen "
                + "ningún producto asignado en TSuministro.");

        private final String titulo;
        private final String descripcion;

        Consulta(String titulo, String descripcion) {
            this.titulo = titulo;
            this.descripcion = descripcion;
        }

        /** @return explicación de la consulta para mostrar en la vista */
        public String getDescripcion() {
            return descripcion;
        }

        /** El combo de la vista muestra el título. */
        @Override
        public String toString() {
            return titulo;
        }
    }

    private final ConsultaDAO dao = new ConsultaDAO();

    /**
     * Ejecuta la consulta indicada.
     *
     * @param consulta consulta elegida por el usuario
     * @return columnas y filas obtenidas (puede no tener filas)
     * @throws ControladorException si no hay permiso, no se eligió consulta o falla la BD
     */
    public ResultadoConsulta ejecutar(Consulta consulta) throws ControladorException {
        if (!Sesion.puedeConsultar()) {
            throw new ControladorException("No tiene permiso para realizar consultas.");
        }
        if (consulta == null) {
            throw new ControladorException("Seleccione una consulta.");
        }
        try {
            return switch (consulta) {
                case PROVEEDORES_UBICACION -> dao.proveedoresConUbicacion();
                case PRODUCTOS_PROVEEDORES -> dao.productosConProveedores();
                case PRODUCTOS_CATEGORIA_PROVEEDOR -> dao.productosPorCategoriaYProveedor();
                case PRODUCTOS_SOBRE_PROMEDIO -> dao.productosSobrePromedioCategoria();
                case PROVEEDORES_SIN_SUMINISTROS -> dao.proveedoresSinSuministros();
            };
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }
}
