# Guía de pruebas de integración por rol

Recorrido completo de la aplicación con los **tres tipos de usuario** (Administrador, Tradicional y Esporádico) para comprobar que todos los módulos funcionan juntos: login, permisos, CRUD de entidades, suministros, consultas, reportes y bitácora.

Las pruebas detalladas de cada CRUD de Entidades están en [`PruebasEntidades.md`](PruebasEntidades.md); aquí se revisa el flujo completo de punta a punta.

## Preparación

1. En SQL Developer, con el usuario del proyecto, ejecutar en orden:
   `00_borrar_tablas.sql` → `01_crear_tablas.sql` → `02_datos_iniciales.sql` → `03_datos_prueba.sql`.
2. Configurar `src/main/resources/db.properties` (ver la sección 7 de [`DocumentoEntregable.md`](DocumentoEntregable.md)).
3. Ejecutar la aplicación (`mvn exec:java` o desde el IDE).

| Usuario | Clave | Rol |
|---------|-------|-----|
| `admin` | `Admin123*` | Administrador |
| `tradicional` | `Trad123*` | Tradicional |
| `esporadico` | `Espo123*` | Esporádico |

Resultados esperados con los datos de `03_datos_prueba.sql` (sin cambios previos): 9 proveedores, 20 productos, 5 categorías, 32 suministros.

En cada prueba marcar ✔ si el resultado coincide o anotar lo que pasó.

---

## 0. Arranque

| # | Acción | Resultado esperado | ✔ |
|---|--------|--------------------|---|
| 0.1 | Apagar Oracle (o poner una clave mala en `db.properties`) y ejecutar la aplicación | Mensaje de que no se pudo conectar a la base de datos y la aplicación se cierra. | |
| 0.2 | Encender Oracle, corregir `db.properties` y ejecutar | Aparece la ventana de Login. | |

## 1. Login

| # | Acción | Resultado esperado | ✔ |
|---|--------|--------------------|---|
| 1.1 | Pulsar **Ingresar** con los campos vacíos | "Ingrese el nombre de usuario." No cuenta como intento fallido. | |
| 1.2 | Usuario `admin` con clave `mala123` | "Usuario o contraseña incorrectos." e "Intentos restantes: 2". | |
| 1.3 | Usuario `noexiste` con cualquier clave | Mismo mensaje "Usuario o contraseña incorrectos." (no revela si el usuario existe). "Intentos restantes: 1". | |
| 1.4 | Un tercer intento fallido | "Se agotaron los 3 intentos. La aplicación se cerrará." y la aplicación se cierra. | |
| 1.5 | Volver a abrir, escribir `admin` / `Admin123*` y pulsar **Enter** | Ingresa y se abre el Menú principal. La barra de estado muestra el usuario y su rol. | |

---

## 2. Recorrido como ADMINISTRADOR (`admin`)

### 2.1 Menús visibles

| # | Acción | Resultado esperado | ✔ |
|---|--------|--------------------|---|
| 2.1.1 | Revisar la barra de menús | Aparecen **Entidades, Transacciones, Reportes/Consultas, Auditoría y Sesión**. | |

### 2.2 Gestión de usuarios

Menú **Auditoría → Gestión de usuarios**.

