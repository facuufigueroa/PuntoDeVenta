# Historial de ventas

Importá `database/ventas.sql` después de `database/caja.sql` en la base configurada. Las tablas nuevas guardan los productos de cada compra y las anulaciones; no modifican productos ni borran movimientos anteriores.

Desde la tarjeta **Nueva venta** del menú, abrí **Historial de ventas**. Inicialmente muestra el día actual. Hacé clic en **Desde** o **Hasta**, o en su botón de calendario, y elegí el día. Podés navegar por meses y cambiar el año directamente dentro del calendario. No hace falta escribir fechas. Ambas fechas se incluyen en la búsqueda. Elegí un medio de pago o **Todos**, y pulsá **Buscar**. **Hoy** restablece el día actual y todos los medios. Con foco en la fecha, **Espacio** o **Alt + flecha abajo** abre el calendario.

**Gráficos de ventas** abre tortas por medio de pago y por facturación de productos, con totales y filtro de fechas. Consultá [la documentación de gráficos](graficos-ventas.md).

**Reporte de ventas** abre una vista previa A4 del listado y sus totales, respetando fechas y medio de pago. Desde el visor se imprime o se guarda como PDF. Consultá [la documentación del reporte](reporte-ventas.md).

El resumen muestra el importe bruto de las ventas del período, el importe anulado y el neto. Las anulaciones se descuentan del período de la venta original, aunque se hayan hecho otro día. Caja refleja la devolución en el turno en que se registra.

Seleccioná una venta para ver sus productos, total, medio de pago y estado. **Reimprimir ticket** recupera los nombres y precios guardados al cobrar; no depende del catálogo actual ni registra un nuevo cobro. Las ventas anuladas se consultan con su motivo, pero no se reimprimen como tickets de venta vigentes.

**Anular venta** exige motivo y confirmación. Conserva la venta y los productos, y registra un movimiento `REVERSO` por el mismo importe y medio en la caja abierta. No modifica cierres anteriores. Para devolver efectivo, la caja debe tener saldo suficiente. Tarjeta y transferencia ajustan sus totales sin modificar el efectivo; la devolución real al cliente por esos medios se realiza fuera de este sistema. Una venta solo se anula una vez.

Los cobros anteriores también permiten reimprimir cuando su detalle original conserva una línea por producto con el formato `Nombre · $precio` y la suma coincide con el total guardado. La recuperación no modifica la base ni registra otro cobro. Si faltan precios, alguna línea es inválida o la suma no coincide, se muestra el detalle original sin habilitar la reimpresión.

Los nuevos cobros guardan movimiento, venta y productos en una única transacción. Si se pierde la respuesta, reintentar el mismo carrito en la misma ventana usa la misma solicitud para evitar duplicar el cobro. Antes de cambiar el carrito, descartar la compra o reiniciar tras un error de conexión, revisá el historial: la base podría haber guardado la venta. Después de un error de anulación, buscá otra vez antes de reintentar.

Verificación aislada: `tests/HistorialVentasCheck.java` utiliza HSQLDB en memoria, sin conectarse a la base del negocio. Comprueba filtros, productos, reintentos, importes, devoluciones, cierres anteriores y rollback de ventas incompletas.
