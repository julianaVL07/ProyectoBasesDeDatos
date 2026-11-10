-- =====================================================================
-- Datos de PRUEBA para consultas, reportes y roles.
--
-- Orden de ejecución (con el usuario del proyecto, en SQL Developer):
--   00_borrar_tablas.sql   (opcional, para empezar de cero)
--   01_crear_tablas.sql
--   02_datos_iniciales.sql (roles, admin y ubicaciones base)
--   03_datos_prueba.sql    (este archivo)
--
-- Credenciales para probar la aplicación:
--   Usuario       Clave       Rol
--   admin         Admin123*   ADMINISTRADOR  (creado en 02_datos_iniciales.sql)
--   tradicional   Trad123*    TRADICIONAL
--   esporadico    Espo123*    ESPORADICO
--
-- Notas:
--   * No se usan códigos fijos (salvo los roles): cada INSERT busca las
--     llaves foráneas por nombre (INSERT ... SELECT), así el script funciona
--     sin importar qué códigos haya generado la IDENTITY.
--   * Los nombres de empresas son ficticios; las ciudades y departamentos son reales.
--   * Si se ejecuta dos veces sin borrar, Oracle mostrará ORA-00001 en los
--     datos únicos: hay que volver a correr 00 -> 01 -> 02 -> 03.
-- =====================================================================

-- ============================ UBICACIONES ===============================
-- 02_datos_iniciales.sql ya trae Quindío, Valle del Cauca y Antioquia con
-- Armenia, Calarcá, Cali y Medellín. Aquí se agregan 4 departamentos y 8 ciudades.

INSERT INTO TDepartamento (DNombre) VALUES ('Risaralda');
INSERT INTO TDepartamento (DNombre) VALUES ('Caldas');
INSERT INTO TDepartamento (DNombre) VALUES ('Santander');
INSERT INTO TDepartamento (DNombre) VALUES ('Cundinamarca');

INSERT INTO TCiudad (CNombre, DCodigo) SELECT 'Pereira',       DCodigo FROM TDepartamento WHERE DNombre = 'Risaralda';
INSERT INTO TCiudad (CNombre, DCodigo) SELECT 'Dosquebradas',  DCodigo FROM TDepartamento WHERE DNombre = 'Risaralda';
INSERT INTO TCiudad (CNombre, DCodigo) SELECT 'Manizales',     DCodigo FROM TDepartamento WHERE DNombre = 'Caldas';
INSERT INTO TCiudad (CNombre, DCodigo) SELECT 'Bucaramanga',   DCodigo FROM TDepartamento WHERE DNombre = 'Santander';
INSERT INTO TCiudad (CNombre, DCodigo) SELECT 'Floridablanca', DCodigo FROM TDepartamento WHERE DNombre = 'Santander';
INSERT INTO TCiudad (CNombre, DCodigo) SELECT 'Soacha',        DCodigo FROM TDepartamento WHERE DNombre = 'Cundinamarca';
INSERT INTO TCiudad (CNombre, DCodigo) SELECT 'Zipaquirá',     DCodigo FROM TDepartamento WHERE DNombre = 'Cundinamarca';
INSERT INTO TCiudad (CNombre, DCodigo) SELECT 'Palmira',       DCodigo FROM TDepartamento WHERE DNombre = 'Valle del Cauca';

-- ============================ CATEGORÍAS ================================

INSERT INTO TCategoria (CaNombre, CaDescripcion) VALUES ('Aseo',       'Productos de limpieza e higiene para oficinas y hogares');
INSERT INTO TCategoria (CaNombre, CaDescripcion) VALUES ('Papelería',  'Útiles de oficina, papel y elementos de archivo');
INSERT INTO TCategoria (CaNombre, CaDescripcion) VALUES ('Tecnología', 'Periféricos, accesorios y equipos de cómputo');
INSERT INTO TCategoria (CaNombre, CaDescripcion) VALUES ('Alimentos',  'Alimentos y bebidas para cafetería');
INSERT INTO TCategoria (CaNombre, CaDescripcion) VALUES ('Ferretería', 'Herramientas manuales y eléctricas');

