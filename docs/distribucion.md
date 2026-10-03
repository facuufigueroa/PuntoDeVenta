# Distribución

El paquete se genera con una lista explícita de bibliotecas y una base vacía. No contiene datos ni credenciales de la instalación de desarrollo. Los archivos de configuración del cliente deben conservarse en las actualizaciones.

JasperReports 6.21.5 utiliza OpenPDF para exportar PDF. El acceso a MySQL utiliza MariaDB Connector/J 3.5.10; se aceptan las URL MySQL existentes mediante conversión interna de esquema. No se distribuyen iText 5 ni MySQL Connector/J en el paquete.

Las licencias y avisos originales están en `lib/licencias/`; el código fuente correspondiente de los componentes LGPL se entrega en `lib/sources/`. Conservá estos archivos al redistribuir. Consultá las condiciones de cada dependencia antes de definir tu contrato y licencia comercial del producto. Fuentes: [JasperReports](https://github.com/Jaspersoft/jasperreports/tree/6.21.5), [MariaDB Connector/J](https://mariadb.com/docs/connectors/mariadb-connector-j/about-mariadb-connector-j), [OpenPDF](https://github.com/LibrePDF/OpenPDF).

El programa administra ventas y caja. No implementa facturación fiscal, gestión de stock, roles de usuario ni cobros electrónicos. El ticket es un comprobante interno. Antes de entregarlo a un cliente comprobá la conexión a su MySQL, una venta, reimpresión, reporte, cierre y restauración de backups con su impresora. La base inicial no incluye un catálogo cargado.

## Instalador Windows

Con el paquete generado, ejecutá tools/instalador.ps1 indicando -Makensis si NSIS está en otra ubicación. Instala por usuario, crea accesos directos y registra el desinstalador. Conserva config y backups al desinstalar; la base MySQL no se modifica. No instala Java ni MySQL automáticamente. El EXE no tiene firma digital.

