package co.edu.uniquindio.proyectobasesdedatos.vista.reportes;

import co.edu.uniquindio.proyectobasesdedatos.controlador.CategoriaControlador;
import co.edu.uniquindio.proyectobasesdedatos.controlador.CiudadControlador;
import co.edu.uniquindio.proyectobasesdedatos.controlador.ControladorException;
import co.edu.uniquindio.proyectobasesdedatos.controlador.DepartamentoControlador;
import co.edu.uniquindio.proyectobasesdedatos.controlador.ProveedorControlador;
import co.edu.uniquindio.proyectobasesdedatos.controlador.ReporteControlador;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.CategoriaDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.CiudadDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.DepartamentoDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.ProveedorDTO;
import co.edu.uniquindio.proyectobasesdedatos.vista.util.UtilVista;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana de Reportes: una pestaña por cada uno de los 5 reportes.
 *
 * <p>Flujo: el usuario elige los filtros → pulsa <b>Generar PDF</b> → escoge
 * dónde guardar (se sugiere un nombre con la fecha) → el
 * {@link ReporteControlador} genera el archivo → se muestra en un
 * {@link VisorPDF} dentro de la aplicación.</p>
 *
 * <p>Los combos opcionales empiezan con "Todos"/"Todas" (sin filtro). No hay
 * SQL aquí: todo pasa por los controladores.</p>
 */
public class VentanaReportes extends JInternalFrame {

    private static final String TODOS = "Todos";
    private static final String TODAS = "Todas";
    private static final DateTimeFormatter FORMATO_ARCHIVO = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm");

    /** Última carpeta usada, para que el selector de archivos la recuerde. */
    private static File ultimaCarpeta;

    private final ReporteControlador controlador = new ReporteControlador();
    private final DepartamentoControlador departamentoControlador = new DepartamentoControlador();
    private final CiudadControlador ciudadControlador = new CiudadControlador();
    private final CategoriaControlador categoriaControlador = new CategoriaControlador();
    private final ProveedorControlador proveedorControlador = new ProveedorControlador();

    // 1. Proveedores por ubicación
    private final JComboBox<Object> cmbDepartamento = new JComboBox<>();
    private final JComboBox<Object> cmbCiudad = new JComboBox<>();
    private List<CiudadDTO> ciudades = new ArrayList<>();

    // 2. Productos por precio y stock
    private final JTextField txtPrecioMin = new JTextField(12);
    private final JTextField txtPrecioMax = new JTextField(12);
    private final JTextField txtStockMin = new JTextField(8);
    private final JTextField txtStockMax = new JTextField(8);

    // 3. Productos de un proveedor
    private final JComboBox<ProveedorDTO> cmbProveedor = new JComboBox<>();

    // 4. Productos de una categoría
    private final JComboBox<CategoriaDTO> cmbCategoria = new JComboBox<>();

    // 5. Resumen por categoría
    private final JComboBox<Object> cmbCategoriaResumen = new JComboBox<>();

    public VentanaReportes() {
        super("Reportes", true, true, true, true);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Proveedores por ubicación", crearPestanaProveedores());
        pestanas.addTab("Productos por precio y stock", crearPestanaPrecioStock());
        pestanas.addTab("Productos de un proveedor", crearPestanaProveedor());
        pestanas.addTab("Productos de una categoría", crearPestanaCategoria());
        pestanas.addTab("Resumen por categoría", crearPestanaResumen());

        JButton btnActualizar = new JButton("Actualizar listas");
        btnActualizar.setToolTipText("Vuelve a cargar departamentos, ciudades, proveedores y categorías");
        btnActualizar.addActionListener(e -> cargarListas());
        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        abajo.add(btnActualizar);

        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(pestanas, BorderLayout.CENTER);
        getContentPane().add(abajo, BorderLayout.SOUTH);

        cmbDepartamento.addActionListener(e -> filtrarCiudades());
        cargarListas();
        setSize(720, 330);
    }

    // ------------------------------------------------------------- Pestañas

    private JPanel crearPestanaProveedores() {
        JPanel campos = new JPanel(new GridBagLayout());
        campos.add(new JLabel("Departamento:"), celda(0, 0));
        campos.add(cmbDepartamento, celda(1, 0));
        campos.add(new JLabel("Ciudad:"), celda(0, 1));
        campos.add(cmbCiudad, celda(1, 1));
        return pestana("Lista los proveedores con su NIT, ciudad, departamento, teléfono, correo y estado. "
                + "Elija primero el departamento para poder filtrar por ciudad.", campos, this::generarProveedores);
    }

