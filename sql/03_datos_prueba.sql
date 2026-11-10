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

-- ============================ PROVEEDORES ===============================
-- 9 proveedores en 8 departamentos. La ciudad se busca por nombre Y
-- departamento (los nombres de ciudad no son únicos en el modelo).
--   * 'Aseo Total Caldas' e 'Importadora del Oriente' NO tienen suministros
--     (consulta "proveedores sin suministros").
--   * 'Alimentos La Sabana' está INACTIVO (PActivo = 0) pero sí tiene
--     suministros: sirve para probar que no se puede eliminar (ORA-02292).

INSERT INTO TProveedor (PNit, PNombre, PDireccion, PCorreo, PTelefono, PActivo, CCodigo)
SELECT '900123456-7', 'Distribuidora del Quindío S.A.S.', 'Calle 21 # 15-30', 'ventas@distquindio.com.co', '6067451200', 1, c.CCodigo
  FROM TCiudad c JOIN TDepartamento d ON d.DCodigo = c.DCodigo
 WHERE c.CNombre = 'Armenia' AND d.DNombre = 'Quindío';

INSERT INTO TProveedor (PNit, PNombre, PDireccion, PCorreo, PTelefono, PActivo, CCodigo)
SELECT '900234567-1', 'Suministros Andinos Ltda.', 'Avenida 6N # 23-45', 'contacto@suministrosandinos.co', '6023856700', 1, c.CCodigo
  FROM TCiudad c JOIN TDepartamento d ON d.DCodigo = c.DCodigo
 WHERE c.CNombre = 'Cali' AND d.DNombre = 'Valle del Cauca';

INSERT INTO TProveedor (PNit, PNombre, PDireccion, PCorreo, PTelefono, PActivo, CCodigo)
SELECT '900345678-2', 'Comercializadora Paisa S.A.S.', 'Carrera 43A # 10-50', 'pedidos@compaisa.com.co', '6044448899', 1, c.CCodigo
  FROM TCiudad c JOIN TDepartamento d ON d.DCodigo = c.DCodigo
 WHERE c.CNombre = 'Medellín' AND d.DNombre = 'Antioquia';

INSERT INTO TProveedor (PNit, PNombre, PDireccion, PCorreo, PTelefono, PActivo, CCodigo)
SELECT '900456789-3', 'TecnoValle S.A.S.', 'Calle 30 # 28-12', 'soporte@tecnovalle.co', '6022731500', 1, c.CCodigo
  FROM TCiudad c JOIN TDepartamento d ON d.DCodigo = c.DCodigo
 WHERE c.CNombre = 'Palmira' AND d.DNombre = 'Valle del Cauca';

INSERT INTO TProveedor (PNit, PNombre, PDireccion, PCorreo, PTelefono, PActivo, CCodigo)
SELECT '900567890-4', 'Papelería Central Ltda.', 'Carrera 8 # 19-40', 'ventas@papeleriacentral.co', '6063350022', 1, c.CCodigo
  FROM TCiudad c JOIN TDepartamento d ON d.DCodigo = c.DCodigo
 WHERE c.CNombre = 'Pereira' AND d.DNombre = 'Risaralda';

INSERT INTO TProveedor (PNit, PNombre, PDireccion, PCorreo, PTelefono, PActivo, CCodigo)
SELECT '900678901-5', 'Ferretería El Tornillo S.A.S.', 'Calle 36 # 17-25', 'pedidos@eltornillo.com.co', '6076342211', 1, c.CCodigo
  FROM TCiudad c JOIN TDepartamento d ON d.DCodigo = c.DCodigo
 WHERE c.CNombre = 'Bucaramanga' AND d.DNombre = 'Santander';

INSERT INTO TProveedor (PNit, PNombre, PDireccion, PCorreo, PTelefono, PActivo, CCodigo)
SELECT '900789012-6', 'Aseo Total Caldas S.A.S.', 'Avenida Santander # 52-10', 'info@aseototal.co', '6068857400', 1, c.CCodigo
  FROM TCiudad c JOIN TDepartamento d ON d.DCodigo = c.DCodigo
 WHERE c.CNombre = 'Manizales' AND d.DNombre = 'Caldas';

INSERT INTO TProveedor (PNit, PNombre, PDireccion, PCorreo, PTelefono, PActivo, CCodigo)
SELECT '900890123-8', 'Alimentos La Sabana S.A.S.', 'Autopista Sur # 12-80', 'compras@lasabana.com.co', '6017220300', 0, c.CCodigo
  FROM TCiudad c JOIN TDepartamento d ON d.DCodigo = c.DCodigo
 WHERE c.CNombre = 'Soacha' AND d.DNombre = 'Cundinamarca';

INSERT INTO TProveedor (PNit, PNombre, PDireccion, PCorreo, PTelefono, PActivo, CCodigo)
SELECT '900901234-9', 'Importadora del Oriente S.A.S.', 'Carrera 27 # 30-15', 'gerencia@importadoraoriente.co', '6076199800', 1, c.CCodigo
  FROM TCiudad c JOIN TDepartamento d ON d.DCodigo = c.DCodigo
 WHERE c.CNombre = 'Floridablanca' AND d.DNombre = 'Santander';

-- ============================ SUMINISTROS ===============================
-- 32 suministros. El precio de suministro es el precio base del producto
-- multiplicado por un factor entre 0,87 y 1,13 (dentro de ±15 %), redondeado
-- a la centena. Días de entrega entre 1 y 15.
-- Productos con 3 proveedores: Detergente, Resma, Mouse, Café y Martillo.
-- 'Archivador AZ oficio' no tiene proveedores.

