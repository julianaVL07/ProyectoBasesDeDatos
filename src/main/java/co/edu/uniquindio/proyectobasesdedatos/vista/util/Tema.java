package co.edu.uniquindio.proyectobasesdedatos.vista.util;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.RootPaneContainer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;

/**
 * Paleta pastel de la aplicación (la misma de los diagramas draw.io del
 * proyecto) y el método {@link #aplicar(Container)}, que la aplica a una
 * ventana. Solo cambia colores: no toca textos, tamaños, layouts ni fuentes.
 */
public final class Tema {

    // ------------------------------------------------------------------ Paleta

    public static final Color ROSA = new Color(0xF8CECC);
    public static final Color ROSA_BORDE = new Color(0xB85450);
    public static final Color LILA = new Color(0xE1D5E7);
    public static final Color LILA_BORDE = new Color(0x9673A6);
    public static final Color AZUL = new Color(0xDAE8FC);
    public static final Color AZUL_BORDE = new Color(0x6C8EBF);
    public static final Color VERDE = new Color(0xD5E8D4);
    public static final Color VERDE_BORDE = new Color(0x82B366);
    public static final Color DURAZNO = new Color(0xFFE6CC);
    public static final Color DURAZNO_BORDE = new Color(0xD79B00);

    /** Fondo general de paneles: lila muy claro. */
    public static final Color FONDO = new Color(0xF7F2FA);
    /** Texto oscuro para encabezados y selección. */
    public static final Color TEXTO = Color.BLACK;
    /** Texto de botones y títulos de ventana. */
    public static final Color TEXTO_OSCURO = new Color(0x333333);
    /** Botón deshabilitado: gris claro con texto gris para que se note que no está disponible. */
    public static final Color DESHABILITADO = new Color(0xEEEEEE);
    public static final Color DESHABILITADO_BORDE = new Color(0xC8C8C8);
    public static final Color TEXTO_DESHABILITADO = new Color(0x9A9A9A);

    /** Propiedad de cliente para marcar componentes con un rol especial del tema. */
    private static final String ROL = "tema.rol";
    private static final String ROL_BARRA_ESTADO = "barraEstado";
    /** Marca de los botones ya configurados (para no repetir listeners). */
    private static final String BOTON_LISTO = "tema.botonListo";
    /** Color de borde del botón (se usa para oscurecer el fondo al pasar el mouse). */
    private static final String BOTON_BORDE = "tema.botonBorde";

    private Tema() {
    }

    /** Marca un panel como barra de estado del menú principal (se pinta azul). */
    public static void marcarBarraEstado(JComponent barra) {
        barra.putClientProperty(ROL, ROL_BARRA_ESTADO);
    }

    /**
     * Recorre recursivamente los componentes de {@code raiz} y les aplica los
     * colores del tema. Se puede llamar varias veces sobre la misma ventana.
     */
    public static void aplicar(Container raiz) {
        if (raiz == null) {
            return;
        }
        if (raiz instanceof RootPaneContainer ventana) {
            aplicar(ventana.getContentPane());
            return;
        }
        if (raiz instanceof JComponent c) {
            colorearTitulo(c.getBorder());
        }

        // Componentes que no se tocan (ni sus hijos internos, como flechas o editores).
        if (raiz instanceof JTextComponent || raiz instanceof JComboBox
                || raiz instanceof JSpinner || raiz instanceof JScrollBar
                || raiz instanceof JOptionPane || raiz instanceof BasicInternalFrameTitlePane) {
            return;
        }

        if (raiz instanceof JDesktopPane escritorio) {
            escritorio.setBackground(LILA);
        } else if (raiz instanceof JPanel panel) {
            panel.setBackground(ROL_BARRA_ESTADO.equals(panel.getClientProperty(ROL)) ? AZUL : FONDO);
        } else if (raiz instanceof JTable tabla) {
            colorearTabla(tabla);
            return;
        } else if (raiz instanceof JScrollPane scroll) {
            JViewport viewport = scroll.getViewport();
            // Si la ventana ya eligió un fondo propio (p. ej. el visor de PDF), se respeta.
            if (viewport != null && viewport.getBackground() instanceof UIResource) {
                viewport.setBackground(Color.WHITE);
            }
        } else if (raiz instanceof JTabbedPane pestanas) {
            pestanas.setBackground(FONDO);
        } else if (raiz instanceof JButton boton && !(boton instanceof BasicArrowButton)
                && !(boton instanceof UIResource)) {
            colorearBoton(boton);
            return;
        }

        for (Component hijo : raiz.getComponents()) {
            if (hijo instanceof Container contenedor) {
                aplicar(contenedor);
            }
        }
    }

