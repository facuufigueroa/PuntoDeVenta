# Punto de venta Bru-Yen

Aplicación de escritorio Java Swing con catálogo de productos, venta por código de barras, consulta de precios, tickets JasperReports y backups semanales.

## Configuración

1. Abrí el proyecto en NetBeans con JDK 8 o posterior.
2. Configurá las bibliotecas de MySQL y JasperReports 6.0.0. Las referencias actuales de `nbproject/project.properties` corresponden al entorno de desarrollo y pueden requerir ajustes en otra PC.
3. Para una instalación nueva, importá `database/schema.sql` en MySQL. Solo contiene la estructura, sin productos reales.
4. Copiá `config/database.properties.example` a `config/database.properties` y completá las credenciales locales. También se admiten las variables `BRUYEN_DB_URL`, `BRUYEN_DB_USER` y `BRUYEN_DB_PASSWORD`, que tienen prioridad sobre el archivo.
5. Ejecutá `puntodeventa.bruyen` desde la carpeta del proyecto. La configuración y los backups se resuelven respecto de la carpeta desde la que se inicia la aplicación.

## Funciones y documentación

- [Atajos de teclado](docs/atajos.md)
- [Copias de seguridad](docs/backups.md)
- [Ticket de supermercado](docs/reporte.md)
- [Vistas previas](docs/apariencia)

El módulo de backups comprueba la copia semanal al abrir el sistema y cada 30 minutos mientras permanece abierto. Recupera la copia pendiente cuando un lunes el negocio está cerrado.

Las credenciales, los backups, las exportaciones reales de la base, los archivos privados de NetBeans y las carpetas de compilación se excluyen de Git.