| # | Acción | Resultado esperado | ✔ |
|---|--------|--------------------|---|
| 2.2.1 | Abrir la ventana | La tabla muestra `admin`, `tradicional` y `esporadico` con nombre, rol, activo y fecha de creación. | |
| 2.2.2 | Pulsar **Nuevo** y revisar el combo de roles | **No** aparece ADMINISTRADOR (ya existe uno); solo TRADICIONAL y ESPORADICO. | |
| 2.2.3 | Crear usuario `ana` | "El nombre de usuario debe tener entre 4 y 30 caracteres." | |
| 2.2.4 | Crear usuario `ana perez` | "El nombre de usuario no puede tener espacios." | |
| 2.2.5 | Crear `aperez`, nombre "Ana Pérez", rol TRADICIONAL, clave `abc` | "La contraseña debe tener al menos 6 caracteres." | |
| 2.2.6 | Igual, clave `Ana123*` y confirmación `Ana124*` | "La contraseña y su confirmación no coinciden." | |
| 2.2.7 | Igual, clave y confirmación `Ana123*` | "Usuario creado correctamente." Aparece en la tabla como activo. | |
| 2.2.8 | Crear otro usuario con nombre `tradicional` | "Ya existe un usuario con ese nombre de usuario." | |
| 2.2.9 | Seleccionar `aperez`, cambiar el rol a ESPORADICO y pulsar **Actualizar** | "Usuario actualizado correctamente." La tabla muestra el nuevo rol. | |
| 2.2.10 | Seleccionar `aperez` → **Cambiar contraseña** → `Nueva456*` | "Contraseña actualizada correctamente." | |
| 2.2.11 | Seleccionar `aperez` → **Desactivar** → confirmar | La columna Activo pasa a "No" y el botón cambia a **Activar**. | |
| 2.2.12 | Seleccionar `admin` → **Desactivar** | "No puede desactivar su propio usuario." | |
| 2.2.13 | Seleccionar `admin`, cambiarle el rol a TRADICIONAL y **Actualizar** | "No puede quitarse a sí mismo el rol de administrador." | |
| 2.2.14 | *(En SQL Developer)* `INSERT INTO TUsuario (UNombreUsuario, UContrasena, UNombreCompleto, RCodigo) VALUES ('admin2', 'x', 'Otro admin', 1);` | Oracle rechaza con `ORA-00001` (`UX_USUARIO_UNICO_ADMIN`): solo puede haber un administrador. | |

### 2.3 Entidades (CRUD rápido)

| # | Acción | Resultado esperado | ✔ |
|---|--------|--------------------|---|
| 2.3.1 | **Departamentos**: crear "Nariño" | Se guarda y aparece con su código. | |
| 2.3.2 | **Ciudades**: crear "Pasto" en Nariño | Se guarda y la tabla muestra el departamento. | |
| 2.3.3 | **Categorías**: crear "Cafetería" | Se guarda. | |
| 2.3.4 | **Productos**: crear "Vasos desechables x50", categoría Cafetería, precio `8.500`, stock `100` | Se guarda; el precio se muestra con separador de miles. | |
| 2.3.5 | **Proveedores**: crear NIT `901111222-3`, "Distribuciones Nariño S.A.S.", ciudad Pasto, correo `ventas@dnarino.co` | Se guarda como activo. | |
| 2.3.6 | **Proveedores**: crear otro con NIT `900123456-7` | Error de dato repetido (el NIT ya existe). | |
| 2.3.7 | **Departamentos**: eliminar "Nariño" | No se permite: tiene ciudades asociadas. | |

### 2.4 Transacción Suministros

Menú **Transacciones → Suministros**.

| # | Acción | Resultado esperado | ✔ |
|---|--------|--------------------|---|
| 2.4.1 | Abrir la ventana y revisar el combo de proveedores | Solo aparecen proveedores **activos** ("Alimentos La Sabana" no aparece porque está inactivo). | |
| 2.4.2 | Elegir "Distribuidora del Quindío S.A.S." | La tabla muestra sus 6 productos con precio de suministro y días de entrega. | |
| 2.4.3 | Elegir "Distribuciones Nariño S.A.S." (creado en 2.3.5) | La tabla queda vacía. | |
| 2.4.4 | Elegir el producto "Vasos desechables x50" | Al lado del combo se muestra su precio base como referencia. | |
| 2.4.5 | Precio `-100`, días `3`, **Asignar** | "El precio no puede ser negativo." | |
| 2.4.6 | Precio `8.200`, días `1000`, **Asignar** | "Los días de entrega deben estar entre 0 y 999." | |
| 2.4.7 | Precio `8.200`, días `3`, **Asignar** | "Producto asignado al proveedor." Aparece en la tabla. | |
| 2.4.8 | Volver a asignar el mismo producto al mismo proveedor | "Ese proveedor ya suministra el producto seleccionado. Selecciónelo en la tabla si desea modificar su precio o días de entrega." | |
| 2.4.9 | Seleccionar el suministro, cambiar días a `2` y pulsar **Actualizar** | "Suministro actualizado." | |
| 2.4.10 | **Proveedores**: intentar eliminar "Distribuciones Nariño S.A.S." | No se permite porque tiene suministros asociados; se ofrece desactivarlo. | |
| 2.4.11 | **Suministros**: seleccionar el suministro → **Quitar** → confirmar | "Suministro eliminado." | |
| 2.4.12 | **Proveedores**: desactivar "Distribuciones Nariño S.A.S." y en Suministros pulsar **Recargar** | El proveedor ya no aparece en el combo. | |

