<?php

/**
 * Klasse for å behandle en bruker.
 */
class Bruker {
  /** @var PDO Skal inneholde et PDO-objekt som mottas i constructoren */
  private $dbh;

  /**
   * Tar i mot og lagrer et PDO-objekt som er koblet til databasen
   * 
   * @param PDO $dbh Et PDO-objekt som er koblet til databasen.
   */
  public function __construct($dbh) {
    $this->dbh = $dbh;
  }

  /**
   * Funksjon for å registrere en bruker.
   * 
   * @param string $epost E-postadressen til brukeren.
   * @param string $passord Passordet til brukeren.
   * @return string En JSON-kodet tekst med returverdier som 'suksess' og eventuelt 'feilmelding'.
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
    return json_encode($retur);
  }

  /**
   * Funksjon for å logge inn en bruker.
   * 
   * @param string $epost E-postadressen til brukeren.
   * @param string $passord Passordet til brukeren.
   * @param string $offentligNokkel Den offentlige nøkkelen som brukeren vil knytte til seg.
   * @return string En JSON-kodet tekst med returverdier som 'suksess' og eventuelt 'feilmelding'.
   */
  public function loggInn($epost, $passord, $offentligNokkel) {
    $retur = [];
    $epost = strtolower(trim($epost));                                // Trimmer epost og endrer til små bokstaver

    $offentligNokkel = openssl_get_publickey(base64_decode($offentligNokkel));
    $retur['offentlig_nokkel'] = $offentligNokkel;
    

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
    return json_encode($retur);
  }

  /**
   * Funksjon for å lagre den offentlige nøkkelen til en bruker
   * 
   * @param int $brukerId Id til brukeren som har autentisert seg og sendt inn nøkkelen.
   * @param string $offentligNokkel Den offentlige nøkkelen som skal knyttes til brukeren.
   * @return string Returnerer en UUID som er knyttet til nøkkelen, eventuelt null dersom nøkkelen ikke kunne legges inn.
   */
  private function lagreOffentligNokkel($brukerId, $offentligNokkel) {

    $sql = 'SELECT COUNT(*) AS antall FROM nokkel WHERE offentlig_nokkel = ?';
    $sth = $this->dbh->prepare($sql);                                   // Sjekker om en identisk nøkkel allerede er lagt inn
    $sth->execute([$offentligNokkel]);
    if($sth->fetch(PDO::FETCH_ASSOC)['antall'] != 0) {
      return false;                                                     // Returnerer false dersom nøkkelen allerede eksisterer
    }

    do {
      $uuid = uniqid('', true);                                         // Genererer en (sannsynligvis) unik id for nøkkelen
      $sql = 'SELECT COUNT(*) AS antall FROM nokkel WHERE uuid = ?';
      $sth = $this->dbh->prepare($sql);
      $sth->execute([$uuid]);
    }
    while($sth->fetch(PDO::FETCH_ASSOC)['antall'] != 0);                // Genererer på nytt dersom uuid ikke er unik

    $sql = 'INSERT INTO nokkel(uuid, offentlig_nokkel, bruker) VALUES(?, ?, ?)';
    $sth = $this->dbh->prepare($sql);
    $sth->execute([$uuid, $offentligNokkel, $brukerId]);
    return ($sth->rowCount() == 1) ? $uuid : false;                     // Returnerer uuid dersom nøkkelen kunne settes inn
  }

  /**
   * Starter en økt ved å lagre en angitt øktnøkkel dersom øktnøkkelen er signert av brukeren.
   * 
   * @param string $uuid Den unike id'en til nøkkelen som det signeres med.
   * @param string $offentligOktnokkel Den offentlige nøkkelen som skal brukes til å autentisere kall resten av økten.
   * @param string $signatur Signatur av øktnøkkelen som brukes til å autentisere brukeren som vil starte økten.
   * @return string En JSON-kodet tekst med returverdier som 'suksess' og eventuelt 'feilmelding'.
   */
  public function startOkt($uuid, $offentligOktnokkel, $signatur) {
    $retur = [];

    // TODO: Bekreft signatur

    $sql = 'SELECT COUNT(*) AS antall FROM okt WHERE offentlig_oktnokkel = ?';
    $sth = $this->dbh->prepare($sql);                                   // Sjekker at det ikke allerede eksisterer en økt med angitt nøkkel
    $sth->execute([$offentligOktnokkel]);
    if($sth->fetch(PDO::FETCH_ASSOC)['antall'] != 0) {
      $retur['suksess'] = 'false';
      $retur['feilmelding'] = 'Kunne ikke opprette økt';                // Gir feilmelding dersom en identisk nøkkel allerede eksisterer
      return $retur;
    }

    $utloper = date('Y:m:d H:i:s', strtotime('+1 hour'));               // TODO: Hvor lenge skal den være gyldig?

    $sql = 'SELECT MAX(nr) AS maks FROM okt WHERE nokkel = ?';          // Finner neste økt-nummer i rekken for angitt nøkkel
    $sth = $this->dbh->prepare($sql);
    $sth->execute([$uuid]);
    $maks = $sth->fetch(PDO::FETCH_ASSOC)['maks'];
    $nummer = ($maks != null) ? $maks + 1 : 1;

    $sql = 'INSERT INTO okt(nr, nokkel, offentlig_oktnokkel, utloper) VALUES(?, ?, ?, ?)';
    $sth = $this->dbh->prepare($sql);                                   // Setter økt-nøkkelen inn i databasen
    $sth->execute([$nummer, $uuid, $offentligOktnokkel, $utloper]);
    if($sth->rowCount() == 1) {
      $retur['suksess'] = true;
      $retur['oktNr'] = $nummer;                                        // Returnerer øktnummeret og utløpstidspunkt
      $retur['utloper'] = $utloper;
    }
    else {
      $retur['suksess'] = false;
      $retur['feilmelding'] = 'Kunne ikke opprette økt';
    }
    return json_encode($retur);
  }

  /**
   * Utfører en angitt handling. Krever at brukeren autentiserer seg.
   * 
   * @param string $uuid Id til nøkkelparet.
   * @param int $oktNr Identifikator for økten (sammen med UUID til nøkkelparet).
   * @param string $handlingsdata En JSON-kodet tekst med data tilknyttet handlingen.
   * @param string $signatur Signatur av handlingen, utført med øktnøkkelen.
   * @return string En JSON-kodet tekst med returverdier som 'suksess' og eventuelt 'feilmelding'.
   */
  public function utforHandling($uuid, $oktNr, $handlingsdata, $signatur) {
    $retur = [];

    if(true) { // TODO: Bekreft signatur
      $handling = json_decode($handlingsdata, true);

      // TODO: Sjekk at nonce ikke er brukt tidligere i samme økt

      // TODO: Lagre unna nonce for økten

      // Gjør handling

      $retur['suksess'] = true;
    }
    else {
      $retur['suksess'] = false;
      $retur['feilmelding'] = '';
    }
    return json_encode($retur);
  }
}