    // ------------------------------------------------------------------ Bordes

    /** Cambia el color del título de un TitledBorder (también dentro de bordes compuestos). */
    private static void colorearTitulo(Border borde) {
        if (borde instanceof TitledBorder titulo) {
            titulo.setTitleColor(LILA_BORDE);
            colorearTitulo(titulo.getBorder());
        } else if (borde instanceof CompoundBorder compuesto) {
            colorearTitulo(compuesto.getOutsideBorder());
            colorearTitulo(compuesto.getInsideBorder());
        }
    }

    // ------------------------------------------------------------------ Tablas

    private static void colorearTabla(JTable tabla) {
        tabla.setSelectionBackground(AZUL);
        tabla.setSelectionForeground(TEXTO);
        tabla.setGridColor(LILA);
        JTableHeader encabezado = tabla.getTableHeader();
        if (encabezado != null) {
            encabezado.setBackground(ROSA);
            encabezado.setForeground(TEXTO);
            TableCellRenderer actual = encabezado.getDefaultRenderer();
            if (!(actual instanceof RenderizadorEncabezado)) {
                encabezado.setDefaultRenderer(new RenderizadorEncabezado(actual));
            }
        }
    }

    /**
     * Envuelve el renderer del encabezado: el Look and Feel de Windows pinta su
     * propio fondo e ignora setBackground, así que se copian texto, ícono,
     * alineación y fuente del renderer original a una etiqueta con fondo rosa.
     * Los márgenes se conservan para que el alto y los anchos no cambien.
     */
    private static final class RenderizadorEncabezado implements TableCellRenderer {

        private final TableCellRenderer original;
        private final JLabel etiqueta = new JLabel();

