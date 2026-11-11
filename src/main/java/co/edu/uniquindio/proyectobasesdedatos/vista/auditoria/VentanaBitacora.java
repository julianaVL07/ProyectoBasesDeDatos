package co.edu.uniquindio.proyectobasesdedatos.vista.auditoria;

import co.edu.uniquindio.proyectobasesdedatos.controlador.BitacoraControlador;
import co.edu.uniquindio.proyectobasesdedatos.controlador.ControladorException;
import co.edu.uniquindio.proyectobasesdedatos.controlador.UsuarioControlador;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.BitacoraDTO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.UsuarioDTO;
import co.edu.uniquindio.proyectobasesdedatos.vista.util.UtilVista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Ventana interna de la Bitácora de entradas y salidas (módulo Auditoría,
 * solo Administrador).
 *
 * <p>Filtra por rango de fechas de ingreso (dos {@link JSpinner} de fecha) y
 * por usuario (combo con la opción "Todos"). Si un registro no tiene salida
 * se muestra "Sesión abierta".</p>
 */
public class VentanaBitacora extends JInternalFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String SESION_ABIERTA = "Sesión abierta";
    /** Al abrir la ventana se muestran los ingresos de los últimos días. */
    private static final int DIAS_POR_DEFECTO = 7;

    private final BitacoraControlador controlador = new BitacoraControlador();
    private final UsuarioControlador usuarioControlador = new UsuarioControlador();

    private final JSpinner spnDesde = crearSpinnerFecha();
    private final JSpinner spnHasta = crearSpinnerFecha();
    private final JComboBox<OpcionUsuario> cmbUsuario = new JComboBox<>();
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnHoy = new JButton("Hoy");

    private final JTable tabla = new JTable();
    private final DefaultTableModel modelo = UtilVista.crearModeloNoEditable(
            "Usuario", "Nombre completo", "Fecha ingreso", "Hora ingreso",
            "Fecha salida", "Hora salida");
    private final JLabel lblTotal = new JLabel("Registros: 0");

    public VentanaBitacora() {
        super("Bitácora de entradas y salidas", true, true, true, true);
        setSize(860, 520);

        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(crearPanelFiltros(), BorderLayout.NORTH);
        getContentPane().add(crearPanelTabla(), BorderLayout.CENTER);
        UtilVista.configurarTabla(tabla, modelo);

        LocalDate hoy = LocalDate.now();
        spnDesde.setValue(aDate(hoy.minusDays(DIAS_POR_DEFECTO)));
        spnHasta.setValue(aDate(hoy));

        btnBuscar.addActionListener(e -> buscar());
        btnHoy.addActionListener(e -> {
            spnDesde.setValue(aDate(LocalDate.now()));
            spnHasta.setValue(aDate(LocalDate.now()));
            buscar();
        });

        cargarUsuarios();
        buscar();
    }

    // ----------------------------------------------------------------- Paneles

    private JPanel crearPanelFiltros() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 8, 0, 8),
                BorderFactory.createTitledBorder("Filtros")));
        panel.add(new JLabel("Desde:"));
        panel.add(spnDesde);
        panel.add(new JLabel("Hasta:"));
        panel.add(spnHasta);
        panel.add(new JLabel("Usuario:"));
        panel.add(cmbUsuario);
        panel.add(btnBuscar);
        panel.add(btnHoy);
        return panel;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(lblTotal, BorderLayout.SOUTH);
        return panel;
    }

    /** Spinner de fecha (solo día) con formato dd/MM/yyyy. */
    private static JSpinner crearSpinnerFecha() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        return spinner;
    }

    // ---------------------------------------------------------------- Acciones

    /** Llena el combo con "Todos" y la lista de usuarios. */
    private void cargarUsuarios() {
        cmbUsuario.addItem(new OpcionUsuario(null, "Todos"));
        try {
            for (UsuarioDTO u : usuarioControlador.listar()) {
                cmbUsuario.addItem(new OpcionUsuario(u.getCodigo(),
                        u.getNombreUsuario() + " — " + u.getNombreCompleto()));
            }
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        }
    }

    private void buscar() {
        LocalDate desde = aLocalDate((Date) spnDesde.getValue());
        LocalDate hasta = aLocalDate((Date) spnHasta.getValue());
        OpcionUsuario opcion = (OpcionUsuario) cmbUsuario.getSelectedItem();
        Integer codUsuario = opcion == null ? null : opcion.codigo();

        try {
            List<BitacoraDTO> registros = controlador.listar(desde, hasta, codUsuario);
            UtilVista.limpiarTabla(modelo);
            for (BitacoraDTO b : registros) {
                modelo.addRow(new Object[]{
                        b.getNombreUsuario(),
                        b.getNombreCompleto(),
                        formatear(b.getFechaIngreso()),
                        b.getHoraIngreso(),
                        b.isSesionAbierta() ? SESION_ABIERTA : formatear(b.getFechaSalida()),
                        b.isSesionAbierta() ? SESION_ABIERTA : b.getHoraSalida()
                });
            }
            lblTotal.setText("Registros: " + registros.size());
        } catch (ControladorException e) {
            UtilVista.mostrarError(this, e.getMessage());
        }
    }

    // ------------------------------------------------------- Apoyo interno

    private static String formatear(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    private static LocalDate aLocalDate(Date fecha) {
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date aDate(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** Opción del combo de usuarios: código null significa "Todos". */
    private record OpcionUsuario(Integer codigo, String texto) {
        @Override
        public String toString() {
            return texto;
        }
    }
}
