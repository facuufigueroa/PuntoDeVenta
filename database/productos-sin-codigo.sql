-- Ejecutar sobre la base configurada. Conserva los nombres actuales como lista inicial.
CREATE TABLE IF NOT EXISTS producto_sin_codigo (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 nombre VARCHAR(255) NOT NULL UNIQUE,
 activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- IDs fijos: reimportar no reactiva ni renombra entradas ya administradas.
INSERT IGNORE INTO producto_sin_codigo (id,nombre) VALUES
 (1,'Carnes'),(2,'Pan'),(3,'Fiambre'),(4,'Frutas & Verduras'),
 (5,'Chorizo'),(6,'Chorizo Seco'),(7,'Morcilla'),(8,'Alimento');
