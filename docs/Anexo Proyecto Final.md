**UNIVERSIDAD DEL QUINDIO**  
**PROGRAMA DE INGENIERÍA DE SISTEMAS Y COMPUTACIÓN**  
**BASES DE DATOS I**

**ANEXO PROYECTO FINAL**

**Propósitos del Anexo del Proyecto final.**

El objetivo de este anexo es guiar a los estudiantes en el desarrollo de las etapas del proyecto final.

**Herramientas requeridas**

Dependiendo del Lenguaje de programación con el que debe trabajar, se recomienda:

1. **Entorno de Desarrollo**: Cada grupo puede trabajar con el entorno de desarrollo que se sienta más cómodo; no obstante se recomiendan:  
   - PYTHON: Visual studio code  
   - JAVA: Netbeans, Eclipse  
   - C\#: [Sharpdevelop](https://sourceforge.net/projects/sharpdevelop/), Visual Studio (No code) de Microsoft  
       
2. **Diseñador de GUI (Ventanas)**: Algunos IDE ya traen el plug\_in para crear las ventanas pero, otros NO lo traen, por lo que es necesario construir las ventanas con otra herramienta.  
- PYTHON: Se recomienda usar QT DESIGNER o [wxformbuilder](https://github.com/wxFormBuilder/wxFormBuilder/releases) para crear la GUI. **wxglade** es otra alternativa. O con I.A.  
- JAVA: Con Netbeans ya viene el plug\_in para crear las ventanas. Con Eclipse, hay que instalar un plug\_in como windows builder.  
- C\#: El entorno de desarrollo visual studio ya trae el plug\_in para crear las ventanas. (win\_forms, wpf)

	**NOTA: No se pueden usar frameworks**

3. **Herramienta para hacer Reportes**: Existen varias dependiendo del lenguaje.  
     
- **PYTHON**: No conozco una herramienta directa para hacer los reportes. Existen Bibliotecas que facilitan la creación de archivos pdf que se redirigen a la impresora y, se crea un visor dentro de la aplicación.

		

- **JAVA**: Existe el ireport de JaspertReport. En Eclipse existe BIRT que es como un plug in.

		  
		

- **C\#**:  Se puede usar el reportviewer de sharpdevelop o de Visual studio(no code)  
- Existe una herramienta para crear reportes llamada **Crystal reports**. En teoría se conecta a cualquier lenguaje de programación.

		  
**Desarrollo del Proyecto**

1. **Etapa 1\. Construcción de la interfaz Gráfica**  
     
   Use la herramienta que desee para construir la interfaz gráfica. Se recomiendan los IDE o entornos que aparecen arriba.  
   La interfaz gráfica debe tener una ventana de menú principal que incluye las opciones: *Entidades, Transacciones, Reportes/Consultas, Auditoria.*  
     
   *![][image1]*  
     
     
     
2. **Etapa 2: Construcción del código.**

Es importante tener en cuenta la conexión de la base de datos y el lenguaje de programación. En este sentido, se tienen seis combinaciones:

- Python con Oracle  
- Python con sql server  
- Java con Oracle  
- Java con sql server  
- C\# con Oracle  
- C\# con sql server

Posteriormente se darán ayudas para realizar las conexiones.

	Para cada entidad y transacción, es necesario realizar el CRUD utilizando las instrucciones ***SELECT, UPDATE, DELETE E INSERT***.

**NOTA: No se pueden usar frameworks**

**Recomendación:** 

Aunque no es Ingeniería de software directamente, es ideal utilizar conceptos provistos por ella como, el patrón modelo vista controlador, la orientación a objetos y la separación de clases. Puede crear una clase llamada **Nombre\_Clase\_DAO, DTO** que contiene los métodos para agregar, consultar, editar y borrar. También deben estar las clases que representan las tablas y, que crean los objetos antes de gestionarlos en la base de datos.

3. **Etapa 3: Construcción de los reportes / Informes.**

	**PYTHON**

Clic aquí: [36 Python. Crear archivo PDF con reportlab (youtube.com)](https://www.youtube.com/watch?v=XXAfMylMhGg)

**JAVA**

Clic aqui: [Como crear reportes con iReport y Netbeans (youtube.com)](https://www.youtube.com/watch?v=SvHWBFrLhPs)

Clic aqui: [Como hacer un reporte con Birt (youtube.com)](https://www.youtube.com/watch?v=QpD3NWFl2VY)

Clic aquí: [Crear Archivos PDF en Java , usando PDFBox (youtube.com)](https://www.youtube.com/watch?v=XAFA3IQXTK0&t=139s)

**C\# (SHARP)**

Clic aquí: [(3) Como hacer REPORTES en C\# \| CREAR REPORTES CON REPORTVIEWER C\# \- YouTube](https://www.youtube.com/watch?v=sa6kWjzXBD8) (En visual Studio)

Clic aquí: [como hacer reportes con reportviewer con parámetros en c\# 2022 (youtube.com)](https://www.youtube.com/watch?v=9lWHU6nP9BA)

Clic aquí: [Creando PDFs con C\# de una Manera Sencilla (2 ejemplos) (youtube.com)](https://www.youtube.com/watch?v=T5M-b2xXLGw)

[image1]: <data:image/png;base64,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>