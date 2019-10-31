CREATE DATABASE  IF NOT EXISTS `buymanagerdb` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `buymanagerdb`;
-- MySQL dump 10.13  Distrib 5.6.17, for Win32 (x86)
--
-- Host: buymanagerdb.cy2rizizzzzx.eu-west-1.rds.amazonaws.com    Database: buymanagerdb
-- ------------------------------------------------------
-- Server version	5.6.19-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `client`
--

DROP TABLE IF EXISTS `client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `client` (
  `clientID` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(45) DEFAULT NULL,
  `key` varchar(45) DEFAULT NULL,
  `nbReqAutorisees` int(11) DEFAULT NULL,
  `customer_key` varchar(45) NOT NULL COMMENT '=*, => pas de verification',
  `CustomerVersion` int(11) NOT NULL COMMENT ' 1 => engeniring ',
  `Key_dateExpiration` date NOT NULL,
  `nbCustomerKey` varchar(45) NOT NULL DEFAULT '1' COMMENT 'Nombre de clef disponible pour le client',
  `nbMonthDeferred` int(11) DEFAULT '1',
  PRIMARY KEY (`clientID`),
  UNIQUE KEY `clientID_UNIQUE` (`clientID`),
  UNIQUE KEY `key_UNIQUE` (`key`)
) ENGINE=InnoDB AUTO_INCREMENT=153 DEFAULT CHARSET=utf8 COMMENT='Table correspondant au client avec son ID (clé primaire unique), son nom, sa clé (unique) et le nombre de requetes dont il a le droit et dont il a utilisées';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ip_list`
--

DROP TABLE IF EXISTS `ip_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ip_list` (
  `ip_listID` int(11) NOT NULL AUTO_INCREMENT,
  `clientID` int(11) DEFAULT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  PRIMARY KEY (`ip_listID`),
  UNIQUE KEY `ip_listID_UNIQUE` (`ip_listID`),
  KEY `clientID_idx` (`clientID`),
  CONSTRAINT `clientID` FOREIGN KEY (`clientID`) REFERENCES `client` (`clientID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='Table des IP des clients';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `utilisation_client`
--

DROP TABLE IF EXISTS `utilisation_client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `utilisation_client` (
  `id_utilisation_client` int(11) NOT NULL AUTO_INCREMENT,
  `clientID` int(11) NOT NULL,
  `mois_annee` date NOT NULL,
  `credit_restant` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_utilisation_client`),
  UNIQUE KEY `client_ID_MoisAnnee` (`clientID`,`mois_annee`),
  CONSTRAINT `client ID` FOREIGN KEY (`clientID`) REFERENCES `client` (`clientID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `webservice`
--

DROP TABLE IF EXISTS `webservice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `webservice` (
  `wsID` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`wsID`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='Table referencant l''ensemble des ws ';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ws_client`
--

DROP TABLE IF EXISTS `ws_client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ws_client` (
  `ws_client_ID` int(11) NOT NULL AUTO_INCREMENT,
  `clientID` int(11) NOT NULL,
  `wsID` int(11) NOT NULL,
  `magasin` varchar(45) NOT NULL,
  `swPrio` int(3) NOT NULL,
  `interroParDefaut` enum('true','false') DEFAULT 'false',
  `country` varchar(10) NOT NULL,
  `key` varchar(50) NOT NULL COMMENT 'Clef du Web service : cas RS et octo (si vide on utilise notre code)',
  `login` varchar(45) NOT NULL COMMENT 'login du client pour le service : dans le cas de farnell',
  PRIMARY KEY (`ws_client_ID`,`clientID`,`wsID`),
  KEY `clientIDwsID` (`clientID`,`wsID`),
  KEY `wsID_idx` (`wsID`),
  CONSTRAINT `client_ID` FOREIGN KEY (`clientID`) REFERENCES `client` (`clientID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `wsID` FOREIGN KEY (`wsID`) REFERENCES `webservice` (`wsID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8 COMMENT='Table regroupant les ws autorisés pour les clients';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-02-17 11:49:39
