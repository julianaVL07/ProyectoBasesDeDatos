package co.edu.uniquindio.proyectobasesdedatos.util;

import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.DatosReporte;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.UsuarioDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera el archivo PDF de un reporte con Apache PDFBox.
 *
 * <p>Formato de cada página:</p>
 * <ul>
 *   <li>Hoja carta; horizontal si el reporte tiene más de 5 columnas.</li>
 *   <li>Encabezado institucional, título y filtros (subtítulo).</li>
 *   <li>Tabla con encabezado sombreado, filas alternadas y ajuste de texto
 *       (si un texto no cabe, sigue en la línea de abajo).</li>
 *   <li>Si la tabla no cabe, continúa en otra página repitiendo el encabezado.</li>
 *   <li>Pie con fecha y hora, usuario de la {@link Sesion} y "Página X de Y".</li>
 * </ul>
 *
 * <p>Se usan las fuentes estándar Helvetica, que traen tildes y eñes
 * (codificación WinAnsi); cualquier carácter que no exista se cambia por "?".</p>
 */
public final class GeneradorPDF {

    private static final String ENCABEZADO = "Universidad del Quindío — Proyecto Final Bases de Datos I";
    private static final String SIN_DATOS = "No hay datos para los filtros seleccionados.";

    private static final float MARGEN = 40;
    private static final float ALTO_PIE = 30;
    private static final float RELLENO = 4;          // espacio entre el texto y el borde de la celda
    private static final float TAM_TABLA = 9;
    private static final float INTERLINEA = 11;

    private static final Color COLOR_ENCABEZADO = new Color(204, 217, 234);
    private static final Color COLOR_ALTERNO = new Color(242, 245, 249);
    private static final Color COLOR_TOTALES = new Color(225, 225, 225);
    private static final Color COLOR_BORDE = new Color(170, 170, 170);
    private static final Color COLOR_TENUE = new Color(90, 90, 90);

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private GeneradorPDF() {
    }

    /**
     * Crea el PDF del reporte en el archivo indicado (lo reemplaza si existe).
     *
     * @throws IOException si no se puede escribir el archivo
     */
    public static void generar(DatosReporte datos, File destino) throws IOException {
        try (PDDocument documento = new PDDocument()) {
            new Dibujante(documento, datos).dibujar();
            documento.save(destino);
        }
    }

    // =====================================================================

    /** Lleva el estado mientras se dibuja: página actual, posición vertical, etc. */
    private static final class Dibujante {

        private final PDDocument documento;
        private final DatosReporte datos;
        private final PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDType1Font negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private final PDRectangle tamano;
        private final float[] anchos;              // anchos reales de las columnas en puntos
        private final float anchoUtil;

        private PDPageContentStream contenido;
        private float y;                            // posición vertical actual (0 = abajo)

        Dibujante(PDDocument documento, DatosReporte datos) {
            this.documento = documento;
            this.datos = datos;
            int columnas = datos.getColumnas().length;
            this.tamano = columnas > 5
                    ? new PDRectangle(PDRectangle.LETTER.getHeight(), PDRectangle.LETTER.getWidth())
                    : PDRectangle.LETTER;
            this.anchoUtil = tamano.getWidth() - 2 * MARGEN;
            this.anchos = calcularAnchos(datos.getAnchos());
        }

        /** Reparte el ancho útil de la hoja según los anchos relativos. */
        private float[] calcularAnchos(float[] relativos) {
            float suma = 0;
            for (float r : relativos) {
                suma += r > 0 ? r : 1;
            }
            float[] reales = new float[relativos.length];
            for (int i = 0; i < relativos.length; i++) {
                reales[i] = anchoUtil * (relativos[i] > 0 ? relativos[i] : 1) / suma;
            }
            return reales;
        }

        void dibujar() throws IOException {
            try {
                nuevaPagina(true);
                if (datos.estaVacio()) {
                    escribir(SIN_DATOS, negrita, 11, MARGEN, y - 14, Color.BLACK);
                } else {
                    boolean alterna = false;
                    for (String[] fila : datos.getFilas()) {
                        dibujarFila(fila, alterna ? COLOR_ALTERNO : null, normal, false);
                        alterna = !alterna;
                    }
                    if (datos.getTotales() != null) {
                        dibujarFila(datos.getTotales(), COLOR_TOTALES, negrita, false);
                    }
                }
            } finally {
                if (contenido != null) {
                    contenido.close();
                }
            }
            dibujarPies();
        }

