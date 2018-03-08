<?php

/**
 * Klasse for å behandle en bruker.
 */
class Bruker {
  private $dbh = null;

  /**
   * Constructor som oppretter et nytt PDO-objekt med detaljene som er satt over.
   * Dersom det ikke er mulig å koble til databasen, vil det prøves å bare koble til database-serveren og opprette databasen.
   */
  public function __construct($dbh) {
    $this->dbh = $dbh;
  }

  /**
   * Funksjon for å registrere en bruker.
   * 
   * @param string $epost E-postadressen til brukeren.
   * @param string $passord Passordet til brukeren.
   */
  public function registrer($epost, $passord) {
    $retur = [];
    $epost = strtolower(trim($epost));                                // Trimmer epost og endrer til små bokstaver

    $sql = 'SELECT COUNT(*) AS antall FROM bruker WHERE epost = ?';   // Sjekker at det ikke eksiterer en bruker med angitt e-postadresse
    $sth = $this->dbh->prepare($sql);
    $sth->execute([$epost]);

    if($sth->fetch(PDO::FETCH_ASSOC)['antall'] == 0) {
      $passordhash = password_hash($passord, PASSWORD_DEFAULT);

      $sql = 'INSERT INTO bruker(epost, passordhash) VALUES(?, ?)';   // Oppretter bruker i databasen
      $sth = $this->dbh->prepare($sql);
      $sth->execute([$epost, $passordhash]);
      if($sth->rowCount() == 1) {                                     // Sjekker at en bruker ble opprettet
        $retur['suksess'] = true;
      }
      else {
        $retur['suksess'] = false;
        $retur['feilmelding'] = 'Kunne ikke opprette brukeren';
      }
    }
    else {
      $retur['suksess'] = false;
      $retur['feilmelding'] = 'En bruker med den e-postadressen eksisterer allerede';
    }
    return $retur;
  }

  /**
   * Funksjon for å logge inn en bruker.
   * 
   * @param string $epost E-postadressen til brukeren.
   * @param string $passord Passordet til brukeren.
   * @param string $offentligNokkel Den offentlige nøkkelen som brukeren vil knytte til seg.
   */
  public function loggInn($epost, $passord, $offentligNokkel) {
    $retur = [];
    $epost = strtolower(trim($epost));                                // Trimmer epost og endrer til små bokstaver

    $sql = 'SELECT id, epost, passordhash FROM bruker WHERE epost = ?';
    $sth = $this->dbh->prepare($sql);
    $sth->execute([$epost]);
    $bruker = $sth->fetch(PDO::FETCH_ASSOC);
    if($bruker != null) {                                             // Dersom en bruker med e-postadressen eksisterer
      if(password_verify($passord, $bruker['passordhash'])) {         // Dersom passord matcher
        $resultat = $this->lagreOffentligNokkel($bruker['id'], $offentligNokkel);  // Prøver å lagre den offentlige nøkkelen
        if($resultat != null) {                                       // Dersom den offentlige nøkkelen kan lagres
          $retur['suksess'] = true;                                   // Returner suksess og uuid
          $retur['uuid'] = $resultat;
        }
        else {
          $retur['suksess'] = false;
          $retur['feilmelding'] = 'Kunne ikke lagre nøkkel';
        }
      }
      else {                                                          // Passord feil, gir generell feilmelding
        $retur['suksess'] = false;
        $retur['feilmelding'] = 'Feil e-postadresse eller passord';
      }
    }
    else {                                                            // E-postadresse feil, gir generell feilmelding
      $retur['suksess'] = false;
      $retur['feilmelding'] = 'Feil e-postadresse eller passord';
    }
    return $retur;
  }

  /**
   * Funksjon for å lagre den offentlige nøkkelen til en bruker
   * 
   * @param int $brukerId Id til brukeren som har autentisert seg og sendt inn nøkkelen.
   * @param string $offentligNokkel Den offentlige nøkkelen som skal knyttes til brukeren.
   * @return string Returnerer en UUID som er knyttet til nøkkelen, eventuelt null dersom nøkkelen ikke kunne legges inn.
   */
  private function lagreOffentligNokkel($brukerId, $offentligNokkel) {
    do {
      $uuid = uniqid('', true);                                       // Genererer en (sannsynligvis) unik id
      $sql = "SELECT COUNT(*) AS antall FROM nokkel WHERE uuid = ?";
      $sth = $this->dbh->prepare($sql);
      $sth->execute([$uuid]);
    }
    while($sth->fetch(PDO::FETCH_ASSOC)['antall'] != 0);              // Genererer på nytt dersom uuid ikke er unik

    $sql = 'INSERT INTO nokkel(uuid, offentlig_nokkel, bruker) VALUES(?, ?, ?)';
    $sth = $this->dbh->prepare($sql);
    $sth->execute([$uuid, $offentligNokkel, $brukerId]);
    return ($sth->rowCount() == 1) ? $uuid : false;                    // Returnerer uuid dersom nøkkelen kunne settes inn
  }

  /**
   * Genererer en utfordring knyttet til en spesifikk nøkkel
   * 
   * @param string $uuid Den unike id'en til brukeren sin nøkkel
   * @return int En nonce (engangsnummer) som brukes som utfordring
   */
  public function genererUtfordring($uuid) {
    $retur = [];

    // TODO: Refaktorer etter best-practice

    try {
      $tilfeldigeBytes = random_bytes(16);                             // Genererer tilfeldig binær data
    }
    catch(Exception $e) {                                              // Dersom ikke en sikker PRNG er tilgjengelig, returner feilmelding.
      $retur['suksess'] = false;                                       // Burde føre til en notifikasjon til systemadministrator
      $retur['feilmelding'] = 'Fant ingen sikker kilde til tilfeldighet';
      return $retur;
    }

    $tilfeldigeBytes = base64_encode($tilfeldigeBytes);                // Koder til base64 for å sikre at ingen tegn går tapt under overføring
    $utfordring = $tilfeldigeBytes . time();                           // Setter tilfeldig data sammen med tidspunkt for å sikre at verdien er unik
    $utloper = date('Y:m:d H:i:s', strtotime('+10 minutes'));  // TODO: Må den ha utløpstidspunkt? og i såfall så må vi behandle dette på klienten.
    
    $sql = 'DELETE FROM utfordring WHERE uuid = ?;
    INSERT INTO utfordring(utfordring, uuid, utloper) VALUES(?, ?, ?)';
    $sth = $this->dbh->prepare($sql);
    if($sth->execute([$uuid, $utfordring, $uuid, $utloper])) {
      $retur['suksess'] = true;
      $retur['utfordring'] = $utfordring;
    }
    else {
      $retur['suksess'] = false;
      $retur['feilmelding'] = 'Fant ingen nøkkel med angitt UUID';
    }
    return $retur;
  }
}