### 2.5 Consultas

Menú **Reportes/Consultas → Consultas**. Ejecutar cada una y revisar el contador de filas (valores con los datos de prueba originales; si se hicieron los pasos anteriores pueden cambiar en 1).

| # | Consulta | Resultado esperado | ✔ |
|---|----------|--------------------|---|
| 2.5.1 | (a) Proveedores con ciudad y departamento | 9 filas (10 con el proveedor de Nariño), con ciudad y departamento. | |
| 2.5.2 | (b) Productos con proveedores y precio de suministro | 32 filas. La columna Diferencia es precio suministro − precio base. | |
| 2.5.3 | (c) Productos por categoría y proveedor | 32 filas ordenadas por categoría. | |
| 2.5.4 | (d) Productos con precio mayor al promedio de su categoría | 6 filas: Detergente, Resma, Monitor, Café, Agua y Taladro, con el promedio de su categoría. | |
| 2.5.5 | (e) Proveedores sin suministros | 2 filas: "Aseo Total Caldas S.A.S." e "Importadora del Oriente S.A.S." (3 con el proveedor de Nariño, que quedó sin suministros en 2.4.11). | |

### 2.6 Reportes

Menú **Reportes/Consultas → Reportes**. En cada pestaña: elegir filtros → **Generar PDF** → elegir dónde guardar. El PDF debe abrirse en el visor dentro de la aplicación.

| # | Reporte y filtros | Resultado esperado | ✔ |
|---|-------------------|--------------------|---|
| 2.6.1 | **Proveedores por ubicación**: Departamento "Valle del Cauca", Ciudad "Todas" | 2 proveedores (Cali y Palmira). El subtítulo muestra los filtros. | |
| 2.6.2 | Igual, Departamento "Todos" | Todos los proveedores. | |
| 2.6.3 | **Productos por precio y stock**: precio mínimo `50000`, máximo `10000` | "El precio mínimo no puede ser mayor que el precio máximo." | |
| 2.6.4 | Precio mínimo `20000`, máximo `50000`, stock mínimo `1` | Lista los productos en ese rango que tienen existencias. | |
| 2.6.5 | **Productos de un proveedor**: "TecnoValle S.A.S." | 4 productos de Tecnología con fila de TOTAL y promedio de días. | |
| 2.6.6 | **Productos de una categoría**: "Papelería" | 4 productos; "Archivador AZ oficio" aparece con 0 proveedores. | |
| 2.6.7 | **Resumen por categoría**: "Todas" | Una fila por categoría (cantidad, promedio, mínimo, máximo, stock) y fila TOTAL. "Cafetería" (si se creó) aparece con los productos que tenga. | |
| 2.6.8 | En el visor: pasar de página y usar el zoom | Funciona; el pie de página muestra la fecha, el usuario `admin` y "Página X de Y". | |

### 2.7 Cerrar sesión

| # | Acción | Resultado esperado | ✔ |
|---|--------|--------------------|---|
| 2.7.1 | **Sesión → Cerrar sesión** | Se cierra el menú y vuelve a aparecer el Login. | |

---

## 3. Recorrido como TRADICIONAL (`tradicional`)