        RenderizadorEncabezado(TableCellRenderer original) {
            this.original = original;
            etiqueta.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            Component c = original.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
            if (!(c instanceof JLabel base)) {
                c.setBackground(ROSA);
                c.setForeground(TEXTO);
                return c;
            }
            etiqueta.setText(base.getText());
            etiqueta.setIcon(base.getIcon());
            etiqueta.setFont(base.getFont());
            etiqueta.setHorizontalAlignment(base.getHorizontalAlignment());
            etiqueta.setHorizontalTextPosition(base.getHorizontalTextPosition());
            etiqueta.setVerticalAlignment(base.getVerticalAlignment());
            etiqueta.setIconTextGap(base.getIconTextGap());
            etiqueta.setToolTipText(base.getToolTipText());
            etiqueta.setEnabled(base.isEnabled());
            Insets m = base.getInsets();
            etiqueta.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, ROSA_BORDE),
                    BorderFactory.createEmptyBorder(m.top, m.left,
                            Math.max(0, m.bottom - 1), Math.max(0, m.right - 1))));
            etiqueta.setBackground(ROSA);
            etiqueta.setForeground(TEXTO);
            return etiqueta;
        }
    }

    // ----------------------------------------------------------------- Botones

    /**
     * Pinta el botón según su texto. Windows ignora el fondo de los botones, por
     * eso se cambia SOLO el UI del botón por {@link BotonUI} (basado en
     * BasicButtonUI), que rellena el fondo y lo oscurece al pasar el mouse. El
     * borde nuevo deja los mismos márgenes que tenía, así el tamaño no cambia.
     * Si el botón se deshabilita o cambia de texto, se vuelve a pintar.
     */
    private static void colorearBoton(JButton boton) {
        if (boton.getClientProperty(BOTON_LISTO) == null) {
            // Márgenes del borde original, medidos antes de cambiar el UI.
            boton.putClientProperty(BOTON_LISTO, boton.getInsets());
            boton.setUI(BOTON_UI);
            boton.setOpaque(true);
            boton.setContentAreaFilled(true);
            boton.setRolloverEnabled(true);
            boton.setFocusPainted(false);
            boton.setForeground(TEXTO_OSCURO);
            boton.addPropertyChangeListener("enabled", e -> pintarBoton(boton));
            boton.addPropertyChangeListener(AbstractButton.TEXT_CHANGED_PROPERTY, e -> pintarBoton(boton));
        }
        pintarBoton(boton);
    }

    private static void pintarBoton(JButton boton) {
        Color fondo;
        Color borde;
        String texto = boton.getText() == null ? "" : boton.getText().trim().toLowerCase();
        switch (texto) {
            case "eliminar", "quitar", "desactivar" -> {
                fondo = ROSA;
                borde = ROSA_BORDE;
            }
            case "limpiar", "cancelar", "hoy", "recargar", "actualizar listas" -> {
                fondo = LILA;
                borde = LILA_BORDE;
            }
            case "buscar", "ejecutar" -> {
                fondo = AZUL;
                borde = AZUL_BORDE;
            }
            default -> {
                fondo = VERDE;
                borde = VERDE_BORDE;
            }
        }
        boton.putClientProperty(BOTON_BORDE, borde);
        boton.setBackground(fondo);
        Insets m = (Insets) boton.getClientProperty(BOTON_LISTO);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(boton.isEnabled() ? borde : DESHABILITADO_BORDE),
                BorderFactory.createEmptyBorder(Math.max(0, m.top - 1), Math.max(0, m.left - 1),
                        Math.max(0, m.bottom - 1), Math.max(0, m.right - 1))));
        boton.repaint();
    }

    private static final BotonUI BOTON_UI = new BotonUI();

    /**
     * UI de botón que respeta los colores: rellena con el fondo del botón, un
     * tono más oscuro al pasar el mouse o presionarlo, y gris si está
     * deshabilitado (con el texto también gris).
     */
    private static final class BotonUI extends BasicButtonUI {

        @Override
        public void update(Graphics g, JComponent c) {
            paint(g, c);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel modelo = b.getModel();
            Color base = b.getBackground();
            Color borde = b.getClientProperty(BOTON_BORDE) instanceof Color cb ? cb : base.darker();
            Color fondo;
            if (!b.isEnabled()) {
                fondo = DESHABILITADO;
            } else if (modelo.isPressed() && modelo.isArmed()) {
                fondo = mezclar(base, borde, 0.35);
            } else if (modelo.isRollover()) {
                fondo = mezclar(base, borde, 0.18);
            } else {
                fondo = base;
            }
            g.setColor(fondo);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            super.paint(g, c);
        }

        /** El fondo de "presionado" ya lo pinta {@link #paint}. */
        @Override
        protected void paintButtonPressed(Graphics g, AbstractButton b) {
        }

        @Override
        protected void paintText(Graphics g, AbstractButton b, Rectangle r, String texto) {
            FontMetrics fm = b.getFontMetrics(b.getFont());
            int desplazamiento = getTextShiftOffset();
            g.setColor(b.isEnabled() ? b.getForeground() : TEXTO_DESHABILITADO);
            BasicGraphicsUtils.drawStringUnderlineCharAt(b, (Graphics2D) g, texto,
                    b.getDisplayedMnemonicIndex(), r.x + desplazamiento, r.y + fm.getAscent() + desplazamiento);
        }
    }

    // --------------------------------------------------------------- Utilidades

    /** Mezcla dos colores: {@code proporcion} 0 = solo {@code a}, 1 = solo {@code b}. */
    static Color mezclar(Color a, Color b, double proporcion) {
        double p = Math.max(0, Math.min(1, proporcion));
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * p),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * p),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * p));
    }
}
