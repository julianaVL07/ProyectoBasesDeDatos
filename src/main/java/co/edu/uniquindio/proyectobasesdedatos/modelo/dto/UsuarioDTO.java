package co.edu.uniquindio.proyectobasesdedatos.modelo.dto;

import java.time.LocalDate;

/**
 * DTO de la tabla TUsuario.
 *
 * <p>{@code contrasena} contiene SIEMPRE el hash SHA-256 (ver
 * {@link co.edu.uniquindio.proyectobasesdedatos.util.Seguridad}), nunca la
 * clave en texto plano.</p>
 */
public class UsuarioDTO {

    private int codigo;              // UCodigo (PK, lo genera Oracle)
    private String nombreUsuario;    // UNombreUsuario (único)
    private String contrasena;       // UContrasena (hash SHA-256)
    private boolean activo;          // UActivo (1 = activo, 0 = inactivo)
    private LocalDate fechaCreacion; // UFechaCreacion (Oracle pone SYSDATE por defecto)
    private String nombreCompleto;   // UNombreCompleto
    private int codigoRol;           // RCodigo (FK -> TRol)

    // Solo lectura: se llena cuando el DAO hace JOIN con TRol.
    private String nombreRol;        // RNombre

    public UsuarioDTO() {
    }

    /** Constructor completo (sin fechaCreacion ni nombreRol, que llenan Oracle / el JOIN). */
    public UsuarioDTO(int codigo, String nombreUsuario, String contrasena, boolean activo,
                      String nombreCompleto, int codigoRol) {
        this.codigo = codigo;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.activo = activo;
        this.nombreCompleto = nombreCompleto;
        this.codigoRol = codigoRol;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public int getCodigoRol() {
        return codigoRol;
    }

    public void setCodigoRol(int codigoRol) {
        this.codigoRol = codigoRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    /** No incluye la contraseña a propósito (para que no salga en logs). */
    @Override
    public String toString() {
        return "UsuarioDTO{codigo=" + codigo + ", usuario=" + nombreUsuario
                + ", nombre=" + nombreCompleto
                + ", rol=" + (nombreRol != null ? nombreRol : codigoRol)
                + ", activo=" + activo + "}";
    }
}
