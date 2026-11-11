package co.edu.uniquindio.proyectobasesdedatos.modelo.dto;

/**
 * DTO (Data Transfer Object) de la tabla TProveedor.
 *
 * <p>Solo transporta datos entre la base de datos, el controlador y la vista;
 * no contiene lógica ni acceso a la BD (eso es trabajo del DAO).</p>
 */
public class ProveedorDTO {

    private int codigo;          // PCodigo  (PK, lo genera Oracle)
    private String nit;          // PNit
    private String nombre;       // PNombre
    private String direccion;    // PDireccion
    private String correo;       // PCorreo
    private String telefono;     // PTelefono
    private boolean activo;      // PActivo  (1 = activo, 0 = inactivo)
    private int codigoCiudad;    // CCodigo  (FK -> TCiudad)

    // Solo lectura: se llena cuando el DAO hace JOIN con TCiudad (útil para mostrar en tablas).
    private String nombreCiudad; // CNombre

    /** Constructor vacío (necesario para ir llenando el objeto con setters). */
    public ProveedorDTO() {
    }

    /** Constructor completo (sin nombreCiudad, que solo se llena al consultar). */
    public ProveedorDTO(int codigo, String nit, String nombre, String direccion,
                        String correo, String telefono, boolean activo, int codigoCiudad) {
        this.codigo = codigo;
        this.nit = nit;
        this.nombre = nombre;
        this.direccion = direccion;
        this.correo = correo;
        this.telefono = telefono;
        this.activo = activo;
        this.codigoCiudad = codigoCiudad;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public int getCodigoCiudad() {
        return codigoCiudad;
    }

    public void setCodigoCiudad(int codigoCiudad) {
        this.codigoCiudad = codigoCiudad;
    }

    public String getNombreCiudad() {
        return nombreCiudad;
    }

    public void setNombreCiudad(String nombreCiudad) {
        this.nombreCiudad = nombreCiudad;
    }

    /** Devuelve solo el nombre para poder usar el DTO directamente en un JComboBox. */
    @Override
    public String toString() {
        return nombre;
    }
}
