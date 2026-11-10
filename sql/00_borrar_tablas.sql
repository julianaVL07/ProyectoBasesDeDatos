-- =====================================================================
-- Borra TODAS las tablas del proyecto (y sus datos) para recrearlas.
-- Orden: primero las hijas (con llaves foráneas), luego las padres.
-- Uso: 00_borrar_tablas.sql -> 01_crear_tablas.sql -> 02_datos_iniciales.sql
-- Si una tabla no existe, Oracle muestra ORA-00942: se puede ignorar.
-- =====================================================================

DROP TABLE TBitacora     PURGE;
DROP TABLE TUsuario      PURGE;
DROP TABLE TRol          PURGE;
DROP TABLE TSuministro   PURGE;
DROP TABLE TProducto     PURGE;
DROP TABLE TProveedor    PURGE;
DROP TABLE TCategoria    PURGE;
DROP TABLE TCiudad       PURGE;
DROP TABLE TDepartamento PURGE;
