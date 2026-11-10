package co.edu.uniquindio.proyectobasesdedatos.modelo.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resultado genérico de una consulta para mostrarlo en una {@code JTable}.
 *
 * <p>Guarda los títulos de las columnas y las filas como arreglos de objetos
 * (un {@code Object[]} por fila, en el mismo orden que las columnas). Así la
 * vista puede pintar cualquier consulta sin conocer su SQL ni sus tipos.</p>
 */
public class ResultadoConsulta {

    private final String[] columnas;
    private final List<Object[]> filas;

    /**
     * Crea un resultado vacío con las columnas indicadas.
     *
     * @param columnas títulos que se mostrarán en el encabezado de la tabla
     */
    public ResultadoConsulta(String... columnas) {
        this.columnas = columnas.clone();
        this.filas = new ArrayList<>();
    }

    /**
     * Agrega una fila al resultado.
     *
     * @param valores valores de la fila, en el mismo orden que las columnas
     * @throws IllegalArgumentException si la cantidad de valores no coincide con las columnas
     */
    public void agregarFila(Object... valores) {
        if (valores.length != columnas.length) {
            throw new IllegalArgumentException("La fila tiene " + valores.length
                    + " valores y se esperaban " + columnas.length + ".");
        }
        filas.add(valores);
    }

    /** @return copia de los títulos de las columnas */
    public String[] getColumnas() {
        return columnas.clone();
    }

    /** @return filas del resultado (lista de solo lectura, nunca null) */
    public List<Object[]> getFilas() {
        return Collections.unmodifiableList(filas);
    }

    /** @return cantidad de filas obtenidas */
    public int getCantidadFilas() {
        return filas.size();
    }

    /** @return true si la consulta no devolvió filas */
    public boolean estaVacio() {
        return filas.isEmpty();
    }
}
