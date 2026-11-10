**UNIVERSIDAD DEL QUINDIO**  
**PROGRAMA DE INGENIERÍA DE SISTEMAS Y COMPUTACIÓN**  
**BASES DE DATOS I**

**ENUNCIADO PROYECTO FINAL**

Las diversas empresas de carácter comercial requieren de sistemas de información que les permita determinar los proveedores a los que se pueden realizar los pedidos de sus productos. Los proveedores pueden estar en cualquier ciudad de Colombia y, es necesario almacenar los datos de estos. Así mismo, es fundamental poder identificar los productos, con sus datos, que cada proveedor puede enviar. Es importante determinar que, un Proveedor puede proveer varios productos y, que un Producto puede ser adquirido de varios proveedores. Los productos se pueden clasificar en Categorías, lo cual podrá identificar las familias de Productos. De las Categorías, también es importante almacenar sus datos básicos.

La aplicación deberá contar con un sistema básico de seguridad que consiste en un login de usuarios. Habrán tres tipos de usuarios: El Administrador (solo 1), los usuarios tradicionales y los usuarios esporádicos. Solo el Administrador puede crear nuevos usuarios. Los usuarios tradicionales pueden ejecutar los CRUD para la gestión de datos. Los usuarios esporádicos solo podrán ejecutar las consultas. También, se contará con una Bitácora que almacenará los datos del ingreso y la salida de los usuarios del sistema (fechas y horas de entrada y salida).

NOTA: La aplicación que se creará será de escritorio y NO se van a utilizar frameworks para nada. 

**Consultas y reportes mínimos que debe tener la aplicación.**

1) **Consultas**

- **Se deben crear tres consultas con JOIN que junten al menos, datos de 3 tablas.**  
- **Se deben construir y mostrar los datos arrojados por dos consultas que posean subconsultas y que relacionen dos tablas.**  
    
2) **Reportes (Archivo en .pdf o en una herramienta de reportes mostrado dentro de un formulario en la aplicación)**

- Se deben tener en reportes (TRES diferentes) los datos filtrados por el usuario, de productos y proveedores.  
- Se deben tener dos reportes que relacionen los Productos con sus Categorías y que los pueda filtrar el usuario. 

**ENTREGABLES**

1. Documento conteniendo:  
- Diagrama Entidad relación  
- Diccionario de datos de todas las tablas

| Diccionario de datos tabla Producto |  |  |  |
| :---- | :---- | :---- | :---- |
| Atributo | Descripcion | Tipo de dato | Observaciones |
| codigo P | Es el codigo que identificara cada producto | char de 3 caracteres | es la llave principal de la tabla prodcuto |
| nombre p |  |  |  |
|  |  |  |  |

- Diagrama relacional de tablas normalizado  
- Explicación general de la aplicación  
2. Aplicación funcionando con las consultas y reportes  
- Debe contar con el módulo de ingreso (usuarios)  
- Los módulos de Entidades, Transacciones, Consultas y Reportes, Auditoría (Gestión de usuarios y la Bitácora de Entrada y Salida)