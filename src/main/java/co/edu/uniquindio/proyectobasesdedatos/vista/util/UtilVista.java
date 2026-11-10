package co.edu.uniquindio.proyectobasesdedatos.vista.util;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.Dimension;

/**
 * Métodos de apoyo que comparten todas las ventanas (tablas, ventanas internas
 * y mensajes). Solo contiene código de interfaz: nada de SQL.
 */
public final class UtilVista {

    // Clase de utilidad: no se instancia.
    private UtilVista() {
    }

    // ---------------------------------------------------------------- Tablas

    /**
     * Crea un modelo de tabla cuyas celdas NO se pueden editar con doble clic
     * (las tablas de la app solo muestran datos; se edita en el formulario).
     *
     * @param columnas títulos de las columnas
     * @return modelo vacío, listo para {@code addRow}
     */
    public static DefaultTableModel crearModeloNoEditable(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    /**
     * Configura una tabla con el modelo dado: selección de una sola fila y sin
     * permitir reordenar columnas.
     */
    public static void configurarTabla(JTable tabla, DefaultTableModel modelo) {
        tabla.setModel(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setFillsViewportHeight(true);
    }

    /** Borra todas las filas de la tabla (deja las columnas). */
    public static void limpiarTabla(DefaultTableModel modelo) {
        modelo.setRowCount(0);
    }

    // ------------------------------------------------------ Ventanas internas

    /**
     * Ubica la ventana interna en el centro del escritorio. Si es más grande
     * que el escritorio, la deja en la esquina superior izquierda.
     */
    public static void centrar(JInternalFrame ventana, JDesktopPane escritorio) {
        Dimension e = escritorio.getSize();
        Dimension v = ventana.getSize();
        int x = Math.max(0, (e.width - v.width) / 2);
        int y = Math.max(0, (e.height - v.height) / 2);
        ventana.setLocation(x, y);
    }

    // --------------------------------------------------------------- Mensajes

    /** Muestra un mensaje de error. */
    public static void mostrarError(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Muestra un mensaje informativo. */
    public static void mostrarInfo(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Pide confirmación con botones Sí / No.
     *
     * @return true si el usuario eligió "Sí"
     */
    public static boolean confirmar(Component padre, String mensaje) {
        int opcion = JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return opcion == JOptionPane.YES_OPTION;
    }
}