        // ------------------------------------------------------------ Páginas

        /** Agrega una página con encabezado, título y encabezado de la tabla. */
        private void nuevaPagina(boolean primera) throws IOException {
            if (contenido != null) {
                contenido.close();
            }
            PDPage pagina = new PDPage(tamano);
            documento.addPage(pagina);
            contenido = new PDPageContentStream(documento, pagina);
            y = tamano.getHeight() - MARGEN;

            // Encabezado institucional
            escribir(ENCABEZADO, normal, 9, MARGEN, y - 9, COLOR_TENUE);
            y -= 14;
            linea(MARGEN, y, tamano.getWidth() - MARGEN, y, COLOR_BORDE);
            y -= 8;

            // Título (y en la primera página, los filtros)
            String titulo = primera ? datos.getTitulo() : datos.getTitulo() + " (continuación)";
            for (String l : partirTexto(titulo, negrita, primera ? 15 : 11, anchoUtil)) {
                float tam = primera ? 15 : 11;
                escribir(l, negrita, tam, MARGEN, y - tam, Color.BLACK);
                y -= tam + 4;
            }
            if (primera && !datos.getSubtitulo().isBlank()) {
                for (String l : partirTexto(datos.getSubtitulo(), normal, 10, anchoUtil)) {
                    escribir(l, normal, 10, MARGEN, y - 10, COLOR_TENUE);
                    y -= 13;
                }
            }
            y -= 8;
            dibujarFila(datos.getColumnas(), COLOR_ENCABEZADO, negrita, true);
        }

        /** Escribe en cada página el pie: fecha, usuario y "Página X de Y". */
        private void dibujarPies() throws IOException {
            int total = documento.getNumberOfPages();
            UsuarioDTO usuario = Sesion.getUsuario();
            String izquierda = "Generado el " + LocalDateTime.now().format(FORMATO_FECHA)
                    + " por " + (usuario == null ? "—" : usuario.getNombreUsuario());

            for (int i = 0; i < total; i++) {
                PDPage pagina = documento.getPage(i);
                try (PDPageContentStream pie = new PDPageContentStream(documento, pagina,
                        PDPageContentStream.AppendMode.APPEND, true, true)) {
                    contenido = pie;
                    float base = MARGEN - 10;
                    linea(MARGEN, base + 12, tamano.getWidth() - MARGEN, base + 12, COLOR_BORDE);
                    escribir(izquierda, normal, 8, MARGEN, base, COLOR_TENUE);
                    String derecha = "Página " + (i + 1) + " de " + total;
                    float ancho = anchoTexto(derecha, normal, 8);
                    escribir(derecha, normal, 8, tamano.getWidth() - MARGEN - ancho, base, COLOR_TENUE);
                }
            }
            contenido = null;
        }

        // -------------------------------------------------------------- Tabla

