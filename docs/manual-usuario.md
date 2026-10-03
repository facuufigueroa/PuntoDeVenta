# PUNTO DE VENTA
## Manual de usuario
Una guía para el día a día de tu negocio.

VENTAS · CAJA · PRODUCTOS · REPORTES

Edición 1.0 | Octubre de 2026

Conservá este manual para consultarlo cuando lo necesites.

Este sistema permite registrar ventas, controlar caja y consultar resultados. El ticket que emite es un comprobante interno; no reemplaza una factura fiscal.

LOGO EMPRESA
---page---
# Antes de empezar
## Lo que necesitás
Una PC con Windows, Java 8 o posterior y acceso a un servidor MySQL 8. La impresora y el lector de códigos son opcionales: también podés escribir los códigos con el teclado.

El instalador instala el programa y sus bibliotecas. Java y MySQL se preparan por separado. Pedile al responsable de la instalación que deje la conexión y la base funcionando.

## Instalación inicial
1. Ejecutá el archivo Instalar-PuntoDeVenta y seguí las indicaciones.
2. Para una base nueva, el instalador técnico debe importar database/instalar.sql en MySQL.
3. Abrí Punto de Venta desde el acceso directo. Completá URL, usuario y contraseña de MySQL en el asistente.
4. Pulsá Probar conexión y después Guardar. La base debe tener todas las tablas requeridas.

Una base que ya contiene datos requiere una actualización preparada por el instalador técnico. No repitas la instalación de la base para resolver un error.

## En este manual
03 Configurar negocio | 04 Vender | 05 Caja
06 Productos | 07 Historial y tickets | 08 Reportes
09 Backups | 10 Teclado | 11 Ayuda | 12 Rutina diaria
---page---
# Configurar tu negocio
## Tu nombre y tu logo
1. Desde el menú principal, abrí Configurar negocio.
2. Completá nombre, dirección, contacto y mensaje del ticket.
3. Elegí un logo PNG o JPG, o quitá el logo para usar la imagen genérica.
4. Guardá los cambios.

Las pantallas muestran la identidad configurada y los comprobantes toman los datos del negocio. Sin personalización se muestra MI EMPRESA / LOGO EMPRESA.

## Dónde se guarda
Los datos se guardan en config/empresa.properties y el logo elegido se copia a config/empresa-logo.png, dentro de la carpeta instalada.

La ubicación predeterminada es %LOCALAPPDATA%\PuntoDeVenta. Podés pegar esa ruta en el Explorador de Windows. Si elegiste otra carpeta al instalar, buscá allí.

El backup de MySQL guarda la base de datos. Para conservar también el logo y las preferencias, respaldá por separado la carpeta config.

## Cómo funcionan las ventanas
Desde el menú principal, al abrir una pantalla se oculta el menú. Cerrá esa pantalla con la X para volver.

Las herramientas abiertas desde Ventas, como Ver precio, dejan Ventas visible de fondo. Al cerrarlas podés continuar con la compra.
---page---
# Hacer una venta
## Agregar los productos
1. Abrí Punto de venta. Si no hay caja abierta, aceptá Abrir caja e indicá el efectivo inicial.
2. Escaneá el código del producto o escribilo y presioná Enter.
3. Comprobá que aparezcan el nombre y el precio en el carrito.
4. Repetí el paso para cada artículo. Revisá el total antes de cobrar.

Para un producto sin código, elegí o escribí su nombre, ingresá el precio en pesos enteros positivos y pulsá Agregar. El precio se indica cada vez que lo agregás.

Para quitar un artículo, seleccioná su fila, pulsá Quitar producto y confirmá. Nueva compra permite descartar el carrito actual con confirmación.

## Cobrar
1. Elegí Efectivo, Tarjeta o Transferencia.
2. En efectivo, escribí en Paga con el dinero que recibís. Enter o Calcular vuelto muestra el cambio.
3. Pulsá Confirmar cobro, o F10, para guardar la venta.
4. Esperá la confirmación del sistema. El carrito se vacía cuando el cobro se guarda.
5. Pulsá Imprimir ticket si el cliente necesita un comprobante.

Calcular vuelto e imprimir no registran una venta. Para que quede guardada, usá Confirmar cobro.
---page---
# Controlar la caja
## Abrir y registrar movimientos
En Control de caja, pulsá Abrir caja e ingresá el fondo inicial: el efectivo que hay antes de comenzar a vender. Hay un único turno abierto compartido por el sistema.

Los cobros en efectivo aumentan el efectivo esperado. Tarjeta y transferencia se muestran por separado; no aumentan el dinero del cajón.

Usá Ingreso para agregar efectivo y Retiro para sacar efectivo, indicando el motivo. No se permite retirar más que el saldo disponible. Pulsá Actualizar para consultar los movimientos actuales.

