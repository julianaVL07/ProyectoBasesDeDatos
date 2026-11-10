package co.edu.uniquindio.proyectobasesdedatos.modelo.dto;

import java.time.LocalDate;

/**
 * DTO de la tabla TBitacora: un registro por cada ingreso de un usuario.
 *
 * <p>La fecha y la hora se guardan separadas, como en el diagrama ER. La hora
 * es texto 'HH24:MI:SS'. Mientras la sesión siga abierta, {@code fechaSalida} y
 * {@code horaSalida} son null.</p>
 */
public class BitacoraDTO {

    private int codigo;             // BCodigo (PK, lo genera Oracle)
    private LocalDate fechaIngreso; // BFechaIngreso (DEFAULT TRUNC(SYSDATE))
    private String horaIngreso;     // BHoraIngreso 'HH24:MI:SS'
    private LocalDate fechaSalida;  // BFechaSalida (null = sesión abierta)
    private String horaSalida;      // BHoraSalida  (null = sesión abierta)
    private int codigoUsuario;      // UCodigo (FK -> TUsuario)

    // Solo lectura: se llenan cuando el DAO hace JOIN con TUsuario.
    private String nombreUsuario;   // UNombreUsuario
    private String nombreCompleto;  // UNombreCompleto

    public BitacoraDTO() {
    }

    /** Constructor completo (sin los datos del usuario, que llegan por el JOIN). */
    public BitacoraDTO(int codigo, LocalDate fechaIngreso, String horaIngreso,
                       LocalDate fechaSalida, String horaSalida, int codigoUsuario) {
        this.codigo = codigo;
        this.fechaIngreso = fechaIngreso;
        this.horaIngreso = horaIngreso;
        this.fechaSalida = fechaSalida;
        this.horaSalida = horaSalida;
        this.codigoUsuario = codigoUsuario;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getHoraIngreso() {
        return horaIngreso;
    }

    public void setHoraIngreso(String horaIngreso) {
        this.horaIngreso = horaIngreso;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(String horaSalida) {
        this.horaSalida = horaSalida;
    }

    public int getCodigoUsuario() {
        return codigoUsuario;
    }

    public void setCodigoUsuario(int codigoUsuario) {
        this.codigoUsuario = codigoUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    /** @return true si todavía no se ha registrado la salida */
    public boolean isSesionAbierta() {
        return fechaSalida == null;
    }

    @Override
    public String toString() {
        return "BitacoraDTO{codigo=" + codigo + ", usuario=" + codigoUsuario
                + ", ingreso=" + fechaIngreso + " " + horaIngreso
                + ", salida=" + (isSesionAbierta() ? "abierta" : fechaSalida + " " + horaSalida) + "}";
    }
}
