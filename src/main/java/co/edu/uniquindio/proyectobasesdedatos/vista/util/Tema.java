package co.edu.uniquindio.proyectobasesdedatos.vista.util;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
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
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;

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
    /** Botón deshabilitado: gris neutro para que se note que no está disponible. */
    public static final Color DESHABILITADO = new Color(0xF0F0F0);
    public static final Color DESHABILITADO_BORDE = new Color(0xC8C8C8);

    /** Propiedad de cliente para marcar componentes con un rol especial del tema. */
    private static final String ROL = "tema.rol";
    private static final String ROL_BARRA_ESTADO = "barraEstado";
    /** Marca de los botones ya configurados (para no repetir listeners). */
    private static final String BOTON_LISTO = "tema.botonListo";

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
     * eso se apaga el relleno del Look and Feel y se pinta el fondo opaco con un
     * borde de línea. El borde nuevo deja los mismos márgenes, así el tamaño no
     * cambia. Si el botón se deshabilita o cambia de texto, se vuelve a pintar.
     */
    private static void colorearBoton(JButton boton) {
        if (boton.getClientProperty(BOTON_LISTO) == null) {
            boton.putClientProperty(BOTON_LISTO, boton.getInsets());
            boton.setOpaque(true);
            boton.setContentAreaFilled(false);
            boton.setFocusPainted(false);
            boton.addPropertyChangeListener("enabled", e -> pintarBoton(boton));
            boton.addPropertyChangeListener(AbstractButton.TEXT_CHANGED_PROPERTY, e -> pintarBoton(boton));
        }
        pintarBoton(boton);
    }

    private static void pintarBoton(JButton boton) {
        Color fondo;
        Color borde;
        if (!boton.isEnabled()) {
            fondo = DESHABILITADO;
            borde = DESHABILITADO_BORDE;
        } else {
            String texto = boton.getText() == null ? "" : boton.getText().trim().toLowerCase();
            if (texto.startsWith("eliminar") || texto.startsWith("quitar") || texto.startsWith("desactivar")) {
                fondo = ROSA;
                borde = ROSA_BORDE;
            } else if (texto.startsWith("limpiar") || texto.startsWith("cancelar") || texto.equals("hoy")) {
                fondo = LILA;
                borde = LILA_BORDE;
            } else {
                fondo = VERDE;
                borde = VERDE_BORDE;
            }
        }
        Insets m = (Insets) boton.getClientProperty(BOTON_LISTO);
        boton.setBackground(fondo);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borde),
                BorderFactory.createEmptyBorder(Math.max(0, m.top - 1), Math.max(0, m.left - 1),
                        Math.max(0, m.bottom - 1), Math.max(0, m.right - 1))));
    }
}
