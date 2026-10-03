CREATE DATABASE IF NOT EXISTS `punto_venta` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `punto_venta`;

CREATE TABLE IF NOT EXISTS `producto` (
  `idproducto` int NOT NULL AUTO_INCREMENT,
  `codigo` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `precio` int NOT NULL,
  PRIMARY KEY (`idproducto`),
  UNIQUE KEY `codigo_UNIQUE` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Ejecutar una vez sobre la base existente. No modifica productos.
CREATE TABLE IF NOT EXISTS caja_control (id INT PRIMARY KEY) ENGINE=InnoDB;
INSERT IGNORE INTO caja_control (id) VALUES (1);
CREATE TABLE IF NOT EXISTS caja_sesion (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 apertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 cierre TIMESTAMP NULL DEFAULT NULL,
 fondo DECIMAL(14,2) NOT NULL,
 esperado DECIMAL(14,2) NULL,
 contado DECIMAL(14,2) NULL
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS caja_movimiento (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 sesion BIGINT NOT NULL,
 fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 tipo VARCHAR(10) NOT NULL,
 medio VARCHAR(20) NOT NULL,
 monto DECIMAL(14,2) NOT NULL,
 detalle TEXT NOT NULL,
 FOREIGN KEY (sesion) REFERENCES caja_sesion(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Ejecutar después de caja.sql en la base configurada.
CREATE TABLE IF NOT EXISTS venta (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 movimiento BIGINT NOT NULL UNIQUE,
 solicitud VARCHAR(36) NOT NULL UNIQUE,
 FOREIGN KEY (movimiento) REFERENCES caja_movimiento(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS venta_item (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 venta BIGINT NOT NULL,
 nombre VARCHAR(255) NOT NULL,
 precio DECIMAL(14,2) NOT NULL,
 FOREIGN KEY (venta) REFERENCES venta(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS venta_anulacion (
 movimiento BIGINT PRIMARY KEY,
 ajuste BIGINT NOT NULL UNIQUE,
 fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 motivo VARCHAR(1000) NOT NULL,
 FOREIGN KEY (movimiento) REFERENCES caja_movimiento(id),
 FOREIGN KEY (ajuste) REFERENCES caja_movimiento(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