## Cerrar el turno
1. Contá el efectivo real del cajón.
2. Pulsá Cerrar caja e ingresá el efectivo contado.
3. Revisá y confirmá el cierre.

La diferencia compara el efectivo contado con el esperado. Una diferencia negativa indica faltante; una positiva, sobrante. El historial conserva los cierres anteriores.

## Si quedó abierta desde ayer
El sistema avisa que hay una caja pendiente. Podés elegir Ir al cierre, Continuar turno o Cancelar.

Si necesitás atender a un cliente, Continuar turno permite vender en la caja que ya está abierta. Después podés contar el dinero y cerrar. La caja no se cierra automáticamente al terminar el día ni al cerrar el programa.
---page---
# Administrar productos
## Productos con código
Abrí Administración desde el menú principal.

- Agregar: completá código, nombre y precio; pulsá Agregar.
- Buscar: usá el código o el nombre para localizar un producto.
- Modificar: seleccioná una fila, pulsá Editar, cambiá los datos y pulsá Modificar.
- Eliminar: seleccioná una fila, pulsá Eliminar y confirmá solamente si corresponde.

Revisá el precio y el código antes de guardar. Los precios del catálogo se expresan en pesos enteros. No se puede registrar dos veces el mismo código.

## Productos sin código
Abrí Productos sin código desde el menú, Administración o Administrar sin código dentro de Ventas.

- Nuevo: ingresá un nombre y pulsá Guardar.
- Renombrar: seleccioná la entrada, cambiá el nombre y guardá.
- Desactivar: quita la opción de la lista de ventas sin borrarla.
- Activar: recupera una opción desactivada.
- Actualizar: vuelve a cargar la lista.

Estas opciones guardan nombres habituales. El importe se indica al agregarlas a cada venta. También podés escribir un nombre ocasional sin registrarlo como opción habitual.

Cambiar un nombre o un precio no modifica los tickets ya guardados. El sistema no administra existencias ni descuenta stock.
---page---
# Consultar ventas y tickets
## Buscar una venta
1. Abrí Historial de ventas desde el menú principal.
2. Elegí Desde y Hasta con los calendarios. No hace falta escribir las fechas.
3. Elegí un medio de pago o Todos, y pulsá Buscar.
4. Seleccioná una fila y pulsá Ver detalle para consultar los artículos y el estado.

Hoy vuelve al día actual. El período incluye ambas fechas. El resumen muestra importe bruto, anulado y neto.

## Reimprimir
Seleccioná la venta y pulsá Reimprimir ticket. Se usan los nombres y precios guardados al cobrar, aunque el catálogo haya cambiado. Reimprimir no registra otro cobro.

En Ventas, Imprimir ticket imprime el carrito actual o, si está vacío, el último cobro de esa ventana. Un carrito todavía no confirmado no es una venta registrada.

## Anular una venta
Seleccioná la venta, pulsá Anular venta, indicá el motivo y confirmá. Debe haber caja abierta. Para una devolución en efectivo, la caja debe tener saldo suficiente.

La venta queda conservada como anulada y se registra la devolución en el turno actual. No se alteran cierres anteriores. Las ventas anuladas no se reimprimen como comprobantes vigentes.

Si se pagó con tarjeta o transferencia, la devolución real debe realizarse por el medio correspondiente fuera del sistema.
---page---
# Ver gráficos y reportes
## Gráficos de ventas
Desde Historial de ventas, pulsá Gráficos de ventas. Elegí las fechas y pulsá Actualizar. Los gráficos incluyen todos los medios de pago del período.

- Medios de pago: compara importes de efectivo, tarjeta y transferencia.
- Productos: muestra los cinco productos que más facturaron y agrupa los demás.
- Resumen: cantidad de ventas confirmadas, total, ticket promedio y anulaciones.

La torta de productos compara dinero vendido, no unidades ni ganancias. Las ventas anuladas se informan aparte. Si no hay ventas se muestra Sin ventas.

## Imprimir un reporte
1. En Historial de ventas, elegí las fechas y el medio de pago.
2. Pulsá Reporte de ventas. No hace falta seleccionar una venta.
3. En la vista previa, usá el botón de impresora para elegir impresora y páginas.
4. Usá el botón de guardar para exportar a PDF.

El reporte A4 incluye ventas, estado, importes y totales del período. Si ocupa varias páginas, las numera automáticamente.

Las anulaciones se descuentan del período de la venta original en el historial y los reportes. En caja, la devolución se refleja en el turno en que se realiza. Por eso pueden corresponder a días diferentes.
---page---
# Guardar copias de seguridad
## Preparar las copias
1. Abrí Copias de seguridad desde el menú.
2. Elegí la carpeta de destino.
3. Revisá la ubicación del programa mysqldump de MySQL. Si no se detectó, pedile ayuda al instalador técnico para seleccionarlo.
4. Elegí si querés mantener la programación automática y pulsá Guardar configuración.
5. Pulsá Hacer backup ahora y esperá el resultado correcto.

