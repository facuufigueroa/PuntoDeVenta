# Ticket Bru-Yen

La plantilla está en `src/Reporte/ticket.jrxml`. El ancho sigue siendo 151 puntos, con una página de 612 puntos de alto y márgenes externos de cero, como en el reporte original.

El diseño usa blanco y negro, encabezado centrado, fecha y hora, columnas de producto e importe, cantidad de artículos, total destacado y mensaje de agradecimiento. Los nombres largos se expanden en varias líneas. No se agregaron datos de facturación o números de operación que el sistema no registra.

La aplicación carga `src/Reporte/ticket.jasper`, que debe regenerarse al modificar la plantilla. La versión incluida se compiló con JasperReports 6.0.0 y las expresiones se generaron para Java 8.

`tests/TicketReporteCheck.java` compila la plantilla, comprueba el ancho, los nombres largos y la paginación con 80 productos, y genera el `.jasper`, `docs/apariencia/ticket.png` y `docs/apariencia/ticket.pdf`. Para ejecutarlo se necesitan las dependencias JasperReports, Commons BeanUtils, Commons Collections, Commons Digester, Commons Logging, iText y iText PDF/A del proyecto.

La vista previa tiene datos de ejemplo. Queda comprobar la salida física con la impresora del negocio.
