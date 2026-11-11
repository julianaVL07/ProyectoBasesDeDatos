package co.edu.uniquindio.proyectobasesdedatos.modelo.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Contenido de un reporte listo para pasarlo al generador de PDF.
 *
 * <p>Todo viene ya como texto (los números formateados), así el generador no
 * necesita saber de dónde salen los datos. Incluye:</p>
 * <ul>
 *   <li>Título y subtítulo (el subtítulo describe los filtros usados).</li>
 *   <li>Columnas con su ancho relativo (p. ej. {3, 1, 1} = la primera ocupa
 *       el triple) y si se alinean a la derecha (útil para números).</li>
 *   <li>Filas de la tabla y una fila de totales opcional.</li>
 * </ul>
 */
public class DatosReporte {

    private final String titulo;
    private String subtitulo = "";
    private final String[] columnas;
    private final float[] anchos;
    private final boolean[] alineadasDerecha;
    private final List<String[]> filas = new ArrayList<>();
    private String[] totales;      // null = el reporte no tiene fila de totales

    /**
     * @param titulo   título del reporte
     * @param columnas títulos de las columnas
     * @param anchos   ancho relativo de cada columna (mismo tamaño que columnas)
     */
    public DatosReporte(String titulo, String[] columnas, float[] anchos) {
        if (columnas.length != anchos.length) {
            throw new IllegalArgumentException("Debe haber un ancho por cada columna.");
        }
        this.titulo = titulo;
        this.columnas = columnas.clone();
        this.anchos = anchos.clone();
        this.alineadasDerecha = new boolean[columnas.length];
    }

    /** Marca columnas (por índice) para alinearlas a la derecha, como los valores numéricos. */
    public void alinearDerecha(int... indices) {
        for (int i : indices) {
            alineadasDerecha[i] = true;
        }
    }

    /**
     * Agrega una fila. Los valores null se muestran vacíos.
     *
     * @throws IllegalArgumentException si la cantidad de valores no coincide con las columnas
     */
    public void agregarFila(String... valores) {
        filas.add(validarFila(valores));
    }

    private String[] validarFila(String[] valores) {
        if (valores.length != columnas.length) {
            throw new IllegalArgumentException("La fila tiene " + valores.length
                    + " valores y se esperaban " + columnas.length + ".");
        }
        String[] copia = valores.clone();
        for (int i = 0; i < copia.length; i++) {
            if (copia[i] == null) {
                copia[i] = "";
            }
        }
        return copia;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    /** @param subtitulo descripción de los filtros usados (null = sin subtítulo) */
    public void setSubtitulo(String subtitulo) {
        this.subtitulo = subtitulo == null ? "" : subtitulo;
    }

    public String[] getColumnas() {
        return columnas.clone();
    }

    public float[] getAnchos() {
        return anchos.clone();
    }

    public boolean isAlineadaDerecha(int columna) {
        return alineadasDerecha[columna];
    }

    /** @return filas del reporte (solo lectura, nunca null) */
    public List<String[]> getFilas() {
        return Collections.unmodifiableList(filas);
    }

    /** @return la fila de totales, o null si el reporte no tiene */
    public String[] getTotales() {
        return totales == null ? null : totales.clone();
    }

    /** @param totales fila de totales (mismo tamaño que las columnas), o null para quitarla */
    public void setTotales(String... totales) {
        this.totales = totales == null ? null : validarFila(totales);
    }

    public boolean estaVacio() {
        return filas.isEmpty();
    }

    @Override
    public String toString() {
        return "DatosReporte{titulo=" + titulo + ", columnas=" + Arrays.toString(columnas)
                + ", filas=" + filas.size() + "}";
    }
}