-- ============================ PRODUCTOS =================================
-- 4 productos por categoría. En cada categoría hay uno bastante más caro que
-- el resto (queda sobre el promedio) y algunos productos tienen stock 0.

-- Aseo
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Detergente en polvo 3 kg', 'Detergente multiusos para ropa', 28900, 120, CaCodigo FROM TCategoria WHERE CaNombre = 'Aseo';
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Jabón líquido para manos 1 L', 'Jabón antibacterial con dosificador', 12500, 80, CaCodigo FROM TCategoria WHERE CaNombre = 'Aseo';
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Desinfectante multiusos 2 L', 'Desinfectante para pisos y superficies', 15900, 0, CaCodigo FROM TCategoria WHERE CaNombre = 'Aseo';
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Escoba de cerdas suaves', 'Escoba con mango metálico', 18500, 45, CaCodigo FROM TCategoria WHERE CaNombre = 'Aseo';

-- Papelería
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Resma de papel carta', 'Resma de 500 hojas, 75 g', 24900, 200, CaCodigo FROM TCategoria WHERE CaNombre = 'Papelería';
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Cuaderno argollado 100 hojas', 'Cuaderno cuadriculado tamaño carta', 9800, 150, CaCodigo FROM TCategoria WHERE CaNombre = 'Papelería';
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Caja de bolígrafos x12', 'Bolígrafos de tinta negra', 14500, 60, CaCodigo FROM TCategoria WHERE CaNombre = 'Papelería';
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Archivador AZ oficio', 'Archivador de palanca tamaño oficio', 11900, 0, CaCodigo FROM TCategoria WHERE CaNombre = 'Papelería';

-- Tecnología
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Mouse inalámbrico', 'Mouse óptico con receptor USB', 49900, 35, CaCodigo FROM TCategoria WHERE CaNombre = 'Tecnología';
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Teclado USB en español', 'Teclado alámbrico con ñ', 69900, 20, CaCodigo FROM TCategoria WHERE CaNombre = 'Tecnología';
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Monitor LED 24 pulgadas', 'Monitor Full HD con entrada HDMI', 689000, 8, CaCodigo FROM TCategoria WHERE CaNombre = 'Tecnología';
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Memoria USB 64 GB', 'Memoria USB 3.0', 39900, 0, CaCodigo FROM TCategoria WHERE CaNombre = 'Tecnología';

-- Alimentos
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Café molido 500 g', 'Café tostado y molido de origen colombiano', 21500, 90, CaCodigo FROM TCategoria WHERE CaNombre = 'Alimentos';
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Azúcar 1 kg', 'Azúcar blanca refinada', 4900, 300, CaCodigo FROM TCategoria WHERE CaNombre = 'Alimentos';
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Galletas surtidas x12', 'Paquete de galletas dulces surtidas', 13900, 75, CaCodigo FROM TCategoria WHERE CaNombre = 'Alimentos';
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Agua en botella x24', 'Paca de 24 botellas de 600 ml', 26900, 40, CaCodigo FROM TCategoria WHERE CaNombre = 'Alimentos';

-- Ferretería
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Martillo de uña 16 oz', 'Martillo con mango de fibra de vidrio', 32900, 25, CaCodigo FROM TCategoria WHERE CaNombre = 'Ferretería';
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Juego de destornilladores x6', 'Destornilladores de pala y estrella', 45900, 18, CaCodigo FROM TCategoria WHERE CaNombre = 'Ferretería';
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Cinta métrica 5 m', 'Flexómetro con freno', 19900, 30, CaCodigo FROM TCategoria WHERE CaNombre = 'Ferretería';
INSERT INTO TProducto (PrNombre, PrDescripcion, PrPrecioBase, PrStock, CaCodigo)
SELECT 'Taladro percutor 1/2 pulgada', 'Taladro eléctrico de 650 W', 289000, 6, CaCodigo FROM TCategoria WHERE CaNombre = 'Ferretería';

COMMIT;