Los archivos de respaldo terminados tienen extensión .sql. Un archivo .part todavía no es una copia completa.

## Copias automáticas
El programa procura hacer una copia por semana, tomando el lunes como referencia. Si ese día está cerrado, hace la pendiente al volver a abrir el sistema. La primera ejecución puede crear una copia inicial aunque no sea lunes.

El sistema debe estar abierto y la PC encendida. Si falla una copia, se reintenta mientras el programa está abierto. No se borran copias anteriores automáticamente.

## Proteger y recuperar
Conservá otra copia fuera de la PC del negocio: una falla del disco puede afectar tanto al sistema como a los respaldos que estén en él.

El backup incluye productos, ventas y caja de la base configurada. Respaldá además config para conservar logo y preferencias.

Para recuperar los datos, pedile al responsable técnico que restaure el archivo correcto. Probá la recuperación en una base de prueba antes de necesitarla; no importes una copia sobre la base activa para comprobarla.
---page---
# Atajos de teclado
## En la pantalla de Ventas
F1 - Mostrar ayuda del teclado.
F2 - Nueva compra, con confirmación.
F3 - Ir a Paga con y seleccionar el importe.
F4 - Abrir Ver precio.
F5 - Volver al campo de código.
F6 - Ir a productos sin código.
F7 - Quitar el artículo seleccionado, con confirmación.
F8 - Calcular vuelto.
F9 - Imprimir ticket.
F10 - Confirmar cobro.

## Teclas útiles
Enter en código - Agregar el producto.
Enter en Paga con - Calcular vuelto.
Enter en precio sin código - Agregar el artículo.
Tab / Shift + Tab - Avanzar o retroceder entre campos.
Esc en Ver precio - Cerrar la consulta.

En las confirmaciones de Nueva compra y Quitar producto, Enter acepta y Esc cancela. Si seleccionaste Cancelar con Tab, Enter cancela. Cerrar ese diálogo con la X también cancela.

En las fechas del historial, Espacio o Alt + flecha abajo abre el calendario.

En algunas notebooks necesitás mantener Fn junto con la tecla F correspondiente.
---page---
# Si algo no funciona
## El programa no abre
Comprobá que Java esté instalado y usá el acceso directo. Si usás la versión ZIP, extraé todo antes de abrirla: el JAR necesita la carpeta lib. Consultá al instalador si el problema continúa.

## No conecta o no carga datos
Comprobá que MySQL esté funcionando. Si la base está en otra PC, verificá que esa PC y la red estén disponibles. Revisá la conexión desde Configurar negocio > Conexión MySQL. Los cambios se aplican al reiniciar.

## No encuentra un producto
Verificá el código o consultá Administración. Si no está registrado, cargalo antes de venderlo o agregalo como producto sin código con su nombre e importe.

## El ticket no sale
Comprobá que la impresora esté encendida, conectada y seleccionada. Revisá papel y cola de impresión. Si el cobro ya se confirmó, reimprimí desde el historial: no cobres de nuevo para obtener un ticket.

## No permite reimprimir un cobro antiguo
Algunos cobros antiguos no conservan un detalle de artículos verificable. Consultá Ver detalle; el sistema no inventa productos ni precios faltantes.

## Hubo un error al cobrar o anular
Buscá la operación en el historial y actualizá caja antes de repetirla. Una interrupción puede impedir ver la confirmación aunque el guardado ya se haya realizado. No descartes el carrito ni reinicies sin revisar.
---page---
# Tu rutina diaria
## Al comenzar
1. Abrí el sistema y comprobá la conexión.
2. Si hay un turno de ayer, decidí si continuás o lo cerrás.
3. Si no hay caja abierta, abrí una con el efectivo inicial.
4. Comprobá la impresora antes de atender.

## Con cada cliente
1. Agregá los artículos y revisá el total.
2. Elegí el medio de pago.
3. En efectivo, indicá lo recibido y revisá el vuelto.
4. Confirmá el cobro y esperá el resultado.
5. Entregá el ticket si corresponde.

## Al terminar
1. Registrá los ingresos y retiros pendientes.
2. Contá el efectivo real y cerrá caja.
3. Consultá el historial o guardá el reporte del día.
4. Comprobá el último backup o hacé uno manual.
5. Cerrá el programa.

## Datos de tu instalación
Negocio: ____________________________________
Responsable técnico: __________________________
Contacto de soporte: __________________________
Carpeta de backups: ___________________________

No anotes contraseñas en este manual.
