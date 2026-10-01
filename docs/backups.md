# Copias de seguridad

Abrí **Copias de seguridad** desde el menú principal. Allí podés elegir la carpeta, activar o desactivar la programación y hacer una copia manual. El programa detecta `mysqldump.exe` en las instalaciones habituales de MySQL para Windows; si está en otra ubicación, elegilo desde la pantalla.

## Lunes y feriados

La programación viene activada. Se comprueba al abrir el sistema y cada 30 minutos mientras está abierto. Se hace una copia por semana, tomando el lunes como inicio. La primera ejecución genera una copia inicial, aunque ese día no sea lunes.

Si el lunes el negocio está cerrado, la copia pendiente se ejecuta al volver a abrir el sistema. No se necesita un calendario de feriados. Si MySQL está apagado o falla la copia, la semana no se marca como completada y se reintenta. Una copia manual correcta también cubre la semana actual.

**El módulo no ejecuta tareas con la aplicación cerrada ni con la PC apagada.** No instala tareas de Windows. Si pasan varias semanas sin usar el sistema, al volver se crea una copia del estado actual; no puede recuperar estados de semanas anteriores.

## Archivos y estado

Por defecto las copias están en `backups`, junto a la carpeta desde donde se inicia el sistema. Se pueden guardar en otra carpeta o unidad desde la pantalla. Cada archivo lleva fecha, hora y un identificador para evitar sobrescrituras. No se eliminan copias anteriores automáticamente.

La configuración y el registro de la última copia están en `config/backup.properties`, respecto de esa misma carpeta de inicio. Es un archivo local de cada instalación: no hace falta copiar el de tu PC a la de tu amigo. No contiene contraseñas.

Se respalda la base configurada en `ConexionBD`, mediante `mysqldump`, incluyendo estructura, datos, rutinas, eventos y triggers. Se usa una transacción para las tablas InnoDB. Durante la copia no se debe cambiar la estructura de las tablas. [Documentación de MySQL](https://dev.mysql.com/doc/refman/8.0/en/mysqldump.html).

El archivo se genera primero como `.part`. Solo se publica como `.sql` después de comprobar el resultado del proceso, el tamaño y la marca de finalización. Las credenciales se pasan mediante un archivo temporal con acceso limitado al propietario, que se elimina al finalizar. Hay un límite de 10 minutos por intento y un bloqueo para evitar dos copias simultáneas del mismo módulo.

## Recuperación y pruebas

Guardá también una copia fuera de la PC del negocio. Para comprobar que se puede recuperar, importá un backup en una instancia de MySQL de prueba. El dump incluye `CREATE DATABASE` y `USE` con el nombre de la base original: no lo importes sobre la base activa para hacer una prueba.

`tests/BackupServiceCheck.java` verifica lunes, recuperación después de un cierre, cambio de año, fallos, reintentos, desactivación y persistencia. Se ejecuta sin conectar a MySQL. Con `--real-backup` también crea una copia real, sin restaurar ni modificar la base original.
