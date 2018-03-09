<?php

/**
 * Klasse for å hente ut et PDO-objekt som er koblet til databasen.
 * Hent ut et nytt objekt ved å kalle DB::hentDB();
 */
class DB {
  private static $db = null;
  private $dbh = null;

  /**
   * Constructor som oppretter et nytt PDO-objekt med detaljene som spesifiseres
   */
  private function __construct() {

    require_once __DIR__ . '/../DB/sql_bruker.php';       // Laster inn fil med navn på database, host, brukernavn og passord
    $dbnavn = DBNAVN;
    $host = HOST;
    $bruker = BRUKERNAVN;
    $passord = PASSORD;

    try {
      $this->dbh = new PDO("mysql:dbname=$dbnavn;host=$host", $bruker, $passord);
      $this->dbh->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_WARNING); 
    }
    catch (PDOException $e) {
      echo 'Kunne ikke koble til databasen.';
      exit;
    }
  }

  /**
   * Funksjon for å hente ut et pdo-objekt
   * 
   * @return pdo Et pdo-objekt som er koblet til databasen.
   */
  public static function hentDB() {
      if (DB::$db == null) {
        DB::$db = new self();
      }

      return DB::$db->dbh;
  }
}