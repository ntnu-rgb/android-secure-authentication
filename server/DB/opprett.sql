-- phpMyAdmin SQL Dump
-- version 4.7.4
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: 04. Mar, 2018 17:11 PM
-- Server-versjon: 10.1.29-MariaDB
-- PHP Version: 7.2.0

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `sfa`
--

-- --------------------------------------------------------

--
-- Tabellstruktur for tabell `bruker`
--

CREATE TABLE IF NOT EXISTS `bruker` (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `epost` varchar(255) NOT NULL,
  `passordhash` varchar(255) NOT NULL,
  PRIMARY KEY(id),
  UNIQUE(epost)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellstruktur for tabell `nokkel`
--

CREATE TABLE IF NOT EXISTS `nokkel` (
  `uuid` char(23) NOT NULL,
  `offentlig_nokkel` tinytext NOT NULL,
  `bruker` bigint(20) UNSIGNED NOT NULL,
  PRIMARY KEY(uuid),
  FOREIGN KEY(bruker) REFERENCES bruker(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellstruktur for tabell `okt`
--
CREATE TABLE IF NOT EXISTS `okt` (
  `nr` int UNSIGNED NOT NULL,
  `nokkel` CHAR(23) NOT NULL,
  `offentlig_oktnokkel` tinytext NOT NULL,
  `utloper` datetime NULL,
  PRIMARY KEY(nr, nokkel),
  FOREIGN KEY(nokkel) REFERENCES nokkel(uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Tabellstruktur for tabell `nonce`
--

CREATE TABLE IF NOT EXISTS `nonce` (
  `nokkel` CHAR(23) NOT NULL,
  `oktNr` int UNSIGNED NOT NULL,
  `nonce` BIGINT(20) NOT NULL,
  PRIMARY KEY(nokkel, oktNr, nonce),
  FOREIGN KEY(nokkel) REFERENCES nokkel(uuid),
  FOREIGN KEY(oktNr) REFERENCES okt(nr)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
