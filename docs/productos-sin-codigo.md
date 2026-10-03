# Administrar productos sin código

Importá `database/productos-sin-codigo.sql` en la base configurada. Agrega una tabla independiente y carga las opciones iniciales: Carnes, Pan, Fiambre, Frutas & Verduras, Chorizo, Chorizo Seco, Morcilla y Alimento. Reimportar el script conserva sus nombres y estados modificados; no modifica productos con código, ventas ni tickets.

Abrí **Productos sin código** desde la tarjeta **Productos** del menú, desde Administración o desde **Administrar sin código** en ventas.

- Para agregar, pulsá **Nuevo**, ingresá el nombre y **Guardar**.
- Para renombrar, seleccioná la entrada, cambiá el nombre y **Guardar**.
- **Desactivar** quita la opción de la lista rápida sin borrar la entrada. Para recuperarla, seleccioná la fila inactiva y pulsá **Activar**.
- **Actualizar** vuelve a consultar la base. No se permiten nombres vacíos, duplicados ni mayores a 255 caracteres.

La lista de ventas se actualiza al guardar y cuando la ventana de ventas vuelve a estar activa, sin reiniciar el sistema. El precio sigue ingresándose al agregar el producto al carrito, en pesos enteros positivos. También podés escribir un nombre ocasional en ventas sin guardarlo como opción habitual.

Renombrar o desactivar una opción no modifica los productos de un carrito existente ni los tickets ya guardados: conservan el nombre y precio con los que se vendieron. Las opciones administradas se incluyen en los backups de la base.
