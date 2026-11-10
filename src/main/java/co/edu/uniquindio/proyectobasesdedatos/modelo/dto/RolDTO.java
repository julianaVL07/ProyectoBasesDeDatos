package co.edu.uniquindio.proyectobasesdedatos.modelo.dto;

/**
 * DTO de la tabla TRol.
 *
 * <p>Los códigos de los 3 roles son fijos (ver {@code sql/02_datos_iniciales.sql})
 * y se exponen como constantes para no escribir "números mágicos" en el código.</p>
 */
public class RolDTO {

    /** RCodigo del Administrador (solo puede existir un usuario con este rol). */
    public static final int ADMINISTRADOR = 1;
    /** RCodigo del usuario tradicional: ejecuta los CRUD. */
    public static final int TRADICIONAL = 2;
    /** RCodigo del usuario esporádico: solo consultas y reportes. */
    public static final int ESPORADICO = 3;

    private int codigo;          // RCodigo (PK)
    private String nombre;       // RNombre
    private String descripcion;  // RDescripcion

    public RolDTO() {
    }

    public RolDTO(int codigo, String nombre, String descripcion) {
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

    /** Devuelve el nombre: así un JComboBox&lt;RolDTO&gt; muestra el texto correcto. */
    @Override
    public String toString() {
        return nombre;
    }
}
