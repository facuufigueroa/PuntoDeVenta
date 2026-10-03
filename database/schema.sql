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
