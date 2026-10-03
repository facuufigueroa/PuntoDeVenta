# Gráficos de ventas

En **Historial de ventas**, pulsá **Gráficos de ventas**. La pantalla toma el rango de fechas indicado en el historial y permite cambiarlo con calendarios desplegables, o volver a **Hoy**. Podés navegar por meses y cambiar el año sin escribir fechas. Los gráficos incluyen todos los medios de pago del período.

- **Ventas por medio de pago:** muestra el importe confirmado en efectivo, tarjeta y transferencia.
- **Productos por importe vendido:** muestra los cinco productos que más facturaron y agrupa el resto. Se compara facturación, no cantidad de unidades ni ganancia.
- **Resumen:** cantidad de ventas confirmadas, importe total, ticket promedio y cantidad e importe de ventas anuladas.

La leyenda muestra importes y porcentajes. Las ventas anuladas se excluyen de ambas tortas y se informan aparte. Los importes corresponden al período de la venta original, como en el historial; no al turno de devolución.

Los productos se leen con el nombre y precio guardados al vender. Para cobros antiguos, se recuperan del detalle cuando sus líneas y total se pueden verificar. El importe que no puede atribuirse a productos aparece como **Sin detalle de productos**, para que ambas tortas concilien.

Un período sin ventas muestra **Sin ventas**, sin porcentajes inventados. **Actualizar** consulta nuevamente la base. Los calendarios utilizan JCalendar, incluido en `lib/jcalendar-1.4.jar` y configurado en el proyecto NetBeans. No requiere otra migración: se utilizan las tablas de `caja.sql` y `ventas.sql`.
