package co.edu.uniquindio.proyectobasesdedatos.modelo.dto;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) de la tabla TSuministro.
 *
 * <p>Un suministro es la asignación de un producto a un proveedor: indica a qué
 * precio lo vende ese proveedor y en cuántos días lo entrega. La pareja
 * (proveedor, producto) es única en la base de datos.</p>
 *
 * <p>Solo transporta datos entre la base de datos, el controlador y la vista;
 * no contiene lógica ni acceso a la BD (eso es trabajo del DAO).</p>
 */
public class SuministroDTO {

    private int codigo;               // SCodigo       (PK, lo genera Oracle)
    private int diasEntrega;          // SDiasEntrega  NUMBER(3) >= 0
    private BigDecimal precio;        // SPrecio       NUMBER(12,2) >= 0
    private int codigoProveedor;      // PCodigo       (FK -> TProveedor)
    private int codigoProducto;       // PrCodigo      (FK -> TProducto)

    // Solo lectura: se llenan cuando el DAO hace JOIN con TProveedor y TProducto.
    private String nombreProveedor;       // PNombre
    private String nombreProducto;        // PrNombre
    private BigDecimal precioBaseProducto; // PrPrecioBase (referencia para comparar)

    /** Constructor vacío (necesario para ir llenando el objeto con setters). */
    public SuministroDTO() {
    }

    /** Constructor completo (sin los campos de solo lectura, que se llenan al consultar). */
    public SuministroDTO(int codigo, int diasEntrega, BigDecimal precio,
                         int codigoProveedor, int codigoProducto) {
        this.codigo = codigo;
        this.diasEntrega = diasEntrega;
        this.precio = precio;
        this.codigoProveedor = codigoProveedor;
        this.codigoProducto = codigoProducto;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public int getDiasEntrega() {
        return diasEntrega;
    }

    public void setDiasEntrega(int diasEntrega) {
        this.diasEntrega = diasEntrega;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getCodigoProveedor() {
        return codigoProveedor;
    }

    public void setCodigoProveedor(int codigoProveedor) {
        this.codigoProveedor = codigoProveedor;
    }

    public int getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(int codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public BigDecimal getPrecioBaseProducto() {
        return precioBaseProducto;
    }

    public void setPrecioBaseProducto(BigDecimal precioBaseProducto) {
        this.precioBaseProducto = precioBaseProducto;
    }

    @Override
    public String toString() {
        return "SuministroDTO{codigo=" + codigo
                + ", proveedor=" + (nombreProveedor != null ? nombreProveedor : codigoProveedor)
                + ", producto=" + (nombreProducto != null ? nombreProducto : codigoProducto)
                + ", precio=" + precio + ", dias=" + diasEntrega + "}";
    }
}
