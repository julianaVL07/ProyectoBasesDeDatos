-- =====================================================================
-- Datos mínimos para probar el CRUD de Proveedor.
-- Ejecutar DESPUÉS de 01_crear_tablas.sql
-- =====================================================================

-- Los 3 tipos de usuario que pide el enunciado
INSERT INTO TRol (RNombre, RDescripcion) VALUES ('ADMINISTRADOR', 'Único usuario que crea nuevos usuarios');
INSERT INTO TRol (RNombre, RDescripcion) VALUES ('TRADICIONAL',   'Ejecuta los CRUD de gestión de datos');
INSERT INTO TRol (RNombre, RDescripcion) VALUES ('ESPORADICO',    'Solo ejecuta consultas');

-- Ubicaciones de ejemplo
INSERT INTO TDepartamento (DNombre) VALUES ('Quindío');
INSERT INTO TDepartamento (DNombre) VALUES ('Valle del Cauca');
INSERT INTO TDepartamento (DNombre) VALUES ('Antioquia');

INSERT INTO TCiudad (CNombre, DCodigo) SELECT 'Armenia',  DCodigo FROM TDepartamento WHERE DNombre = 'Quindío';
INSERT INTO TCiudad (CNombre, DCodigo) SELECT 'Calarcá',  DCodigo FROM TDepartamento WHERE DNombre = 'Quindío';
INSERT INTO TCiudad (CNombre, DCodigo) SELECT 'Cali',     DCodigo FROM TDepartamento WHERE DNombre = 'Valle del Cauca';
INSERT INTO TCiudad (CNombre, DCodigo) SELECT 'Medellín', DCodigo FROM TDepartamento WHERE DNombre = 'Antioquia';

COMMIT;
