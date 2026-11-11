package co.edu.uniquindio.proyectobasesdedatos.modelo.dto;

/**
 * DTO (Data Transfer Object) de la tabla TCiudad.
 *
 * <p>Solo transporta datos entre la base de datos, el controlador y la vista;
 * no contiene lógica ni acceso a la BD (eso es trabajo del DAO).</p>
 */
public class CiudadDTO {

    private int codigo;                // CCodigo  (PK, lo genera Oracle)
    private String nombre;             // CNombre
    private int codigoDepartamento;    // DCodigo  (FK -> TDepartamento)

    // Solo lectura: se llena cuando el DAO hace JOIN con TDepartamento.
    private String nombreDepartamento; // DNombre

    /** Constructor vacío (necesario para ir llenando el objeto con setters). */
    public CiudadDTO() {
    }

    /** Constructor completo (sin nombreDepartamento, que solo se llena al consultar). */
    public CiudadDTO(int codigo, String nombre, int codigoDepartamento) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.codigoDepartamento = codigoDepartamento;
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

    public int getCodigoDepartamento() {
        return codigoDepartamento;
    }

    public void setCodigoDepartamento(int codigoDepartamento) {
        this.codigoDepartamento = codigoDepartamento;
    }

    public String getNombreDepartamento() {
        return nombreDepartamento;
    }

    public void setNombreDepartamento(String nombreDepartamento) {
        this.nombreDepartamento = nombreDepartamento;
    }

    /** Devuelve solo el nombre para poder usar el DTO directamente en un JComboBox. */
    @Override
    public String toString() {
        return nombre;
    }
}
