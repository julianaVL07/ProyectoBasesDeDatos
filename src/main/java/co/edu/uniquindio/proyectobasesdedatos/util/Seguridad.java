package co.edu.uniquindio.proyectobasesdedatos.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Utilidades de seguridad para las contraseñas de los usuarios.
 *
 * <p>Las contraseñas NUNCA se guardan en texto plano: en la columna
 * {@code TUsuario.UContrasena} se guarda su hash SHA-256 en hexadecimal
 * minúscula (64 caracteres). Es el mismo valor que produce Oracle con
 * {@code LOWER(RAWTOHEX(STANDARD_HASH('clave', 'SHA256')))}, que se usa en
 * {@code sql/02_datos_iniciales.sql} para el administrador inicial.</p>
 */
public final class Seguridad {

    private Seguridad() {
    }

    /**
     * Calcula el hash SHA-256 de una contraseña.
     *
     * @param clave contraseña en texto plano (no null)
     * @return hash en hexadecimal minúscula de 64 caracteres
     */
    public static String hashear(String clave) {
        if (clave == null) {
            throw new IllegalArgumentException("La contraseña no puede ser null");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(clave.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash); // minúsculas
        } catch (NoSuchAlgorithmException e) {
            // Todo JDK está obligado a traer SHA-256: esto no debería pasar.
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    /**
     * Verifica si una contraseña escrita coincide con el hash guardado.
     *
     * @param claveEscrita  lo que el usuario digitó en el login
     * @param hashGuardado  valor de {@code UContrasena} en la BD
     * @return true si coinciden
     */
    public static boolean verificar(String claveEscrita, String hashGuardado) {
        if (claveEscrita == null || hashGuardado == null) {
            return false;
        }
        // Comparación en tiempo constante (buena práctica para hashes).
        return MessageDigest.isEqual(
                hashear(claveEscrita).getBytes(StandardCharsets.US_ASCII),
                hashGuardado.trim().toLowerCase().getBytes(StandardCharsets.US_ASCII));
    }
}
