# Reporte de ventas

En **Historial de ventas**, elegí **Desde**, **Hasta** y el medio de pago, y pulsá **Reporte de ventas**. El reporte consulta nuevamente la base con esos filtros; no requiere seleccionar una venta ni pulsar Buscar primero.

Se abre una vista previa JasperReports en formato A4 con:

- Período consultado, medio de pago y fecha de generación.
- Número de venta, fecha y hora, caja, medio de pago, estado e importe.
- Cantidad de ventas confirmadas y anuladas.
- Totales bruto, anulado y neto, y ventas confirmadas por medio de pago.

Usá el botón de **impresora** del visor para elegir impresora y páginas. El botón de **guardar** permite exportar a PDF. Cerrar la vista previa no cierra la aplicación. Si el rango no contiene ventas, se genera un reporte con el aviso y totales en cero.

Las ventas anuladas se conservan en el listado y se descuentan del neto del período original de venta, como en el historial. No se cuentan como ventas confirmadas por medio de pago. Los períodos largos se dividen en páginas con encabezado y número de página.

La plantilla editable está en `src/Reporte/ventas.jrxml`; la aplicación utiliza `src/Reporte/ventas.jasper`, compilada para Java 8 con JasperReports 6.21.5. `tests/VentasReporteCheck.java` recompila la plantilla y valida filtros, anulaciones, importes, período vacío, paginación y exportación a PDF con datos de ejemplo, sin conectarse a la base del negocio.
