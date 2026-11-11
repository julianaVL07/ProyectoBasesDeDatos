package co.edu.uniquindio.proyectobasesdedatos.modelo.dto;

/**
 * DTO (Data Transfer Object) de la tabla TDepartamento.
 *
 * <p>Solo transporta datos entre la base de datos, el controlador y la vista;
 * no contiene lógica ni acceso a la BD (eso es trabajo del DAO).</p>
 */
public class DepartamentoDTO {

    private int codigo;     // DCodigo  (PK, lo genera Oracle)
    private String nombre;  // DNombre  (único)

    /** Constructor vacío (necesario para ir llenando el objeto con setters). */
    public DepartamentoDTO() {
    }

    /** Constructor completo. */
    public DepartamentoDTO(int codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
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

    /** Devuelve solo el nombre para poder usar el DTO directamente en un JComboBox. */
    @Override
    public String toString() {
        return nombre;
    }
}
