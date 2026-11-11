package co.edu.uniquindio.proyectobasesdedatos.controlador;

import co.edu.uniquindio.proyectobasesdedatos.modelo.dao.ProveedorDAO;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.ProveedorDTO;
import co.edu.uniquindio.proyectobasesdedatos.util.ErroresBD;
import co.edu.uniquindio.proyectobasesdedatos.util.Sesion;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Controlador del CRUD de Proveedores.
 *
 * <p>Valida los datos (obligatorios, longitudes del SQL, formato de NIT, correo
 * y teléfono) antes de llamar al {@link ProveedorDAO} y convierte los errores de
 * Oracle en {@link ControladorException} con mensajes amigables. Las operaciones
 * de escritura exigen {@link Sesion#puedeEditar()}.</p>
 */
public class ProveedorControlador {

    // Longitudes máximas según sql/01_crear_tablas.sql
    private static final int NIT_MAX = 20;
    private static final int NOMBRE_MAX = 100;
    private static final int DIRECCION_MAX = 120;
    private static final int CORREO_MAX = 100;
    private static final int TELEFONO_MAX = 20;

    /** NIT colombiano: solo dígitos y, opcional, guion con dígito de verificación (900123456-7). */
    private static final Pattern PATRON_NIT = Pattern.compile("\\d{5,15}(-\\d)?");

    /** Correo simple: algo@dominio.ext */
    private static final Pattern PATRON_CORREO =
            Pattern.compile("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+");

    /** Teléfono: dígitos con espacios, guiones, paréntesis o "+" opcionales. */
    private static final Pattern PATRON_TELEFONO = Pattern.compile("\\+?[0-9 ()-]+");

    /** Mínimo de dígitos que debe tener un teléfono (fijo o celular). */
    private static final int TELEFONO_MIN_DIGITOS = 7;

    /** Código de Oracle: la llave foránea apunta a un registro que no existe. */
    private static final int ORA_PADRE_NO_EXISTE = 2291;

    /** Código de Oracle: se repite un dato único (aquí el único posible es el NIT). */
    private static final int ORA_DATO_UNICO = 1;

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    // ---------------------------------------------------------------- Lectura

    /** @return todos los proveedores ordenados por nombre */
    public List<ProveedorDTO> listar() throws ControladorException {
        verificarConsulta();
        try {
            return proveedorDAO.listar();
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    /**
     * Busca por texto en el nombre o el NIT. Si el texto está vacío lista todo.
     */
    public List<ProveedorDTO> buscar(String texto) throws ControladorException {
        verificarConsulta();
        try {
            if (texto == null || texto.isBlank()) {
                return proveedorDAO.listar();
            }
            return proveedorDAO.buscarPorTexto(texto);
        } catch (SQLException e) {
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    // ---------------------------------------------------------------- Escritura

    /**
     * Inserta un proveedor nuevo; al terminar el DTO trae el código generado.
     *
     * @throws ControladorException si los datos no son válidos o falla la BD
     */
    public void guardar(ProveedorDTO p) throws ControladorException {
        verificarEdicion();
        validar(p);
        try {
            proveedorDAO.insertar(p);
        } catch (SQLException e) {
            throw new ControladorException(traducir(e), e);
        }
    }

    /**
     * Actualiza todos los datos de un proveedor existente.
     *
     * @throws ControladorException si no hay proveedor seleccionado, los datos
     *                              no son válidos o falla la BD
     */
    public void actualizar(ProveedorDTO p) throws ControladorException {
        verificarEdicion();
        verificarSeleccion(p == null ? 0 : p.getCodigo());
        validar(p);
        try {
            if (!proveedorDAO.actualizar(p)) {
                throw new ControladorException("El proveedor ya no existe.");
            }
        } catch (SQLException e) {
            throw new ControladorException(traducir(e), e);
        }
    }

    /**
     * Elimina un proveedor. Si tiene suministros, Oracle lo impide (ORA-02292).
     *
     * @param codigo PCodigo del proveedor
     */
    public void eliminar(int codigo) throws ControladorException {
        verificarEdicion();
        verificarSeleccion(codigo);
        try {
            if (!proveedorDAO.eliminar(codigo)) {
                throw new ControladorException("El proveedor ya no existe.");
            }
        } catch (SQLException e) {
            if (ErroresBD.tieneRegistrosAsociados(e)) {
                throw new ControladorException(
                        "No se puede eliminar el proveedor porque tiene suministros asociados.", e);
            }
            throw new ControladorException(ErroresBD.traducir(e), e);
        }
    }

    // ------------------------------------------------------- Apoyo interno

    private void verificarConsulta() throws ControladorException {
        if (!Sesion.puedeConsultar()) {
            throw new ControladorException("Debe iniciar sesión.");
        }
    }

    private void verificarEdicion() throws ControladorException {
        if (!Sesion.puedeEditar()) {
            throw new ControladorException("Su rol no tiene permiso para modificar proveedores.");
        }
    }

    private void verificarSeleccion(int codigo) throws ControladorException {
        if (codigo <= 0) {
            throw new ControladorException("Seleccione un proveedor de la tabla.");
        }
    }

    /**
     * Valida obligatorios, longitudes y formatos. Además quita espacios
     * sobrantes y deja en null los campos opcionales vacíos.
     */
    private void validar(ProveedorDTO p) throws ControladorException {
        if (p == null) {
            throw new ControladorException("No hay datos del proveedor.");
        }

        String nit = limpiar(p.getNit());
        if (nit == null) {
            throw new ControladorException("Ingrese el NIT del proveedor.");
        }
        validarLongitud(nit, NIT_MAX, "El NIT");
        if (!PATRON_NIT.matcher(nit).matches()) {
            throw new ControladorException(
                    "El NIT solo puede tener números y, opcionalmente, el dígito de verificación (ej.: 900123456-7).");
        }

        String nombre = limpiar(p.getNombre());
        if (nombre == null) {
            throw new ControladorException("Ingrese el nombre del proveedor.");
        }
        validarLongitud(nombre, NOMBRE_MAX, "El nombre");

        String direccion = limpiar(p.getDireccion());
        if (direccion != null) {
            validarLongitud(direccion, DIRECCION_MAX, "La dirección");
        }

        String correo = limpiar(p.getCorreo());
        if (correo != null) {
            validarLongitud(correo, CORREO_MAX, "El correo");
            if (!PATRON_CORREO.matcher(correo).matches()) {
                throw new ControladorException("El correo no tiene un formato válido (ej.: ventas@empresa.com).");
            }
        }

        String telefono = limpiar(p.getTelefono());
        if (telefono != null) {
            validarLongitud(telefono, TELEFONO_MAX, "El teléfono");
            long digitos = telefono.chars().filter(Character::isDigit).count();
            if (!PATRON_TELEFONO.matcher(telefono).matches() || digitos < TELEFONO_MIN_DIGITOS) {
                throw new ControladorException("El teléfono debe tener al menos " + TELEFONO_MIN_DIGITOS
                        + " dígitos y solo puede usar espacios, guiones, paréntesis o \"+\".");
            }
        }

        if (p.getCodigoCiudad() <= 0) {
            throw new ControladorException("Seleccione la ciudad del proveedor.");
        }

        p.setNit(nit);
        p.setNombre(nombre);
        p.setDireccion(direccion);
        p.setCorreo(correo);
        p.setTelefono(telefono);
    }

    /** @return el texto sin espacios a los lados, o null si queda vacío */
    private static String limpiar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    private static void validarLongitud(String valor, int maximo, String campo) throws ControladorException {
        if (valor.length() > maximo) {
            throw new ControladorException(campo + " no puede superar " + maximo + " caracteres.");
        }
    }

    /** Mensajes específicos para NIT repetido y ciudad borrada mientras tanto. */
    private String traducir(SQLException e) {
        if (e.getErrorCode() == ORA_DATO_UNICO) {
            return "Ya existe un proveedor con ese NIT.";
        }
        if (e.getErrorCode() == ORA_PADRE_NO_EXISTE) {
            return "La ciudad seleccionada ya no existe. Recargue la ventana.";
        }
        return ErroresBD.traducir(e);
    }
}
