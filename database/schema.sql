CREATE DATABASE IF NOT EXISTS `bru-yen` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `bru-yen`;

CREATE TABLE IF NOT EXISTS `producto` (
  `idproducto` int NOT NULL AUTO_INCREMENT,
  `codigo` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `precio` int NOT NULL,
  PRIMARY KEY (`idproducto`),
  UNIQUE KEY `codigo_UNIQUE` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
