package co.edu.uniquindio.proyectobasesdedatos.modelo.dto;

/**
 * DTO (Data Transfer Object) de la tabla TCategoria.
 *
 * <p>Solo transporta datos entre la base de datos, el controlador y la vista;
 * no contiene lógica ni acceso a la BD (eso es trabajo del DAO).</p>
 */
public class CategoriaDTO {

    private int codigo;          // CaCodigo       (PK, lo genera Oracle)
    private String nombre;       // CaNombre       (único)
    private String descripcion;  // CaDescripcion  (opcional)

    /** Constructor vacío (necesario para ir llenando el objeto con setters). */
    public CategoriaDTO() {
    }

    /** Constructor completo. */
    public CategoriaDTO(int codigo, String nombre, String descripcion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** Devuelve solo el nombre para poder usar el DTO directamente en un JComboBox. */
    @Override
    public String toString() {
        return nombre;
    }
}
