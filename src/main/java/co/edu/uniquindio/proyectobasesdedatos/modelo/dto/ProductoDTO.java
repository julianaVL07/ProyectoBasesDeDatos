package co.edu.uniquindio.proyectobasesdedatos.modelo.dto;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) de la tabla TProducto.
 *
 * <p>Solo transporta datos entre la base de datos, el controlador y la vista;
 * no contiene lógica ni acceso a la BD (eso es trabajo del DAO).</p>
 */
public class ProductoDTO {

    private int codigo;             // PrCodigo       (PK, lo genera Oracle)
    private String nombre;          // PrNombre
    private String descripcion;     // PrDescripcion  (opcional)
    private BigDecimal precioBase;  // PrPrecioBase   NUMBER(12,2) >= 0
    private int stock;              // PrStock        NUMBER(8) >= 0
    private int codigoCategoria;    // CaCodigo       (FK -> TCategoria)

    // Solo lectura: se llena cuando el DAO hace JOIN con TCategoria.
    private String nombreCategoria; // CaNombre

    /** Constructor vacío (necesario para ir llenando el objeto con setters). */
    public ProductoDTO() {
    }

    /** Constructor completo (sin nombreCategoria, que solo se llena al consultar). */
    public ProductoDTO(int codigo, String nombre, String descripcion, BigDecimal precioBase,
                       int stock, int codigoCategoria) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioBase = precioBase;
        this.stock = stock;
        this.codigoCategoria = codigoCategoria;
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

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getCodigoCategoria() {
        return codigoCategoria;
    }

    public void setCodigoCategoria(int codigoCategoria) {
        this.codigoCategoria = codigoCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    /** Devuelve solo el nombre para poder usar el DTO directamente en un JComboBox. */
    @Override
    public String toString() {
        return nombre;
    }
}
