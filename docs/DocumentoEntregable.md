<div align="center">

# UNIVERSIDAD DEL QUINDÍO

**Facultad de Ingeniería**
**Programa de Ingeniería de Sistemas y Computación**

**BASES DE DATOS I**

<br>

## PROYECTO FINAL
### Sistema de gestión de proveedores, productos y categorías

<br>

**Integrantes:**

[Nombre 1]
[Nombre 2]
[Nombre 3]

<br>

Armenia, Quindío
Noviembre de 2026

</div>

---

## Contenido

1. [Explicación general de la aplicación](#1-explicación-general-de-la-aplicación)
2. [Diagrama Entidad-Relación](#2-diagrama-entidad-relación)
3. [Diccionario de datos](#3-diccionario-de-datos)

---

## 1. Explicación general de la aplicación

### 1.1 Problema

Las empresas comerciales necesitan saber **a qué proveedores les pueden hacer los pedidos de sus productos**. Los proveedores pueden estar en cualquier ciudad de Colombia, un proveedor puede ofrecer varios productos y un mismo producto se puede conseguir con varios proveedores (cada uno con su propio precio y tiempo de entrega). Además, los productos se clasifican en categorías (familias de productos).

La aplicación que construimos permite registrar toda esta información, asignar qué productos ofrece cada proveedor, consultarla y sacar reportes en PDF. También tiene un sistema de seguridad con login, tres tipos de usuario y una bitácora de entradas y salidas.

### 1.2 Tecnologías

| Elemento | Herramienta |
|----------|-------------|
| Lenguaje | Java (JDK 25) |
| Interfaz gráfica | Swing (aplicación de escritorio) |
| Base de datos | Oracle (XE 21c / Free 23ai) |
| Conexión | JDBC puro con el driver `ojdbc11` |
| Reportes | Apache PDFBox (genera el PDF) y un visor propio dentro de la aplicación |
| Construcción | Maven |

### 1.3 Módulos

La ventana principal (`MenuPrincipal`) es un escritorio con ventanas internas y tiene los menús que pide el enunciado:

| Módulo | Menú / Ventana | Qué hace |
|--------|----------------|----------|
| **Ingreso** | Ventana de *Login* | Pide usuario y contraseña, valida contra `TUsuario` (clave en SHA-256), permite máximo 3 intentos y registra la entrada en la bitácora. Si el usuario está inactivo no lo deja entrar. |
| **Entidades** | Departamentos, Ciudades, Categorías, Productos, Proveedores | CRUD completo (`INSERT`, `SELECT`, `UPDATE`, `DELETE`) de cada entidad, con búsqueda por nombre y validaciones. |
| **Transacciones** | Suministros | Asigna productos a proveedores con su precio de suministro y días de entrega (tabla `TSuministro`, que resuelve la relación muchos a muchos). |
| **Reportes/Consultas** | Consultas, Reportes | 5 consultas (3 con JOIN de 3 o más tablas y 2 con subconsultas) mostradas en una tabla, y 5 reportes en PDF filtrados por el usuario y mostrados en un visor dentro de la aplicación. |
| **Auditoría** | Gestión de usuarios, Bitácora | El administrador crea, edita, activa/desactiva usuarios y les cambia la clave; también consulta la bitácora de entradas y salidas filtrando por fechas y usuario. |
| **Sesión** | Cerrar sesión, Salir | Registra la salida en la bitácora. "Cerrar sesión" vuelve al Login y "Salir" cierra la aplicación. |

### 1.4 Tipos de usuario y matriz de permisos

El enunciado pide tres tipos de usuario. Están en la tabla `TRol` con códigos fijos:

| Código | Rol | Descripción |
|:------:|-----|-------------|
| 1 | ADMINISTRADOR | Único usuario que puede crear nuevos usuarios. Solo puede existir uno. |
| 2 | TRADICIONAL | Ejecuta los CRUD de gestión de datos. |
| 3 | ESPORADICO | Solo ejecuta las consultas (y los reportes). |

**Matriz de permisos** (lo que ve y puede hacer cada rol después de ingresar):

| Módulo / acción | Administrador | Tradicional | Esporádico |
|-----------------|:-------------:|:-----------:|:----------:|
| Ingresar al sistema (Login) | ✔ | ✔ | ✔ |
| Entidades (CRUD de Departamentos, Ciudades, Categorías, Productos y Proveedores) | ✔ | ✔ | ✘ |
| Transacciones (Suministros) | ✔ | ✔ | ✘ |
| Consultas | ✔ | ✔ | ✔ |
| Reportes en PDF | ✔ | ✔ | ✔ |
| Gestión de usuarios | ✔ | ✘ | ✘ |
| Bitácora de entrada y salida | ✔ | ✘ | ✘ |
| Cerrar sesión / Salir | ✔ | ✔ | ✔ |

Los permisos se controlan en dos lugares:

1. **En el menú:** `MenuPrincipal` solo agrega los menús que el rol puede usar (por ejemplo, el esporádico no ve *Entidades* ni *Transacciones*, y solo el administrador ve *Auditoría*).
2. **En los controladores:** antes de guardar, editar o eliminar, cada controlador vuelve a revisar el permiso con la clase `Sesion` (`puedeEditar()`, `puedeGestionarUsuarios()`, `puedeVerBitacora()`). Así, aunque alguien llegara a abrir una ventana, el controlador no deja hacer la operación.

### 1.5 Arquitectura en capas

La aplicación sigue el patrón **Modelo – Vista – Controlador** con objetos **DAO** y **DTO**, como recomienda el anexo del proyecto. El paquete raíz es `co.edu.uniquindio.proyectobasesdedatos`:

```
vista  ──►  controlador  ──►  modelo.dao  ──►  Oracle (JDBC)
  │              │                 │
  └──────────────┴──── modelo.dto ─┘   (objetos que viajan entre capas)
```

| Paquete | Responsabilidad |
|---------|-----------------|
| `vista` | Ventanas Swing (`JInternalFrame`, `JDialog`). Solo muestran datos y capturan eventos; **nunca escriben SQL**. Los errores se muestran con `JOptionPane`. |
| `controlador` | Valida los datos (obligatorios, longitudes, formatos, rangos), revisa permisos y llama al DAO. Si algo falla lanza `ControladorException` con un mensaje claro en español. |
| `modelo.dao` | Una clase por tabla (`ProveedorDAO`, `ProductoDAO`, `SuministroDAO`, `UsuarioDAO`, `BitacoraDAO`, etc.) con las sentencias `INSERT`, `SELECT`, `UPDATE` y `DELETE`. Usan **siempre `PreparedStatement` con parámetros `?`** (evita la inyección SQL) y *try-with-resources* para cerrar conexiones. **No muestran mensajes**: lanzan `SQLException`. |
| `modelo.dto` | Objetos que representan una fila de cada tabla (`ProveedorDTO`, `ProductoDTO`, ...). Algunos traen además datos de solo lectura que vienen de un JOIN (por ejemplo, el nombre de la ciudad del proveedor). |
| `config` | `ConexionOracle`: lee `db.properties` y entrega conexiones JDBC. |
| `util` | `Sesion` (usuario conectado y permisos), `Seguridad` (hash SHA-256), `ErroresBD` (traduce los códigos `ORA-xxxxx` a mensajes en español) y `GeneradorPDF`. |

**¿Por qué sin frameworks?** El enunciado lo prohíbe explícitamente, y además el objetivo del curso es practicar SQL: con JDBC puro cada consulta está escrita a mano, se ve exactamente qué se le envía a Oracle y se trabaja directamente con llaves primarias, foráneas, restricciones y transacciones. Las únicas librerías externas son el driver de Oracle (`ojdbc11`, obligatorio para conectarse) y PDFBox (solo para dibujar el PDF de los reportes). Las ventanas también se escribieron a mano con Swing, sin generadores de formularios.

### 1.6 Seguridad

- **Contraseñas con hash SHA-256:** la clave nunca se guarda en texto plano. La clase `Seguridad` calcula el SHA-256 y lo guarda en hexadecimal (64 caracteres) en `TUsuario.UContrasena`. Al ingresar se calcula el hash de lo que escribió el usuario y se compara con el guardado. Los scripts SQL usan `STANDARD_HASH(..., 'SHA256')` de Oracle, que produce exactamente el mismo valor.
- **Un solo administrador, garantizado por la base de datos:** además de la validación en `UsuarioControlador`, el script crea un **índice único basado en función**:

  ```sql
  CREATE UNIQUE INDEX ux_usuario_unico_admin
      ON TUsuario (CASE WHEN RCodigo = 1 THEN 1 END);
  ```

  Para los usuarios que no son administradores la expresión da `NULL` y Oracle no indexa los `NULL`, así que no cuentan. Para el administrador da `1`, y como el índice es único, un segundo administrador provoca el error `ORA-00001`. Aunque alguien intente insertarlo directamente desde SQL Developer, la base de datos no lo permite.
- **Otras reglas:** el administrador no puede desactivarse ni quitarse el rol a sí mismo; un usuario inactivo (`UActivo = 0`) no puede ingresar; el Login se cierra después de 3 intentos fallidos y el mensaje de error no dice si lo que falló fue el usuario o la clave.
- **Bitácora:** cada ingreso inserta una fila en `TBitacora` (fecha y hora de entrada con los valores por defecto de Oracle) y al cerrar sesión, salir o cerrar la ventana se completa la fecha y hora de salida. Hay un *shutdown hook* de respaldo por si la aplicación se cierra de otra forma.
- **Inyección SQL:** todos los valores que escribe el usuario viajan como parámetros `?` de `PreparedStatement`; nunca se concatenan al SQL.

---

## 2. Diagrama Entidad-Relación

![ER](DiagramaEntidadRelacion.png)

El modelo tiene **8 entidades** y una **entidad asociativa** (`Suministro`), organizadas en dos módulos.

### 2.1 Módulo Entidades / Transacciones

| Relación | Entidades | Cardinalidad | Explicación |
|----------|-----------|:------------:|-------------|
| **tiene** | Departamento – Ciudad | 1 : N | Un departamento tiene muchas ciudades; cada ciudad pertenece a un solo departamento. En el modelo relacional `TCiudad` guarda la llave foránea `DCodigo`. |
| **contiene** | Ciudad – Proveedor | 1 : N | En una ciudad puede haber muchos proveedores; cada proveedor está ubicado en una sola ciudad. `TProveedor` guarda `CCodigo`. |
| **agrupa** | Categoría – Producto | 1 : N | Una categoría agrupa muchos productos (una familia de productos); cada producto pertenece a una sola categoría. `TProducto` guarda `CaCodigo`. |
| **ofrece / incluye** | Proveedor – Producto (a través de Suministro) | N : M | Como dice el enunciado, un proveedor puede proveer varios productos y un producto puede ser adquirido de varios proveedores. Esta relación muchos a muchos se resuelve con la entidad asociativa **Suministro**, que además tiene atributos propios: el **precio** al que ese proveedor vende ese producto y los **días de entrega**. Un proveedor ofrece N suministros y un producto está incluido en M suministros; cada suministro une exactamente un proveedor con un producto. |

### 2.2 Módulo Auditoría

| Relación | Entidades | Cardinalidad | Explicación |
|----------|-----------|:------------:|-------------|
| **agrupa** | Rol – Usuario | 1 : N | Cada usuario tiene un solo rol (Administrador, Tradicional o Esporádico) y un rol puede tener muchos usuarios. La excepción es el rol Administrador, que por regla del negocio solo puede tener un usuario (se controla con un índice único). `TUsuario` guarda `RCodigo`. |
| **registra** | Usuario – Bitácora | 1 : N | Cada vez que un usuario entra al sistema se registra una fila en la bitácora con la fecha y hora de entrada y, al salir, la fecha y hora de salida. Un usuario tiene muchos registros; cada registro es de un solo usuario. `TBitacora` guarda `UCodigo`. |

### 2.3 Decisiones de diseño

- **Ciudad y Departamento como entidades** (y no como texto dentro del proveedor) para no repetir nombres, evitar errores de escritura y poder filtrar los reportes por ubicación.
- **Llaves primarias sustitutas** numéricas (`...Codigo`) generadas con `IDENTITY` de Oracle. Los datos naturales que deben ser únicos (NIT del proveedor, nombre de usuario, nombres de departamento, categoría y rol) tienen restricción `UNIQUE`.
- **Suministro con llave propia** (`SCodigo`) y además `UNIQUE (PCodigo, PrCodigo)` para que un proveedor no pueda tener dos veces el mismo producto.
- **Proveedor activo/inactivo** (`PActivo`) para "dar de baja" un proveedor sin borrar su historial de suministros.
- **Bitácora con fecha y hora separadas**, tal como aparecen en el diagrama ER (`BFechaIngreso`, `BHoraIngreso`, `BFechaSalida`, `BHoraSalida`).

---

## 3. Diccionario de datos

Tomado del script `sql/01_crear_tablas.sql` (Oracle 12c o superior). Convenciones:

- **Llave:** `PK` = llave primaria, `FK` = llave foránea, `UQ` = único.
- **Nulo:** `No` = la columna es `NOT NULL`.
- Todas las llaves primarias son `NUMBER GENERATED BY DEFAULT AS IDENTITY`: Oracle genera el código automáticamente (1, 2, 3, ...) si no se envía uno.
- Las llaves foráneas tienen además un índice (`ix_...`) porque Oracle no los crea solo; esto acelera los JOIN y la verificación de integridad al borrar.

### 3.1 TDepartamento

Departamentos de Colombia donde se ubican las ciudades de los proveedores.

| Columna | Tipo | Nulo | Llave | Default | Restricción | Descripción |
|---------|------|:----:|:-----:|---------|-------------|-------------|
| DCodigo | NUMBER | No | PK | IDENTITY | `pk_departamento` | Código único del departamento. |
| DNombre | VARCHAR2(60) | No | UQ | — | `uq_departamento_nombre` | Nombre del departamento (p. ej. "Quindío"). No se puede repetir. |

### 3.2 TCiudad

Ciudades (municipios) donde están ubicados los proveedores. Cada ciudad pertenece a un departamento.

| Columna | Tipo | Nulo | Llave | Default | Restricción | Descripción |
|---------|------|:----:|:-----:|---------|-------------|-------------|
| CCodigo | NUMBER | No | PK | IDENTITY | `pk_ciudad` | Código único de la ciudad. |
| CNombre | VARCHAR2(60) | No | — | — | — | Nombre de la ciudad (p. ej. "Armenia"). Puede repetirse en departamentos distintos. |
| DCodigo | NUMBER | No | FK | — | `fk_ciudad_departamento` → `TDepartamento(DCodigo)`; índice `ix_ciudad_dep` | Departamento al que pertenece la ciudad. |

### 3.3 TCategoria

Categorías o familias en las que se clasifican los productos.

| Columna | Tipo | Nulo | Llave | Default | Restricción | Descripción |
|---------|------|:----:|:-----:|---------|-------------|-------------|
| CaCodigo | NUMBER | No | PK | IDENTITY | `pk_categoria` | Código único de la categoría. |
| CaNombre | VARCHAR2(60) | No | UQ | — | `uq_categoria_nombre` | Nombre de la categoría (p. ej. "Papelería"). No se puede repetir. |
| CaDescripcion | VARCHAR2(200) | Sí | — | — | — | Descripción de qué tipo de productos agrupa. |

### 3.4 TProveedor

Empresas a las que se les pueden hacer pedidos de productos.

| Columna | Tipo | Nulo | Llave | Default | Restricción | Descripción |
|---------|------|:----:|:-----:|---------|-------------|-------------|
| PCodigo | NUMBER | No | PK | IDENTITY | `pk_proveedor` | Código único del proveedor. |
| PNit | VARCHAR2(20) | No | UQ | — | `uq_proveedor_nit` | NIT de la empresa (p. ej. "900123456-7"). No se puede repetir. |
| PNombre | VARCHAR2(100) | No | — | — | — | Razón social del proveedor. |
| PDireccion | VARCHAR2(120) | Sí | — | — | — | Dirección física. |
| PCorreo | VARCHAR2(100) | Sí | — | — | — | Correo electrónico de contacto (la aplicación valida el formato). |
| PTelefono | VARCHAR2(20) | Sí | — | — | — | Teléfono de contacto. Es texto para admitir "+", espacios y guiones. |
| PActivo | NUMBER(1) | No | — | `1` | `ck_proveedor_activo`: `PActivo IN (0, 1)` | 1 = activo, 0 = inactivo. Solo a los activos se les pueden asignar productos. |
| CCodigo | NUMBER | No | FK | — | `fk_proveedor_ciudad` → `TCiudad(CCodigo)`; índice `ix_proveedor_ciu` | Ciudad donde está ubicado el proveedor. |

### 3.5 TProducto

Productos que la empresa puede pedir a los proveedores.

| Columna | Tipo | Nulo | Llave | Default | Restricción | Descripción |
|---------|------|:----:|:-----:|---------|-------------|-------------|
| PrCodigo | NUMBER | No | PK | IDENTITY | `pk_producto` | Código único del producto. |
| PrNombre | VARCHAR2(100) | No | — | — | — | Nombre del producto. |
| PrDescripcion | VARCHAR2(250) | Sí | — | — | — | Descripción o características. |
| PrPrecioBase | NUMBER(12,2) | No | — | — | `ck_producto_precio`: `PrPrecioBase >= 0` | Precio de referencia del producto (hasta 10 enteros y 2 decimales). |
| PrStock | NUMBER(8) | No | — | `0` | `ck_producto_stock`: `PrStock >= 0` | Unidades disponibles en inventario. |
| CaCodigo | NUMBER | No | FK | — | `fk_producto_categoria` → `TCategoria(CaCodigo)`; índice `ix_producto_cat` | Categoría a la que pertenece el producto. |

### 3.6 TSuministro

Entidad asociativa que resuelve la relación muchos a muchos entre Proveedor y Producto: indica qué productos ofrece cada proveedor, a qué precio y en cuántos días los entrega.

| Columna | Tipo | Nulo | Llave | Default | Restricción | Descripción |
|---------|------|:----:|:-----:|---------|-------------|-------------|
| SCodigo | NUMBER | No | PK | IDENTITY | `pk_suministro` | Código único del suministro. |
| SDiasEntrega | NUMBER(3) | No | — | — | `ck_suministro_dias`: `SDiasEntrega >= 0` | Días que tarda el proveedor en entregar el producto (0 a 999). |
| SPrecio | NUMBER(12,2) | No | — | — | `ck_suministro_precio`: `SPrecio >= 0` | Precio al que este proveedor vende este producto. |
| PCodigo | NUMBER | No | FK, UQ\* | — | `fk_suministro_proveedor` → `TProveedor(PCodigo)` | Proveedor que ofrece el producto. |
| PrCodigo | NUMBER | No | FK, UQ\* | — | `fk_suministro_producto` → `TProducto(PrCodigo)`; índice `ix_suministro_prod` | Producto ofrecido. |

\* `uq_suministro_prov_prod UNIQUE (PCodigo, PrCodigo)`: un proveedor no puede tener dos veces el mismo producto. Este índice único también sirve como índice de la llave foránea `PCodigo` (es su primera columna).

### 3.7 TRol

Tipos de usuario del sistema. Los códigos son fijos porque el índice del administrador y la clase `RolDTO` dependen de ellos.

| Columna | Tipo | Nulo | Llave | Default | Restricción | Descripción |
|---------|------|:----:|:-----:|---------|-------------|-------------|
| RCodigo | NUMBER | No | PK | IDENTITY | `pk_rol` | Código del rol: 1 = ADMINISTRADOR, 2 = TRADICIONAL, 3 = ESPORADICO. |
| RNombre | VARCHAR2(30) | No | UQ | — | `uq_rol_nombre` | Nombre del rol. |
| RDescripcion | VARCHAR2(150) | Sí | — | — | — | Qué puede hacer el rol. |

### 3.8 TUsuario

Usuarios que pueden ingresar a la aplicación.

| Columna | Tipo | Nulo | Llave | Default | Restricción | Descripción |
|---------|------|:----:|:-----:|---------|-------------|-------------|
| UCodigo | NUMBER | No | PK | IDENTITY | `pk_usuario` | Código único del usuario. |
| UNombreUsuario | VARCHAR2(30) | No | UQ | — | `uq_usuario_nombre` | Nombre con el que ingresa (login). La aplicación exige 4 a 30 caracteres sin espacios. |
| UContrasena | VARCHAR2(100) | No | — | — | — | **Hash SHA-256** de la contraseña en hexadecimal (64 caracteres). Nunca se guarda la clave en texto plano. |
| UActivo | NUMBER(1) | No | — | `1` | `ck_usuario_activo`: `UActivo IN (0, 1)` | 1 = puede ingresar, 0 = bloqueado. |
| UFechaCreacion | DATE | No | — | `SYSDATE` | — | Fecha y hora en que se creó el usuario. |
| UNombreCompleto | VARCHAR2(100) | No | — | — | — | Nombre real de la persona. |
| RCodigo | NUMBER | No | FK | — | `fk_usuario_rol` → `TRol(RCodigo)`; índice `ix_usuario_rol`; índice único `ux_usuario_unico_admin` | Rol del usuario. El índice único sobre `CASE WHEN RCodigo = 1 THEN 1 END` impide que exista más de un administrador. |

### 3.9 TBitacora

Registro de cada ingreso y salida de los usuarios del sistema.

| Columna | Tipo | Nulo | Llave | Default | Restricción | Descripción |
|---------|------|:----:|:-----:|---------|-------------|-------------|
| BCodigo | NUMBER | No | PK | IDENTITY | `pk_bitacora` | Código único del registro. |
| BFechaIngreso | DATE | No | — | `TRUNC(SYSDATE)` | `ck_bitacora_fecha_ing`: solo el día, sin hora | Fecha en que el usuario ingresó. |
| BHoraIngreso | VARCHAR2(8) | No | — | `TO_CHAR(SYSDATE, 'HH24:MI:SS')` | `ck_bitacora_hora_ing`: formato `HH24:MI:SS` válido (`REGEXP_LIKE`) | Hora de ingreso (p. ej. "14:05:09"). |
| BFechaSalida | DATE | Sí | — | — | `ck_bitacora_fecha_sal`: solo el día | Fecha de salida. `NULL` mientras la sesión sigue abierta. |
| BHoraSalida | VARCHAR2(8) | Sí | — | — | `ck_bitacora_hora_sal`: formato `HH24:MI:SS` válido | Hora de salida. `NULL` mientras la sesión sigue abierta. |
| UCodigo | NUMBER | No | FK | — | `fk_bitacora_usuario` → `TUsuario(UCodigo)`; índice `ix_bitacora_usu` | Usuario que ingresó. |

Restricciones adicionales de la tabla:

- `ck_bitacora_salida_completa`: la fecha y la hora de salida van juntas (ambas `NULL` o ambas con valor).
- `ck_bitacora_orden`: la salida no puede ser anterior al ingreso (si es el mismo día, la hora de salida debe ser mayor o igual; como la hora tiene formato fijo `HH24:MI:SS`, se puede comparar como texto).
