package co.edu.uniquindio.proyectobasesdedatos.vista.reportes;

import co.edu.uniquindio.proyectobasesdedatos.vista.util.UtilVista;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Ventana interna que muestra un PDF dentro de la aplicación.
 *
 * <p>Cada página se dibuja como imagen con {@link PDFRenderer} de PDFBox y se
 * muestra en un {@link JScrollPane}. Tiene botones para cambiar de página,
 * acercar/alejar y abrir el archivo en el visor de PDF del sistema.</p>
 *
 * <p>El documento queda abierto mientras la ventana está visible y se cierra
 * al cerrar la ventana.</p>
 */
public class VisorPDF extends JInternalFrame {

    private static final float ZOOM_MIN = 0.5f;
    private static final float ZOOM_MAX = 3.0f;
    private static final float ZOOM_PASO = 0.25f;

    private final File archivo;
    private final PDDocument documento;
    private final PDFRenderer renderer;

    private final JLabel lblPagina = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblImagen = new JLabel("", SwingConstants.CENTER);
    private final JLabel lblZoom = new JLabel(" ");
    private final JButton btnAnterior = new JButton("◀ Anterior");
    private final JButton btnSiguiente = new JButton("Siguiente ▶");
    private final JButton btnAlejar = new JButton("−");
    private final JButton btnAcercar = new JButton("+");
    private final JButton btnAbrir = new JButton("Abrir en el sistema");

    private int pagina;              // índice de la página visible (0 = primera)
    private float zoom = 1.25f;      // 1.0 = tamaño real (72 puntos por pulgada)

    /**
     * Abre el PDF y muestra la primera página.
     *
     * @throws IOException si el archivo no existe o no es un PDF válido
     */
    public VisorPDF(File archivo) throws IOException {
        super("Vista previa — " + archivo.getName(), true, true, true, true);
        this.archivo = archivo;
        this.documento = Loader.loadPDF(archivo);
        this.renderer = new PDFRenderer(documento);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(crearBarra(), BorderLayout.NORTH);

        lblImagen.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scroll = new JScrollPane(lblImagen);
        scroll.getViewport().setBackground(new Color(128, 128, 128));
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getHorizontalScrollBar().setUnitIncrement(20);
        getContentPane().add(scroll, BorderLayout.CENTER);

        btnAnterior.addActionListener(e -> irA(pagina - 1));
        btnSiguiente.addActionListener(e -> irA(pagina + 1));
        btnAlejar.addActionListener(e -> cambiarZoom(-ZOOM_PASO));
        btnAcercar.addActionListener(e -> cambiarZoom(ZOOM_PASO));
        btnAbrir.addActionListener(e -> abrirEnSistema());
        btnAlejar.setToolTipText("Alejar");
        btnAcercar.setToolTipText("Acercar");

        // Libera el archivo al cerrar la ventana (si no, Windows lo deja bloqueado).
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                cerrarDocumento();
            }
        });

        setSize(850, 650);
        mostrarPagina();
    }

    private JPanel crearBarra() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        barra.add(btnAnterior);
        barra.add(lblPagina);
        barra.add(btnSiguiente);
        barra.add(new JLabel("   Zoom:"));
        barra.add(btnAlejar);
        barra.add(lblZoom);
        barra.add(btnAcercar);
        barra.add(new JLabel("   "));
        barra.add(btnAbrir);
        return barra;
    }

    // ---------------------------------------------------------------- Acciones

    private void irA(int nuevaPagina) {
        if (nuevaPagina >= 0 && nuevaPagina < documento.getNumberOfPages()) {
            pagina = nuevaPagina;
            mostrarPagina();
        }
    }

    private void cambiarZoom(float cambio) {
        zoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, zoom + cambio));
        mostrarPagina();
    }

    /** Dibuja la página actual con el zoom actual y actualiza los botones. */
    private void mostrarPagina() {
        int total = documento.getNumberOfPages();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            BufferedImage imagen = renderer.renderImage(pagina, zoom);
            lblImagen.setIcon(new ImageIcon(imagen));
        } catch (IOException e) {
            lblImagen.setIcon(null);
            UtilVista.mostrarError(this, "No se pudo mostrar la página " + (pagina + 1) + ".");
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
        lblPagina.setText("Página " + (pagina + 1) + " de " + total);
        lblZoom.setText(Math.round(zoom * 100) + " %");
        btnAnterior.setEnabled(pagina > 0);
        btnSiguiente.setEnabled(pagina < total - 1);
        btnAlejar.setEnabled(zoom > ZOOM_MIN);
        btnAcercar.setEnabled(zoom < ZOOM_MAX);
    }

    /** Abre el PDF con el programa predeterminado del sistema operativo. */
    private void abrirEnSistema() {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            UtilVista.mostrarError(this, "Este sistema no permite abrir archivos desde la aplicación.\n"
                    + "El reporte está guardado en: " + archivo.getAbsolutePath());
            return;
        }
        try {
            Desktop.getDesktop().open(archivo);
        } catch (IOException | IllegalArgumentException e) {
            UtilVista.mostrarError(this, "No se pudo abrir el archivo. Verifique que tenga un "
                    + "visor de PDF instalado.\nRuta: " + archivo.getAbsolutePath());
        }
    }

    private void cerrarDocumento() {
        try {
            documento.close();
        } catch (IOException ignorada) {
            // Solo se estaba liberando el archivo; no hay nada más que hacer.
        }
    }
}
