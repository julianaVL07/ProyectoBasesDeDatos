# Consultas del sistema

Este documento describe las cinco consultas del módulo **Reportes/Consultas** y explica por qué cumplen lo que pide el enunciado:

> - Se deben crear **tres consultas con JOIN** que junten al menos datos de **3 tablas**.
> - Se deben construir y mostrar los datos arrojados por **dos consultas que posean subconsultas** y que relacionen **dos tablas**.

Todas las consultas están en `modelo/dao/ConsultaDAO.java` (una constante SQL por consulta), se ejecutan desde `controlador/ConsultaControlador.java` y se muestran en `vista/consultas/VentanaConsultas.java` (menú **Reportes/Consultas → Consultas**). Las puede ejecutar cualquier usuario con sesión (Administrador, Tradicional y Esporádico).

| # | Consulta | Tipo | Tablas |
|---|----------|------|--------|
| a | Proveedores con ciudad y departamento | JOIN | TProveedor, TCiudad, TDepartamento |
| b | Productos con proveedores y precio de suministro | JOIN | TProducto, TSuministro, TProveedor |
| c | Productos por categoría y proveedor | JOIN | TProducto, TCategoria, TSuministro, TProveedor |
| d | Productos con precio mayor al promedio de su categoría | Subconsulta correlacionada | TProducto, TCategoria |
| e | Proveedores sin suministros | Subconsulta `NOT EXISTS` | TProveedor, TSuministro |

---

## (a) Proveedores con ciudad y departamento — JOIN de 3 tablas

```sql
SELECT p.PNit, p.PNombre, p.PTelefono, p.PCorreo,
       c.CNombre, d.DNombre,
       CASE p.PActivo WHEN 1 THEN 'Sí' ELSE 'No' END AS Activo
FROM TProveedor p
JOIN TCiudad c       ON c.CCodigo = p.CCodigo
JOIN TDepartamento d ON d.DCodigo = c.DCodigo
ORDER BY d.DNombre, c.CNombre, p.PNombre;
```

**Justificación:** el proveedor solo guarda el código de su ciudad (`CCodigo`) y la ciudad solo guarda el código de su departamento (`DCodigo`), porque así quedó normalizado el modelo. Para ver la ubicación completa hay que recorrer las dos llaves foráneas: `TProveedor → TCiudad → TDepartamento`. Sirve para saber en qué parte de Colombia está cada proveedor.

**Columnas:** NIT, Proveedor, Teléfono, Correo, Ciudad, Departamento, Activo.

## (b) Productos con proveedores y precio de suministro — JOIN de 3 tablas

```sql
SELECT pr.PrNombre, pr.PrPrecioBase, p.PNombre, s.SPrecio,
       s.SDiasEntrega, (s.SPrecio - pr.PrPrecioBase) AS Diferencia
FROM TProducto pr
JOIN TSuministro s ON s.PrCodigo = pr.PrCodigo
JOIN TProveedor p  ON p.PCodigo  = s.PCodigo
ORDER BY pr.PrNombre, s.SPrecio;
```

**Justificación:** la relación entre Producto y Proveedor es **muchos a muchos** y se resolvió con la entidad asociativa `TSuministro`, que guarda el precio y los días de entrega de cada pareja. Para ver qué proveedores venden cada producto hay que pasar por `TSuministro`. La columna *Diferencia* (precio de suministro − precio base) ayuda a decidir a qué proveedor conviene hacerle el pedido, que es el objetivo principal del sistema.

**Columnas:** Producto, Precio base, Proveedor, Precio suministro, Días de entrega, Diferencia.

## (c) Productos por categoría y proveedor — JOIN de 4 tablas

```sql
SELECT ca.CaNombre, pr.PrNombre, pr.PrStock, p.PNombre, s.SDiasEntrega
FROM TProducto pr
JOIN TCategoria ca ON ca.CaCodigo = pr.CaCodigo
JOIN TSuministro s ON s.PrCodigo  = pr.PrCodigo
JOIN TProveedor p  ON p.PCodigo   = s.PCodigo
ORDER BY ca.CaNombre, pr.PrNombre, s.SDiasEntrega;
```

**Justificación:** agrupa la información por familias de productos (categorías), como pide el enunciado. Une la categoría del producto (`TProducto → TCategoria`) con sus proveedores (`TProducto → TSuministro → TProveedor`). Así se ve, por categoría, cuánto stock hay de cada producto y qué proveedor lo entrega más rápido.

**Columnas:** Categoría, Producto, Stock, Proveedor, Días de entrega.

---

## (d) Productos con precio mayor al promedio de su categoría — subconsulta

```sql
SELECT ca.CaNombre, pr.PrNombre, pr.PrPrecioBase,
       (SELECT ROUND(AVG(p3.PrPrecioBase), 2)
          FROM TProducto p3
         WHERE p3.CaCodigo = pr.CaCodigo) AS PromedioCategoria
FROM TProducto pr
JOIN TCategoria ca ON ca.CaCodigo = pr.CaCodigo
WHERE pr.PrPrecioBase > (SELECT AVG(p2.PrPrecioBase)
                           FROM TProducto p2
                          WHERE p2.CaCodigo = pr.CaCodigo)
ORDER BY ca.CaNombre, pr.PrPrecioBase DESC;
```

**Justificación:** relaciona **dos tablas** (`TProducto` y `TCategoria`). La subconsulta del `WHERE` es **correlacionada**: para cada producto calcula el promedio de precios de *su misma* categoría (`p2.CaCodigo = pr.CaCodigo`) y deja solo los que están por encima. En el `SELECT` se usa una **subconsulta escalar** para mostrar ese promedio redondeado a 2 decimales y poder compararlo. Sirve para identificar los productos más costosos de cada familia.

**Columnas:** Categoría, Producto, Precio base, Promedio de la categoría.

## (e) Proveedores sin suministros — subconsulta con NOT EXISTS

```sql
SELECT p.PNit, p.PNombre, p.PTelefono, p.PCorreo,
       CASE p.PActivo WHEN 1 THEN 'Sí' ELSE 'No' END AS Activo
FROM TProveedor p
WHERE NOT EXISTS (SELECT 1
                    FROM TSuministro s
                   WHERE s.PCodigo = p.PCodigo)
ORDER BY p.PNombre;
```

**Justificación:** relaciona **dos tablas** (`TProveedor` y `TSuministro`). Para cada proveedor, la subconsulta busca si tiene al menos un suministro; con `NOT EXISTS` quedan solo los que no tienen ninguno. Se prefirió `NOT EXISTS` en vez de `NOT IN` porque no tiene problemas si la subconsulta devolviera valores `NULL` y Oracle puede detenerse en la primera coincidencia. Sirve para detectar proveedores registrados a los que todavía no se les han asignado productos.

**Columnas:** NIT, Proveedor, Teléfono, Correo, Activo.

---

## Detalles de implementación

- Las consultas no reciben datos del usuario, pero igual se ejecutan con `PreparedStatement` y try-with-resources, como el resto de los DAO.
- `ConsultaDAO` devuelve un `ResultadoConsulta` (títulos de columnas + lista de filas), así la ventana puede mostrar cualquier consulta en la misma `JTable`.
- La ventana muestra una descripción de cada consulta, el número de filas obtenidas, y formatea los números con separador de miles (y 2 decimales cuando hay valores con decimales).
- Si ocurre un error de base de datos, el controlador lo traduce con `ErroresBD.traducir` y la vista lo muestra en un `JOptionPane`.