    private JPanel crearPestanaPrecioStock() {
        JPanel campos = new JPanel(new GridBagLayout());
        campos.add(new JLabel("Precio base mínimo:"), celda(0, 0));
        campos.add(txtPrecioMin, celda(1, 0));
        campos.add(new JLabel("Precio base máximo:"), celda(2, 0));
        campos.add(txtPrecioMax, celda(3, 0));
        campos.add(new JLabel("Stock mínimo:"), celda(0, 1));
        campos.add(txtStockMin, celda(1, 1));
        campos.add(new JLabel("Stock máximo:"), celda(2, 1));
        campos.add(txtStockMax, celda(3, 1));
        return pestana("Productos cuyo precio base y stock están dentro de los rangos. "
                + "Deje un campo vacío para no limitar por ese valor.", campos, this::generarPrecioStock);
    }

    private JPanel crearPestanaProveedor() {
        JPanel campos = new JPanel(new GridBagLayout());
        campos.add(new JLabel("Proveedor:"), celda(0, 0));
        campos.add(cmbProveedor, celda(1, 0));
        return pestana("Productos que suministra el proveedor, con precio base, precio de suministro, "
                + "días de entrega y totales.", campos, this::generarProveedor);
    }

    private JPanel crearPestanaCategoria() {
        JPanel campos = new JPanel(new GridBagLayout());
        campos.add(new JLabel("Categoría:"), celda(0, 0));
        campos.add(cmbCategoria, celda(1, 0));
        return pestana("Productos de la categoría con descripción, precio base, stock y cuántos "
                + "proveedores los suministran.", campos, this::generarCategoria);
    }

    private JPanel crearPestanaResumen() {
        JPanel campos = new JPanel(new GridBagLayout());
        campos.add(new JLabel("Categoría:"), celda(0, 0));
        campos.add(cmbCategoriaResumen, celda(1, 0));
        return pestana("Por cada categoría: cantidad de productos, precio promedio, mínimo y máximo, "
                + "y stock total, con una fila de totales.", campos, this::generarResumen);
    }

    /** Arma una pestaña: descripción arriba, filtros en el centro y botón Generar PDF abajo. */
    private JPanel pestana(String descripcion, JPanel campos, Runnable accion) {
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblDescripcion = new JLabel("<html>" + descripcion + "</html>");
        panel.add(lblDescripcion, BorderLayout.NORTH);

        campos.setBorder(BorderFactory.createTitledBorder("Filtros"));
        panel.add(campos, BorderLayout.CENTER);

        JButton btnGenerar = new JButton("Generar PDF");
        btnGenerar.addActionListener(e -> accion.run());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botones.add(btnGenerar);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private static GridBagConstraints celda(int columna, int fila) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = columna;
        g.gridy = fila;
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        boolean esCampo = columna % 2 == 1;
        g.fill = esCampo ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        g.weightx = esCampo ? 1.0 : 0.0;
        return g;
    }

    // ---------------------------------------------------------------- Listas

    /** Carga (o recarga) todos los combos desde la base de datos. */
    private void cargarListas() {
        try {
            List<DepartamentoDTO> departamentos = departamentoControlador.listar();
            ciudades = ciudadControlador.listar();
            List<ProveedorDTO> proveedores = proveedorControlador.listar();
            List<CategoriaDTO> categorias = categoriaControlador.listar();

            cmbDepartamento.setModel(conOpcionTodos(TODOS, departamentos));
            filtrarCiudades();
            cmbProveedor.setModel(new DefaultComboBoxModel<>(proveedores.toArray(new ProveedorDTO[0])));
            cmbCategoria.setModel(new DefaultComboBoxModel<>(categorias.toArray(new CategoriaDTO[0])));
            cmbCategoriaResumen.setModel(conOpcionTodos(TODAS, categorias));
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        }
    }

    /** Modelo de combo con "Todos"/"Todas" de primero y luego los elementos. */
    private static DefaultComboBoxModel<Object> conOpcionTodos(String todos, List<?> elementos) {
        DefaultComboBoxModel<Object> modelo = new DefaultComboBoxModel<>();
        modelo.addElement(todos);
        for (Object o : elementos) {
            modelo.addElement(o);
        }
        return modelo;
    }

    /**
     * Deja en el combo de ciudades solo las del departamento elegido. Con
     * "Todos" los departamentos, la ciudad queda fija en "Todas".
     */
    private void filtrarCiudades() {
        DefaultComboBoxModel<Object> modelo = new DefaultComboBoxModel<>();
        modelo.addElement(TODAS);
        if (cmbDepartamento.getSelectedItem() instanceof DepartamentoDTO departamento) {
            for (CiudadDTO c : ciudades) {
                if (c.getCodigoDepartamento() == departamento.getCodigo()) {
                    modelo.addElement(c);
                }
            }
        }
        cmbCiudad.setModel(modelo);
        cmbCiudad.setEnabled(modelo.getSize() > 1);
    }

    // -------------------------------------------------------------- Generar

    private void generarProveedores() {
        Integer departamento = cmbDepartamento.getSelectedItem() instanceof DepartamentoDTO d ? d.getCodigo() : null;
        Integer ciudad = cmbCiudad.getSelectedItem() instanceof CiudadDTO c ? c.getCodigo() : null;
        generar("proveedores_por_ubicacion",
                destino -> controlador.proveedoresPorUbicacion(departamento, ciudad, destino));
    }