| # | Acción | Resultado esperado | ✔ |
|---|--------|--------------------|---|
| 3.1 | Ingresar con `tradicional` / `Trad123*` | Abre el Menú principal con rol TRADICIONAL. | |
| 3.2 | Revisar la barra de menús | Aparecen **Entidades, Transacciones, Reportes/Consultas y Sesión**. **No** aparece Auditoría. | |
| 3.3 | **Categorías**: crear "Prueba Tradicional", editarla y luego eliminarla | Las tres operaciones funcionan. | |
| 3.4 | **Productos**: editar el stock de "Azúcar 1 kg" a `250` | Se actualiza. | |
| 3.5 | **Suministros**: asignar "Archivador AZ oficio" a "Papelería Central Ltda." (precio `12.000`, días `2`) | "Producto asignado al proveedor." | |
| 3.6 | **Consultas**: ejecutar la (b) | Aparece el nuevo suministro de 3.5. | |
| 3.7 | **Reportes**: generar "Productos de un proveedor" de "Papelería Central Ltda." | Incluye el Archivador AZ; el pie del PDF dice `tradicional`. | |
| 3.8 | **Sesión → Salir** | La aplicación se cierra. | |

---

## 4. Recorrido como ESPORÁDICO (`esporadico`)

| # | Acción | Resultado esperado | ✔ |
|---|--------|--------------------|---|
| 4.1 | Ingresar con `esporadico` / `Espo123*` | Abre el Menú principal con rol ESPORADICO. | |
| 4.2 | Revisar la barra de menús | Solo aparecen **Reportes/Consultas y Sesión**. No puede entrar a Entidades, Transacciones ni Auditoría. | |
| 4.3 | **Consultas**: ejecutar las 5 consultas | Todas funcionan. | |
| 4.4 | **Reportes**: generar "Resumen por categoría" | Se genera y se ve en el visor. | |
| 4.5 | Cerrar la ventana principal con la **X** | La aplicación se cierra y registra la salida en la bitácora. | |

---

## 5. Usuario inactivo

| # | Acción | Resultado esperado | ✔ |
|---|--------|--------------------|---|
| 5.1 | Ingresar con `aperez` / `Nueva456*` (desactivado en 2.2.11) | "Usuario inactivo, contacte al administrador." | |
| 5.2 | Como `admin`, activar a `aperez`; cerrar sesión e ingresar con `aperez` | Ingresa con rol ESPORADICO (cambiado en 2.2.9). | |

---

## 6. Bitácora (como `admin`)

Menú **Auditoría → Bitácora**.

| # | Acción | Resultado esperado | ✔ |
|---|--------|--------------------|---|
| 6.1 | Abrir la ventana (por defecto muestra los últimos días, usuario "Todos") | Aparecen los ingresos de hoy de `admin`, `tradicional`, `esporadico` y `aperez`, del más reciente al más antiguo, con fecha y hora de ingreso y de salida. | |
| 6.2 | Revisar la fila de la sesión actual de `admin` | En la salida dice **"Sesión abierta"**. | |
| 6.3 | Revisar las filas de `tradicional` (salió con **Salir**) y `esporadico` (salió con la **X**) | Ambas tienen fecha y hora de salida. | |
| 6.4 | Filtrar por usuario `tradicional` | Solo aparecen los registros de `tradicional`. | |
| 6.5 | Poner la fecha "desde" posterior a la fecha "hasta" y pulsar **Buscar** | "La fecha "desde" no puede ser posterior a la fecha "hasta"." | |
| 6.6 | Pulsar **Hoy** | Las dos fechas vuelven a hoy y se recarga la tabla. | |
| 6.7 | *(En SQL Developer)* `SELECT * FROM TBitacora ORDER BY BCodigo DESC;` | Los mismos registros; `BFechaSalida` y `BHoraSalida` en `NULL` solo para la sesión abierta. | |
| 6.8 | Salir con **Sesión → Salir** y repetir la consulta 6.7 | La última sesión de `admin` ya tiene fecha y hora de salida. | |

---

## Resumen de resultados

| Bloque | Pruebas | Pasaron | Observaciones |
|--------|:-------:|:-------:|---------------|
| 0. Arranque | 2 | | |
| 1. Login | 5 | | |
| 2. Administrador | 48 | | |
| 3. Tradicional | 8 | | |
| 4. Esporádico | 5 | | |
| 5. Usuario inactivo | 2 | | |
| 6. Bitácora | 8 | | |

Probado por: ____________________  Fecha: ____________
