# Punto de venta

Sistema de escritorio Java Swing para ventas, catálogo, caja, historial, reimpresión de tickets, gráficos, reportes PDF y copias de seguridad.

## Instalar en otra PC

El paquete ZIP de `dist/` contiene el programa, sus bibliotecas y las instrucciones `INSTALACION.txt`. Requiere Java 8 o posterior y un servidor MySQL 8. Importá `database/instalar.sql` para una instalación nueva; luego ejecutá `INICIAR.cmd` y completá el asistente de conexión.

Desde **Configurar negocio** podés cambiar nombre, dirección, contacto, mensaje del ticket y logo. La identidad inicial muestra **LOGO EMPRESA / MI EMPRESA**. Los datos se guardan localmente en `config/`.

Para una base existente conservá la configuración e importá únicamente las migraciones pendientes: `caja.sql`, `ventas.sql` y `productos-sin-codigo.sql`. No importes `instalar.sql` sobre una instalación existente. Hacé un backup antes de actualizar.

## Generar el paquete

Con JDK 17 o posterior: `powershell -ExecutionPolicy Bypass -File tools/empaquetar.ps1`. También admite `-JdkHome`. El proyecto NetBeans usa bibliotecas relativas de `lib/`; la clase principal es `puntodeventa.Main`. Las dependencias seleccionadas están en `tools/runtime-libs.txt`.

El paquete no incluye credenciales, logo de clientes, productos reales ni backups. Las variables `PDV_DB_URL`, `PDV_DB_USER` y `PDV_DB_PASSWORD` tienen prioridad sobre `config/database.properties`.

## Documentación

- [Caja](docs/caja.md)
- [Historial y tickets](docs/historial-ventas.md)
- [Gráficos](docs/graficos-ventas.md)
- [Reporte de ventas](docs/reporte-ventas.md)
- [Productos sin código](docs/productos-sin-codigo.md)
- [Backups](docs/backups.md)
- [Distribución y alcance](docs/distribucion.md)

## Manual para el usuario

[Manual de usuario PDF](docs/Manual-de-usuario.pdf), de 12 páginas A5, y [versión para navegador](docs/Manual-de-usuario.html). Para imprimir como librito, usá la opción Folleto del lector PDF; para hojas individuales, seleccioná A5 o ajustar al papel disponible. El texto editable está en docs/manual-usuario.md y se regenera al empaquetar. El instalador agrega un acceso al manual en el menú Inicio.