-- Distribuidora del Quindío (900123456-7)
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 2, ROUND(pr.PrPrecioBase * 0.95, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900123456-7' AND pr.PrNombre = 'Detergente en polvo 3 kg';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 2, ROUND(pr.PrPrecioBase * 1.05, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900123456-7' AND pr.PrNombre = 'Jabón líquido para manos 1 L';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 2, ROUND(pr.PrPrecioBase * 0.97, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900123456-7' AND pr.PrNombre = 'Escoba de cerdas suaves';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 3, ROUND(pr.PrPrecioBase * 1.04, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900123456-7' AND pr.PrNombre = 'Resma de papel carta';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 1, ROUND(pr.PrPrecioBase * 0.92, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900123456-7' AND pr.PrNombre = 'Café molido 500 g';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 1, ROUND(pr.PrPrecioBase * 1.00, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900123456-7' AND pr.PrNombre = 'Azúcar 1 kg';

-- Suministros Andinos (900234567-1)
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 4, ROUND(pr.PrPrecioBase * 0.90, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900234567-1' AND pr.PrNombre = 'Detergente en polvo 3 kg';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 5, ROUND(pr.PrPrecioBase * 1.08, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900234567-1' AND pr.PrNombre = 'Desinfectante multiusos 2 L';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 6, ROUND(pr.PrPrecioBase * 1.10, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900234567-1' AND pr.PrNombre = 'Mouse inalámbrico';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 3, ROUND(pr.PrPrecioBase * 0.96, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900234567-1' AND pr.PrNombre = 'Galletas surtidas x12';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 5, ROUND(pr.PrPrecioBase * 1.02, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900234567-1' AND pr.PrNombre = 'Martillo de uña 16 oz';

-- Comercializadora Paisa (900345678-2)
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 7, ROUND(pr.PrPrecioBase * 1.12, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900345678-2' AND pr.PrNombre = 'Detergente en polvo 3 kg';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 5, ROUND(pr.PrPrecioBase * 1.01, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900345678-2' AND pr.PrNombre = 'Cuaderno argollado 100 hojas';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 4, ROUND(pr.PrPrecioBase * 0.88, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900345678-2' AND pr.PrNombre = 'Café molido 500 g';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 3, ROUND(pr.PrPrecioBase * 1.06, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900345678-2' AND pr.PrNombre = 'Agua en botella x24';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 6, ROUND(pr.PrPrecioBase * 0.93, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900345678-2' AND pr.PrNombre = 'Cinta métrica 5 m';

-- TecnoValle (900456789-3)
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 3, ROUND(pr.PrPrecioBase * 0.90, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900456789-3' AND pr.PrNombre = 'Mouse inalámbrico';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 3, ROUND(pr.PrPrecioBase * 0.95, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900456789-3' AND pr.PrNombre = 'Teclado USB en español';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 8, ROUND(pr.PrPrecioBase * 0.98, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900456789-3' AND pr.PrNombre = 'Monitor LED 24 pulgadas';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 4, ROUND(pr.PrPrecioBase * 1.07, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900456789-3' AND pr.PrNombre = 'Memoria USB 64 GB';

-- Papelería Central (900567890-4)
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 2, ROUND(pr.PrPrecioBase * 0.94, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900567890-4' AND pr.PrNombre = 'Resma de papel carta';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 2, ROUND(pr.PrPrecioBase * 0.89, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900567890-4' AND pr.PrNombre = 'Cuaderno argollado 100 hojas';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 2, ROUND(pr.PrPrecioBase * 1.03, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900567890-4' AND pr.PrNombre = 'Caja de bolígrafos x12';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 10, ROUND(pr.PrPrecioBase * 1.13, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900567890-4' AND pr.PrNombre = 'Mouse inalámbrico';

-- Ferretería El Tornillo (900678901-5)
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 2, ROUND(pr.PrPrecioBase * 0.91, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900678901-5' AND pr.PrNombre = 'Martillo de uña 16 oz';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 3, ROUND(pr.PrPrecioBase * 1.05, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900678901-5' AND pr.PrNombre = 'Juego de destornilladores x6';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 2, ROUND(pr.PrPrecioBase * 1.00, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900678901-5' AND pr.PrNombre = 'Cinta métrica 5 m';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 12, ROUND(pr.PrPrecioBase * 0.96, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900678901-5' AND pr.PrNombre = 'Taladro percutor 1/2 pulgada';

-- Alimentos La Sabana (900890123-8, inactivo)
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 9, ROUND(pr.PrPrecioBase * 1.08, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900890123-8' AND pr.PrNombre = 'Café molido 500 g';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 7, ROUND(pr.PrPrecioBase * 0.87, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900890123-8' AND pr.PrNombre = 'Galletas surtidas x12';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 15, ROUND(pr.PrPrecioBase * 1.11, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900890123-8' AND pr.PrNombre = 'Resma de papel carta';
INSERT INTO TSuministro (SDiasEntrega, SPrecio, PCodigo, PrCodigo)
SELECT 14, ROUND(pr.PrPrecioBase * 1.09, -2), p.PCodigo, pr.PrCodigo FROM TProveedor p, TProducto pr
 WHERE p.PNit = '900890123-8' AND pr.PrNombre = 'Martillo de uña 16 oz';

COMMIT;
