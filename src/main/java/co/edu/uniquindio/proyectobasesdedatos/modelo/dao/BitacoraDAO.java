package co.edu.uniquindio.proyectobasesdedatos.modelo.dao;

import co.edu.uniquindio.proyectobasesdedatos.config.ConexionOracle;
import co.edu.uniquindio.proyectobasesdedatos.modelo.dto.BitacoraDTO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la tabla TBitacora: registra la entrada y la salida de los usuarios y
 * permite consultarlas con filtros.
 *
 * <p>La fecha y la hora las pone siempre Oracle ({@code SYSDATE}), no el PC del
 * usuario, para que todos los registros usen el mismo reloj.</p>
 */
public class BitacoraDAO {

    // ---------------------------------------------------------------- INSERT

    /**
     * Registra el ingreso de un usuario. Solo se envía UCodigo: BFechaIngreso y
     * BHoraIngreso toman sus DEFAULT (fecha y hora actuales de Oracle) y la
     * salida queda en NULL.
     *
     * @param codUsuario UCodigo del usuario que ingresó
     * @return BCodigo generado (se guarda en la Sesion para registrar la salida)
     * @throws SQLException si no se pudo insertar o no se obtuvo el código
     */
    public int registrarIngreso(int codUsuario) throws SQLException {
        String sql = "INSERT INTO TBitacora (UCodigo) VALUES (?)";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"BCODIGO"})) {

            ps.setInt(1, codUsuario);
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
        }
        throw new SQLException("No se obtuvo el código del registro de bitácora.");
    }

    // ---------------------------------------------------------------- UPDATE

    /**
     * Registra la salida (fecha y hora actuales de Oracle) de un ingreso que
     * siga abierto. Si la salida ya estaba registrada no cambia nada, así que es
     * seguro llamarlo más de una vez.
     *
     * @param codBitacora BCodigo del ingreso
     * @return true si se registró la salida; false si ya estaba cerrada o no existe
     */
    public boolean registrarSalida(int codBitacora) throws SQLException {
        String sql = "UPDATE TBitacora SET "
                + "BFechaSalida = TRUNC(SYSDATE), "
                + "BHoraSalida = TO_CHAR(SYSDATE, 'HH24:MI:SS') "
                + "WHERE BCodigo = ? AND BFechaSalida IS NULL";

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, codBitacora);
            return ps.executeUpdate() > 0;
        }
    }

    // ---------------------------------------------------------------- SELECT

    /**
     * Lista los registros de la bitácora, del más reciente al más antiguo. Los
     * tres filtros son opcionales (null = sin filtro) y se agregan al WHERE
     * solo si vienen, siempre como parámetros "?".
     *
     * @param desde      fecha de ingreso mínima (inclusive) o null
     * @param hasta      fecha de ingreso máxima (inclusive) o null
     * @param codUsuario UCodigo del usuario o null para todos
     * @return lista (vacía si no hay registros, nunca null)
     */
    public List<BitacoraDTO> listar(LocalDate desde, LocalDate hasta, Integer codUsuario)
            throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();

        if (desde != null) {
            sql.append(" AND b.BFechaIngreso >= ?");
            parametros.add(Date.valueOf(desde));
        }
        if (hasta != null) {
            sql.append(" AND b.BFechaIngreso <= ?");
            parametros.add(Date.valueOf(hasta));
        }
        if (codUsuario != null) {
            sql.append(" AND b.UCodigo = ?");
            parametros.add(codUsuario);
        }
        sql.append(" ORDER BY b.BFechaIngreso DESC, b.BHoraIngreso DESC, b.BCodigo DESC");

        List<BitacoraDTO> lista = new ArrayList<>();

        try (Connection con = ConexionOracle.getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    // ------------------------------------------------------- Apoyo interno

    /** SELECT común: trae el registro y los nombres del usuario (JOIN con TUsuario). */
    private static final String SELECT_BASE =
            "SELECT b.BCodigo, b.BFechaIngreso, b.BHoraIngreso, b.BFechaSalida, "
            + "b.BHoraSalida, b.UCodigo, u.UNombreUsuario, u.UNombreCompleto "
            + "FROM TBitacora b "
            + "JOIN TUsuario u ON u.UCodigo = b.UCodigo";

    /** Convierte la fila actual del ResultSet en un BitacoraDTO. */
    private BitacoraDTO mapear(ResultSet rs) throws SQLException {
        BitacoraDTO b = new BitacoraDTO();
        b.setCodigo(rs.getInt("BCodigo"));
        b.setFechaIngreso(aLocalDate(rs.getDate("BFechaIngreso")));
        b.setHoraIngreso(rs.getString("BHoraIngreso"));
        b.setFechaSalida(aLocalDate(rs.getDate("BFechaSalida")));
        b.setHoraSalida(rs.getString("BHoraSalida"));
        b.setCodigoUsuario(rs.getInt("UCodigo"));
        b.setNombreUsuario(rs.getString("UNombreUsuario"));
        b.setNombreCompleto(rs.getString("UNombreCompleto"));
        return b;
    }

    /** Convierte un java.sql.Date que puede ser null. */
    private static LocalDate aLocalDate(Date fecha) {
        return fecha != null ? fecha.toLocalDate() : null;
    }
}
