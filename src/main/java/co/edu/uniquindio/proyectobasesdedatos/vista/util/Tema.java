package co.edu.uniquindio.proyectobasesdedatos.vista.util;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.plaf.basic.BasicMenuBarUI;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;

/**
 * Paleta pastel de la aplicación (la misma de los diagramas draw.io del
 * proyecto) y el método {@link #aplicar(Container)}, que la aplica a una
 * ventana. Solo cambia apariencia: no toca textos, tamaños ni layouts. La
 * fuente de toda la interfaz se cambia aparte con {@link #aplicarFuente()}.
 *
 * <p>Cada módulo tiene su color (barra de título y fondo claro): Entidades
 * rosa, Transacciones durazno, Consultas/Reportes verde, Auditoría azul y
 * Login/menú principal lila.</p>
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

    // ----------------------------------------------------------------- Módulos

    /** Colores de un módulo: el de la barra de título, su borde y el fondo muy claro. */
    private record Modulo(Color base, Color borde, Color fondo) {
    }

    private static final Modulo ENTIDADES = new Modulo(ROSA, ROSA_BORDE, mezclar(ROSA, Color.WHITE, 0.65));
    private static final Modulo TRANSACCIONES = new Modulo(DURAZNO, DURAZNO_BORDE, mezclar(DURAZNO, Color.WHITE, 0.6));
    private static final Modulo CONSULTAS = new Modulo(VERDE, VERDE_BORDE, mezclar(VERDE, Color.WHITE, 0.6));
    private static final Modulo AUDITORIA = new Modulo(AZUL, AZUL_BORDE, mezclar(AZUL, Color.WHITE, 0.55));
    private static final Modulo PRINCIPAL = new Modulo(LILA, LILA_BORDE, FONDO);

    /** El módulo sale del paquete de la ventana (vista.entidades, vista.auditoria, ...). */
    private static Modulo moduloDe(Object ventana) {
        String paquete = ventana.getClass().getPackageName();
        if (paquete.endsWith(".entidades")) {
            return ENTIDADES;
        } else if (paquete.endsWith(".transacciones")) {
            return TRANSACCIONES;
        } else if (paquete.endsWith(".consultas") || paquete.endsWith(".reportes")) {
            return CONSULTAS;
        } else if (paquete.endsWith(".auditoria")) {
            return AUDITORIA;
        }
        return PRINCIPAL; // Login y menú principal
    }

    // ------------------------------------------------------------- Recorrido

    /**
     * Aplica los colores del tema a una ventana (JFrame, JDialog o
     * JInternalFrame) o a cualquier contenedor. Se puede llamar varias veces
     * sobre la misma ventana.
     */
    public static void aplicar(Container raiz) {
        if (raiz == null) {
            return;
        }
        if (raiz instanceof JInternalFrame ventana) {
            Modulo modulo = moduloDe(ventana);
            colorearVentanaInterna(ventana, modulo);
            recorrer(ventana.getContentPane(), modulo.fondo());
        } else if (raiz instanceof RootPaneContainer ventana) {
            Modulo modulo = moduloDe(ventana);
            JMenuBar barra = ventana.getRootPane().getJMenuBar();
            if (barra != null) {
                colorearBarraMenu(barra);
            }
            recorrer(ventana.getContentPane(), modulo.fondo());
        } else {
            recorrer(raiz, FONDO);
        }
    }

    /** Recorre recursivamente los componentes y solo cambia colores. */
    private static void recorrer(Container raiz, Color fondo) {
        if (raiz == null) {
            return;
        }
        if (raiz instanceof JInternalFrame interna) {
            aplicar(interna); // cada ventana interna usa el color de su módulo
            return;
        }
        if (raiz instanceof JComponent c) {
            colorearTitulo(c.getBorder());
        }

        // Texto de solo lectura usado como etiqueta (p. ej. la descripción de Consultas).
        if (raiz instanceof JTextArea area && !area.isEditable()) {
            area.setBackground(fondo);
            return;
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
            panel.setBackground(ROL_BARRA_ESTADO.equals(panel.getClientProperty(ROL)) ? AZUL : fondo);
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
            colorearPestanas(pestanas, fondo);
        } else if (raiz instanceof JButton boton && !(boton instanceof BasicArrowButton)
                && !(boton instanceof UIResource)) {
            colorearBoton(boton);
            return;
        }

        for (Component hijo : raiz.getComponents()) {
            if (hijo instanceof Container contenedor) {
                recorrer(contenedor, fondo);
            }
        }
    }

    // -------------------------------------------------------- Ventanas internas

    /**
     * Barra de título del color del módulo y texto oscuro. Windows pinta su
     * propia barra e ignora los colores, por eso se cambia SOLO el UI de la
     * ventana por la versión Basic; los botones de minimizar/maximizar/cerrar
     * son los mismos de Swing y funcionan igual.
     */
    private static void colorearVentanaInterna(JInternalFrame ventana, Modulo modulo) {
        ventana.putClientProperty(TITULO_COLOR, modulo.base());
        if (!(ventana.getUI() instanceof VentanaInternaUI)) {
            ventana.setUI(new VentanaInternaUI(ventana));
        }
        ventana.setBackground(modulo.fondo());
        ventana.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(modulo.borde()),
                BorderFactory.createMatteBorder(3, 3, 3, 3, modulo.base())));
    }

    /** Color de la barra de título, guardado en la ventana (la barra lo lee al crearse). */
    private static final String TITULO_COLOR = "tema.tituloColor";

    private static final class VentanaInternaUI extends BasicInternalFrameUI {

        VentanaInternaUI(JInternalFrame ventana) {
            super(ventana);
        }

        @Override
        protected JComponent createNorthPane(JInternalFrame ventana) {
            return new BarraTitulo(ventana);
        }
    }

    private static final class BarraTitulo extends BasicInternalFrameTitlePane {

        BarraTitulo(JInternalFrame ventana) {
            super(ventana);
        }

        @Override
        protected void installDefaults() {
            super.installDefaults();
            Color base = frame.getClientProperty(TITULO_COLOR) instanceof Color c ? c : LILA;
            selectedTitleColor = base;
            notSelectedTitleColor = mezclar(base, Color.WHITE, 0.45);
            selectedTextColor = TEXTO_OSCURO;
            notSelectedTextColor = TEXTO_OSCURO;
        }
    }

    // --------------------------------------------------------------- Pestañas

    private static final Color[] PESTANA_BASE = {ROSA, VERDE, AZUL, LILA, DURAZNO};
    private static final Color[] PESTANA_BORDE = {ROSA_BORDE, VERDE_BORDE, AZUL_BORDE, LILA_BORDE, DURAZNO_BORDE};

    /** Cada pestaña de un color de la paleta; Windows los ignora, por eso se usa la versión Basic. */
    private static void colorearPestanas(JTabbedPane pestanas, Color fondo) {
        if (!(pestanas.getUI() instanceof PestanasUI)) {
            pestanas.setUI(new PestanasUI());
        }
        pestanas.setBackground(fondo);
        pestanas.setForeground(TEXTO_OSCURO);
    }

    private static final class PestanasUI extends BasicTabbedPaneUI {

        @Override
        protected void paintTabBackground(Graphics g, int ubicacion, int indice,
                                          int x, int y, int ancho, int alto, boolean seleccionada) {
            int i = indice % PESTANA_BASE.length;
            Color color = seleccionada ? mezclar(PESTANA_BASE[i], PESTANA_BORDE[i], 0.3) : PESTANA_BASE[i];
            g.setColor(color);
            g.fillRect(x + 1, y + 1, ancho - 2, alto - 1);
        }

        @Override
        protected void paintTabBorder(Graphics g, int ubicacion, int indice,
                                      int x, int y, int ancho, int alto, boolean seleccionada) {
            if (ubicacion != TOP) {
                super.paintTabBorder(g, ubicacion, indice, x, y, ancho, alto, seleccionada);
                return;
            }
            g.setColor(PESTANA_BORDE[indice % PESTANA_BORDE.length]);
            g.drawLine(x, y, x, y + alto - 1);                       // izquierda
            g.drawLine(x, y, x + ancho - 1, y);                      // arriba
            g.drawLine(x + ancho - 1, y, x + ancho - 1, y + alto - 1); // derecha
        }
    }

    // ------------------------------------------------------------ Barra de menús

    /**
     * Barra de menús lila y desplegables claros con selección azul. Windows
     * ignora esos colores, por eso se cambia SOLO el UI de la barra, de los
     * desplegables y de sus opciones por la versión Basic.
     */
    private static void colorearBarraMenu(JMenuBar barra) {
        if (!(barra.getUI() instanceof BarraMenuUI)) {
            barra.setUI(new BarraMenuUI());
        }
        barra.setOpaque(true);
        barra.setBackground(LILA);
        for (int i = 0; i < barra.getMenuCount(); i++) {
            JMenu menu = barra.getMenu(i);
            if (menu == null) {
                continue;
            }
            menu.setOpaque(false); // deja ver el lila de la barra
            menu.setForeground(TEXTO_OSCURO);
            JPopupMenu desplegable = menu.getPopupMenu();
            if (!(desplegable.getUI() instanceof DesplegableUI)) {
                desplegable.setUI(new DesplegableUI());
            }
            desplegable.setBackground(FONDO);
            desplegable.setBorder(BorderFactory.createLineBorder(LILA_BORDE));
            for (Component c : desplegable.getComponents()) {
                if (c instanceof JMenuItem item && !(item instanceof JMenu)) {
                    if (!(item.getUI() instanceof OpcionMenuUI)) {
                        item.setUI(new OpcionMenuUI());
                    }
                    item.setOpaque(true);
                    item.setBackground(FONDO);
                    item.setForeground(TEXTO_OSCURO);
                } else if (c instanceof JSeparator separador) {
                    separador.setBackground(FONDO);
                    separador.setForeground(LILA_BORDE);
                }
            }
        }
    }

    private static final class BarraMenuUI extends BasicMenuBarUI {
    }

    private static final class DesplegableUI extends BasicPopupMenuUI {
    }

    /** Opción de menú con selección azul y texto oscuro. */
    private static final class OpcionMenuUI extends BasicMenuItemUI {

        @Override
        protected void installDefaults() {
            super.installDefaults();
            selectionBackground = AZUL;
            selectionForeground = TEXTO_OSCURO;
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
        instalarFilasAlternas(tabla);
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

    // ------------------------------------------------------------ Filas alternas

    /** Fila impar: lila muy claro (la par queda blanca). */
    public static final Color FILA_ALTERNA = FONDO;
    private static final String FILAS_LISTAS = "tema.filasAlternas";
    private static final Class<?>[] CLASES_RENDERER = {
            Object.class, Number.class, Float.class, Double.class, java.util.Date.class,
            javax.swing.Icon.class, javax.swing.ImageIcon.class, Boolean.class};

    /**
     * Pone filas alternas blanco / lila muy claro envolviendo el renderer que
     * ya tiene cada columna, así se conservan sus formatos (precios, alineación,
     * etc.). También envuelve los renderers que se pongan después (p. ej.
     * Consultas crea columnas y renderers nuevos en cada ejecución).
     */
    private static void instalarFilasAlternas(JTable tabla) {
        if (tabla.getClientProperty(FILAS_LISTAS) != null) {
            return;
        }
        tabla.putClientProperty(FILAS_LISTAS, Boolean.TRUE);

        for (Class<?> clase : CLASES_RENDERER) {
            TableCellRenderer actual = tabla.getDefaultRenderer(clase);
            if (actual != null && !(actual instanceof RenderizadorAlterno)) {
                tabla.setDefaultRenderer(clase, new RenderizadorAlterno(actual));
            }
        }

        TableColumnModelListener oyente = new TableColumnModelAdapter() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {
                TableColumnModel modelo = (TableColumnModel) e.getSource();
                envolverColumna(modelo.getColumn(e.getToIndex()));
            }
        };
        TableColumnModel columnas = tabla.getColumnModel();
        columnas.addColumnModelListener(oyente);
        for (int i = 0; i < columnas.getColumnCount(); i++) {
            envolverColumna(columnas.getColumn(i));
        }
        // Si la tabla cambia de modelo de columnas, se vigila el nuevo.
        tabla.addPropertyChangeListener("columnModel", e -> {
            if (e.getOldValue() instanceof TableColumnModel viejo) {
                viejo.removeColumnModelListener(oyente);
            }
            if (e.getNewValue() instanceof TableColumnModel nuevo) {
                nuevo.addColumnModelListener(oyente);
                for (int i = 0; i < nuevo.getColumnCount(); i++) {
                    envolverColumna(nuevo.getColumn(i));
                }
            }
        });
    }

    /** Envuelve el renderer propio de la columna (si tiene) y el que se le ponga después. */
    private static void envolverColumna(TableColumn columna) {
        TableCellRenderer actual = columna.getCellRenderer();
        if (actual != null && !(actual instanceof RenderizadorAlterno)) {
            columna.setCellRenderer(new RenderizadorAlterno(actual));
        }
        for (java.beans.PropertyChangeListener l : columna.getPropertyChangeListeners()) {
            if (l instanceof OyenteRendererColumna) {
                return;
            }
        }
        columna.addPropertyChangeListener(new OyenteRendererColumna(columna));
    }

    private record OyenteRendererColumna(TableColumn columna) implements java.beans.PropertyChangeListener {
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if ("cellRenderer".equals(e.getPropertyName())
                    && e.getNewValue() instanceof TableCellRenderer nuevo
                    && !(nuevo instanceof RenderizadorAlterno)) {
                columna.setCellRenderer(new RenderizadorAlterno(nuevo));
            }
        }
    }

    /**
     * Pide la celda al renderer original y solo cambia el fondo de las filas no
     * seleccionadas. Si el renderer original eligió un fondo propio (distinto al
     * de la tabla), lo respeta.
     */
    private static final class RenderizadorAlterno implements TableCellRenderer {

        private final TableCellRenderer original;

        RenderizadorAlterno(TableCellRenderer original) {
            this.original = original;
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            Component c = original.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
            if (!seleccionada && esFondoNormal(c.getBackground(), tabla)) {
                c.setBackground(fila % 2 == 0 ? Color.WHITE : FILA_ALTERNA);
            }
            return c;
        }

        /**
         * DefaultTableCellRenderer recuerda el último fondo que se le puso, por eso
         * también se aceptan los dos colores de las filas alternas como "normales".
         */
        private static boolean esFondoNormal(Color fondo, JTable tabla) {
            return fondo != null && (fondo.equals(tabla.getBackground())
                    || fondo.equals(Color.WHITE) || fondo.equals(FILA_ALTERNA));
        }
    }

    /** Adaptador vacío de TableColumnModelListener (Swing no trae uno). */
    private abstract static class TableColumnModelAdapter implements TableColumnModelListener {
        @Override
        public void columnAdded(TableColumnModelEvent e) {
        }

        @Override
        public void columnRemoved(TableColumnModelEvent e) {
        }

        @Override
        public void columnMoved(TableColumnModelEvent e) {
        }

        @Override
        public void columnMarginChanged(javax.swing.event.ChangeEvent e) {
        }

        @Override
        public void columnSelectionChanged(javax.swing.event.ListSelectionEvent e) {
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

    // ------------------------------------------------------------------ Fuente

    /** Familia de letra de la interfaz. */
    public static final String FAMILIA_FUENTE = "Segoe UI";

    /**
     * Cambia la familia de todas las fuentes de UIManager (claves *.font) a
     * Segoe UI, conservando tamaño y estilo. Se llama una vez al iniciar,
     * después de aplicar el Look and Feel. Si Segoe UI no está instalada no hace
     * nada, y nunca cambia las fuentes monoespaciadas.
     */
    public static void aplicarFuente() {
        boolean existe = java.util.Arrays.asList(java.awt.GraphicsEnvironment
                .getLocalGraphicsEnvironment().getAvailableFontFamilyNames()).contains(FAMILIA_FUENTE);
        if (!existe) {
            return;
        }
        java.util.List<Object> claves = new java.util.ArrayList<>(
                java.util.Collections.list(UIManager.getLookAndFeelDefaults().keys()));
        for (Object clave : claves) {
            if (!(clave instanceof String nombre) || !nombre.endsWith(".font")) {
                continue;
            }
            if (UIManager.get(clave) instanceof Font fuente && !esMonoespaciada(fuente)) {
                UIManager.put(clave, new FontUIResource(FAMILIA_FUENTE, fuente.getStyle(), fuente.getSize()));
            }
        }
    }

    private static boolean esMonoespaciada(Font fuente) {
        String familia = (fuente.getFamily() + " " + fuente.getName()).toLowerCase();
        return familia.contains("monospaced") || familia.contains("mono")
                || familia.contains("courier") || familia.contains("consolas")
                || familia.contains("lucida console");
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
