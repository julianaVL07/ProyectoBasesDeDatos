# Guía de prueba manual — Módulo de Entidades

Pasos para comprobar a mano los CRUD de **Departamentos, Ciudades, Categorías,
Productos y Proveedores** y los permisos por rol.

## Preparación

1. En SQL Developer, con el usuario del proyecto, ejecutar en orden:
   `00_borrar_tablas.sql` → `01_crear_tablas.sql` → `02_datos_iniciales.sql` → `03_datos_prueba.sql`.
2. Configurar `src/main/resources/db.properties` y ejecutar la aplicación.

| Usuario       | Clave       | Rol           |
|---------------|-------------|---------------|
| `admin`       | `Admin123*` | Administrador |
| `tradicional` | `Trad123*`  | Tradicional   |
| `esporadico`  | `Espo123*`  | Esporádico    |

## 1. Comportamiento común (repetir en cada ventana)

| # | Acción | Resultado esperado |
|---|--------|--------------------|
| 1.1 | Abrir la ventana desde el menú **Entidades** | La tabla carga los registros y abajo dice "Registros: N". |
| 1.2 | Volver a elegir la misma opción del menú | No se abre otra ventana: se trae al frente la que ya estaba. |
| 1.3 | Clic en una fila | El formulario se llena; el código no se puede editar; se habilitan **Actualizar** y **Eliminar** y se deshabilita **Guardar**. |
| 1.4 | **Nuevo** | El formulario queda vacío y solo **Guardar** queda habilitado. |
| 1.5 | Escribir parte de un nombre en **Buscar** y pulsar Enter | La tabla se filtra (sin importar mayúsculas). |
| 1.6 | **Limpiar** | Se borra el texto de búsqueda y se vuelven a ver todos los registros. |
| 1.7 | **Guardar** con los campos vacíos | Mensaje de error indicando el dato que falta; no se guarda nada. |
| 1.8 | **Eliminar** y responder "No" en la confirmación | No se elimina el registro. |
| 1.9 | Doble clic en una celda | La celda no se puede editar. |

## 2. Departamentos

| # | Acción | Resultado esperado |
|---|--------|--------------------|
| 2.1 | Guardar "Risaralda" (ya existe en los datos de prueba) | "Ya existe un departamento con ese nombre" (ORA-00001). |
| 2.2 | Guardar "Nariño" | Aparece en la tabla con su código generado. |
| 2.3 | Escribir un nombre de más de 60 caracteres | Error de longitud antes de ir a la base de datos. |
| 2.4 | Eliminar "Quindío" | No se permite: tiene ciudades asociadas (ORA-02292). |
| 2.5 | Eliminar "Nariño" (recién creado) | Se elimina correctamente. |

## 3. Ciudades

| # | Acción | Resultado esperado |
|---|--------|--------------------|
| 3.1 | Guardar sin elegir departamento | "Seleccione el departamento de la ciudad." |
| 3.2 | Guardar "Pasto" con un departamento | Aparece en la tabla junto al nombre del departamento. |
| 3.3 | Crear un departamento nuevo en su ventana y pulsar **Limpiar** en Ciudades | El combo de departamentos ya muestra el nuevo. |
| 3.4 | Eliminar "Armenia" | No se permite: tiene proveedores asociados. |

## 4. Categorías

| # | Acción | Resultado esperado |
|---|--------|--------------------|
| 4.1 | Guardar "Aseo" | Error de nombre repetido. |
| 4.2 | Guardar una categoría con descripción de más de 200 caracteres | Error de longitud. |
| 4.3 | Eliminar "Aseo" | No se permite: tiene productos asociados. |

## 5. Productos

| # | Acción | Resultado esperado |
|---|--------|--------------------|
| 5.1 | Revisar la columna de precio | Se ve con separador de miles y alineada a la derecha. |
| 5.2 | Guardar con precio `-100` o stock `-1` | Error: deben ser mayores o iguales a 0. |
| 5.3 | Guardar con precio `abc` | Error: el precio debe ser un número. |
| 5.4 | Guardar sin categoría | Error pidiendo seleccionar la categoría. |
| 5.5 | Actualizar el stock de "Desinfectante multiusos 2 L" de 0 a 10 | Se actualiza y la tabla muestra 10. |
| 5.6 | Eliminar "Detergente en polvo 3 kg" | No se permite: tiene suministros asociados. |

## 6. Proveedores

| # | Acción | Resultado esperado |
|---|--------|--------------------|
| 6.1 | Elegir un departamento en el combo | El combo de ciudades solo muestra las de ese departamento. |
| 6.2 | Clic en "Distribuidora del Quindío S.A.S." | Se selecciona Quindío y luego Armenia; la casilla **Activo** está marcada. |
| 6.3 | Guardar con NIT `ABC123` | Error de formato de NIT. |
| 6.4 | Guardar con NIT `900123456-7` | "Ya existe un proveedor con ese NIT." |
| 6.5 | Guardar con correo `ventas@empresa` | Error de formato de correo. |
| 6.6 | Guardar con teléfono `12345` o `311-ABC-4567` | Error: mínimo 7 dígitos y solo espacios, guiones, paréntesis o "+". |
| 6.7 | Guardar dejando dirección, correo y teléfono vacíos | Se guarda (son opcionales). |
| 6.8 | Guardar sin ciudad | "Seleccione la ciudad del proveedor." |
| 6.9 | Guardar un proveedor válido nuevo (p. ej. NIT `901000111-2`, ciudad Pasto) | Aparece en la tabla con "Activo: Sí". |
| 6.10 | Eliminar "Importadora del Oriente S.A.S." (no tiene suministros) | Se elimina correctamente. |
| 6.11 | Eliminar "Distribuidora del Quindío S.A.S." (tiene suministros) | Mensaje: tiene suministros asociados, **¿Desea desactivarlo en su lugar?** Con "Sí" queda "Activo: No" y no se borra. |
| 6.12 | Repetir 6.11 con "No" | No se elimina ni se desactiva. |
| 6.13 | Eliminar "Alimentos La Sabana S.A.S." (ya inactivo, con suministros) | Error indicando que tiene suministros y que ya está inactivo. |
| 6.14 | Abrir **Transacciones → Suministros** | Los proveedores inactivos no aparecen en el combo. |
| 6.15 | Marcar de nuevo **Activo** a un proveedor y **Actualizar** | Vuelve a aparecer en Suministros. |

## 7. Permisos por rol

| # | Usuario | Acción | Resultado esperado |
|---|---------|--------|--------------------|
| 7.1 | `admin` | Revisar la barra de menús | Ve **Entidades, Transacciones, Reportes/Consultas, Auditoría y Sesión**. |
| 7.2 | `tradicional` | Revisar la barra de menús | Ve **Entidades, Transacciones, Reportes/Consultas y Sesión** (sin Auditoría). |
| 7.3 | `tradicional` | Hacer un CRUD completo en Proveedores | Funciona igual que con `admin`. |
| 7.4 | `esporadico` | Revisar la barra de menús | Solo ve **Reportes/Consultas y Sesión**: no puede abrir ninguna ventana de Entidades. |
| 7.5 | — | Si por código se abre una ventana de Entidades sin permiso de edición | El título dice "(solo lectura)", los campos no se pueden modificar y **Nuevo, Guardar, Actualizar y Eliminar** están deshabilitados. Además el controlador rechaza la operación ("Su rol no tiene permiso…"). |
| 7.6 | cualquiera | **Sesión → Cerrar sesión** y entrar con otro usuario | Los menús cambian según el nuevo rol. |