    private void generarPrecioStock() {
        BigDecimal precioMin;
        BigDecimal precioMax;
        Integer stockMin;
        Integer stockMax;
        try {
            precioMin = controlador.convertirPrecioFiltro(txtPrecioMin.getText(), "El precio mínimo");
            precioMax = controlador.convertirPrecioFiltro(txtPrecioMax.getText(), "El precio máximo");
            stockMin = controlador.convertirStockFiltro(txtStockMin.getText(), "El stock mínimo");
            stockMax = controlador.convertirStockFiltro(txtStockMax.getText(), "El stock máximo");
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
            return;
        }
        generar("productos_precio_stock",
                destino -> controlador.productosPorPrecioYStock(precioMin, precioMax, stockMin, stockMax, destino));
    }

    private void generarProveedor() {
        ProveedorDTO proveedor = (ProveedorDTO) cmbProveedor.getSelectedItem();
        if (proveedor == null) {
            UtilVista.mostrarError(this, "Seleccione un proveedor.");
            return;
        }
        generar("productos_proveedor_" + nombreArchivo(proveedor.getNombre()),
                destino -> controlador.productosDeProveedor(proveedor.getCodigo(), destino));
    }

    private void generarCategoria() {
        CategoriaDTO categoria = (CategoriaDTO) cmbCategoria.getSelectedItem();
        if (categoria == null) {
            UtilVista.mostrarError(this, "Seleccione una categoría.");
            return;
        }
        generar("productos_categoria_" + nombreArchivo(categoria.getNombre()),
                destino -> controlador.productosPorCategoria(categoria.getCodigo(), destino));
    }

    private void generarResumen() {
        Integer categoria = cmbCategoriaResumen.getSelectedItem() instanceof CategoriaDTO c ? c.getCodigo() : null;
        generar("resumen_categorias", destino -> controlador.resumenPorCategoria(categoria, destino));
    }

    /** Llamada al controlador que genera el PDF en el destino y devuelve el archivo final. */
    @FunctionalInterface
    private interface Generador {
        File generar(File destino) throws ControladorException;
    }

    /**
     * Pide dónde guardar, genera el reporte con el controlador y lo abre en el
     * visor interno.
     *
     * @param base inicio del nombre sugerido (se le agrega la fecha y hora)
     */
    private void generar(String base, Generador generador) {
        File destino = elegirDestino(base + "_" + LocalDateTime.now().format(FORMATO_ARCHIVO) + ".pdf");
        if (destino == null) {
            return; // el usuario canceló
        }
        File archivo;
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            archivo = generador.generar(destino);
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
            return;
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
        mostrarEnVisor(archivo);
    }

    /**
     * Muestra el selector de archivos con un nombre sugerido.
     *
     * @return el archivo elegido, o null si el usuario canceló
     */
    private File elegirDestino(String nombreSugerido) {
        JFileChooser selector = new JFileChooser(ultimaCarpeta);
        selector.setDialogTitle("Guardar reporte PDF");
        selector.setFileFilter(new FileNameExtensionFilter("Documentos PDF (*.pdf)", "pdf"));
        selector.setSelectedFile(new File(ultimaCarpeta, nombreSugerido));

        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File elegido = selector.getSelectedFile();
        ultimaCarpeta = elegido.getParentFile();

        File conExtension = elegido.getName().toLowerCase().endsWith(".pdf")
                ? elegido : new File(elegido.getParentFile(), elegido.getName() + ".pdf");
        if (conExtension.exists()
                && !UtilVista.confirmar(this, "El archivo «" + conExtension.getName() + "» ya existe. ¿Desea reemplazarlo?")) {
            return null;
        }
        return elegido;
    }

    /** Abre el PDF en un {@link VisorPDF} dentro del mismo escritorio de la aplicación. */
    private void mostrarEnVisor(File archivo) {
        JDesktopPane escritorio = getDesktopPane();
        if (escritorio == null) {
            UtilVista.mostrarInfo(this, "Reporte guardado en:\n" + archivo.getAbsolutePath());
            return;
        }
        try {
            VisorPDF visor = new VisorPDF(archivo);
            escritorio.add(visor);
            UtilVista.centrar(visor, escritorio);
            visor.setVisible(true);
            visor.setSelected(true);
        } catch (IOException e) {
            UtilVista.mostrarError(this, "El reporte se guardó en:\n" + archivo.getAbsolutePath()
                    + "\npero no se pudo mostrar en la aplicación.");
        } catch (PropertyVetoException ignorada) {
            // El visor ya está visible; no importa si no quedó seleccionado.
        }
    }

    /** Convierte un nombre en algo válido para un archivo: "Papelería Central Ltda." -> "Papeleria_Central_Ltda". */
    private static String nombreArchivo(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_|_$", "");
    }
}
