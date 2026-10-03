# Control de caja

Para habilitar el módulo, ejecutá `database/caja.sql` y después `database/ventas.sql` sobre la base configurada. Los scripts agregan tablas sin modificar productos. En instalaciones nuevas, importá primero `database/schema.sql`. Los backups de la base incluyen estas tablas.

1. Entrá a **Control de caja** desde el menú y abrí un turno indicando el efectivo inicial. Solo se permite un turno abierto para todo el sistema; se conserva al reiniciar.
2. En ventas, elegí **Efectivo**, **Tarjeta** o **Transferencia** y pulsá **Confirmar cobro**. Para efectivo, completá **Paga con**. Se registra el total de la compra, descontando implícitamente el vuelto, y se vacía el carrito cuando la base confirma el guardado. Sin caja abierta no se puede confirmar un cobro.
3. **Imprimir ticket** permite imprimir el carrito actual o, cuando está vacío, el último cobro de esa ventana. Imprimir o calcular vuelto no registra un cobro. **Nueva compra** descarta el carrito con confirmación y tampoco cobra.
4. Usá **Ingreso** y **Retiro** para movimientos de efectivo con motivo. No se permite retirar más efectivo que el disponible. Tarjeta y transferencia se muestran por separado y no modifican el efectivo esperado.
5. Pulsá **Actualizar** para consultar los movimientos y totales actuales. Al cerrar, ingresá el efectivo contado y confirmá. El historial conserva fondo, efectivo esperado, contado y diferencia: un valor negativo indica faltante y uno positivo, sobrante. Se muestran los últimos 100 turnos.

Los importes manuales admiten coma o punto decimal, hasta dos decimales y sin separadores de miles. El catálogo conserva sus precios actuales en pesos enteros.

**F10** confirma el cobro; **F9** imprime el ticket.

Al iniciar el sistema, si quedó una caja abierta de un día anterior aparece **Caja pendiente de cierre**. Podés elegir **Ir al cierre**, **Continuar turno** o **Cancelar**. Ir al cierre abre Control de caja; cerrá contando el efectivo real y abrí un nuevo turno. No se cierra automáticamente ni se inventa el efectivo contado.

Al entrar al punto de venta, si no hay caja abierta, se ofrece **Abrir caja** e ingresar el fondo inicial. Al confirmar la apertura se abre ventas automáticamente. Si venís desde un aviso pendiente, cerrar la caja anterior y abrir una nueva también te lleva a ventas. Cancelar conserva el carrito y no registra cobros.

La comprobación se repite antes de confirmar cada cobro, por si la ventana quedó abierta durante la noche o se cerró la caja desde otra ventana. Si elegís continuar un turno anterior, esa decisión se recuerda para esa caja durante el día actual; al día siguiente vuelve a avisar. Un error de conexión no se interpreta como caja cerrada.

Si la conexión falla durante una operación, actualizá caja y revisá los movimientos antes de repetirla. Una interrupción de red puede impedir recibir la confirmación de una operación guardada. El último ticket se conserva en memoria hasta cerrar la aplicación; el registro de caja persiste en la base.