        /**
         * Dibuja una fila de la tabla (fondo, bordes y texto ajustado). Si no
         * cabe en la página actual, pasa a una nueva.
         *
         * @param fondo        color de fondo, o null para dejarla en blanco
         * @param esEncabezado true para la fila de títulos (nunca provoca salto de página)
         */
        private void dibujarFila(String[] valores, Color fondo, PDType1Font fuente,
                                 boolean esEncabezado) throws IOException {
            List<List<String>> lineas = new ArrayList<>();
            int maxLineas = 1;
            for (int i = 0; i < valores.length; i++) {
                List<String> partes = partirTexto(valores[i], fuente, TAM_TABLA, anchos[i] - 2 * RELLENO);
                lineas.add(partes);
                maxLineas = Math.max(maxLineas, partes.size());
            }
            float alto = maxLineas * INTERLINEA + 2 * RELLENO;

            if (!esEncabezado && y - alto < MARGEN + ALTO_PIE) {
                nuevaPagina(false);
            }

            float x = MARGEN;
            if (fondo != null) {
                contenido.setNonStrokingColor(fondo);
                contenido.addRect(MARGEN, y - alto, anchoUtil, alto);
                contenido.fill();
            }
            for (int i = 0; i < valores.length; i++) {
                contenido.setStrokingColor(COLOR_BORDE);
                contenido.setLineWidth(0.5f);
                contenido.addRect(x, y - alto, anchos[i], alto);
                contenido.stroke();

                float yTexto = y - RELLENO - TAM_TABLA + 1;
                boolean derecha = !esEncabezado && datos.isAlineadaDerecha(i);
                for (String l : lineas.get(i)) {
                    float xTexto = derecha
                            ? x + anchos[i] - RELLENO - anchoTexto(l, fuente, TAM_TABLA)
                            : x + RELLENO;
                    escribir(l, fuente, TAM_TABLA, xTexto, yTexto, Color.BLACK);
                    yTexto -= INTERLINEA;
                }
                x += anchos[i];
            }
            y -= alto;
        }

        // ------------------------------------------------------------- Texto

        private void escribir(String texto, PDType1Font fuente, float tam, float x, float yBase,
                              Color color) throws IOException {
            if (texto.isEmpty()) {
                return;
            }
            contenido.beginText();
            contenido.setFont(fuente, tam);
            contenido.setNonStrokingColor(color);
            contenido.newLineAtOffset(x, yBase);
            contenido.showText(limpiar(texto, fuente));
            contenido.endText();
        }

        private void linea(float x1, float y1, float x2, float y2, Color color) throws IOException {
            contenido.setStrokingColor(color);
            contenido.setLineWidth(0.5f);
            contenido.moveTo(x1, y1);
            contenido.lineTo(x2, y2);
            contenido.stroke();
        }

        private float anchoTexto(String texto, PDType1Font fuente, float tam) throws IOException {
            return fuente.getStringWidth(limpiar(texto, fuente)) / 1000f * tam;
        }

        /**
         * Parte un texto en líneas que quepan en el ancho dado: corta por
         * palabras y, si una palabra sola no cabe, la corta por letras.
         */
        private List<String> partirTexto(String texto, PDType1Font fuente, float tam, float ancho)
                throws IOException {
            List<String> lineas = new ArrayList<>();
            String limpio = limpiar(texto == null ? "" : texto, fuente).trim();
            if (limpio.isEmpty()) {
                lineas.add("");
                return lineas;
            }
            StringBuilder actual = new StringBuilder();
            for (String palabra : limpio.split(" +")) {
                String prueba = actual.isEmpty() ? palabra : actual + " " + palabra;
                if (anchoTexto(prueba, fuente, tam) <= ancho) {
                    actual.setLength(0);
                    actual.append(prueba);
                    continue;
                }
                if (!actual.isEmpty()) {
                    lineas.add(actual.toString());
                    actual.setLength(0);
                }
                // La palabra sola no cabe: se corta letra por letra.
                for (char c : palabra.toCharArray()) {
                    if (!actual.isEmpty() && anchoTexto(actual.toString() + c, fuente, tam) > ancho) {
                        lineas.add(actual.toString());
                        actual.setLength(0);
                    }
                    actual.append(c);
                }
            }
            if (!actual.isEmpty()) {
                lineas.add(actual.toString());
            }
            return lineas;
        }

        /**
         * Quita saltos de línea y tabulaciones y cambia por "?" los caracteres
         * que la fuente no puede dibujar (p. ej. emojis), para que PDFBox no falle.
         */
        private static String limpiar(String texto, PDType1Font fuente) {
            StringBuilder sb = new StringBuilder(texto.length());
            for (int i = 0; i < texto.length(); i++) {
                char c = texto.charAt(i);
                if (Character.isWhitespace(c)) {
                    sb.append(' ');
                    continue;
                }
                try {
                    fuente.encode(String.valueOf(c));
                    sb.append(c);
                } catch (IllegalArgumentException | IOException e) {
                    sb.append('?');
                }
            }
            return sb.toString();
        }
    }
}
