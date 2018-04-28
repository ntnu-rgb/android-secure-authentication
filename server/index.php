<?php

require_once 'classes/DB.php';
require_once 'classes/Bruker.php';

$dbh = DB::hentDB();
$bruker = new Bruker($dbh);

header('Content-Type: application/json');

/**
 * ##### API for server #####
 * 
 * Benytter kun POST fordi klienten alltid prøver å endre noe på serveren.
 * Dersom man gjennomfører en GET-forespørsel uten parametre får man en HTML-webside som viser at serveren er tilgjengelig
 * 
 * - For å registrere:
 * registrer=true, epost=<e-postadressen til brukeren>, passord=<passordet til brukeren>
 * 
 * - For å gjennomføre førstegangsautentisering:
 * forstegangsautentisering=true, epost=<e-postadressen til brukeren>, passord=<passordet til brukeren>, offentlig_nokkel=<offentlig nøkkel på PEM-format>
 * 
 * - For å starte en økt:
 * start_okt=true, uuid=<uuid som er tildelt den private nøkkelen>, offentlig_oktnokkel=<den offentlige øktnøkkelen>, signatur=<signatur av den offentlige øktnøkkelen>
 * 
 * - For å gjennomføre en handling i en økt:
 * uuid=<uuid som er tildelt den private nøkkelen>, oktNr=<nummer på økten>, transaksjon=<ønsket handling, på json-format>, signatur=<signatur av transaksjonen>
 */

if(isset($_POST['forstegangsautentisering'], $_POST['epost'], $_POST['passord'], $_POST['offentlig_nokkel'])) {
  returner($bruker->forstegangsautentisering($_POST['epost'], $_POST['passord'], $_POST['offentlig_nokkel']));      // Autentiser og lagre offentlig nøkkel
}
else if(isset($_POST['start_okt'], $_POST['uuid'], $_POST['offentlig_oktnokkel'], $_POST['signatur'])) {
  returner($bruker->startOkt($_POST['uuid'], $_POST['offentlig_oktnokkel'], $_POST['signatur']));                   // Start økt
}
else if(isset($_POST['uuid'], $_POST['oktNr'], $_POST['transaksjon'], $_POST['signatur'])) {
  returner($bruker->utforHandling($_POST['uuid'], $_POST['oktNr'], $_POST['transaksjon'], $_POST['signatur']));     // Utfør handling i økt
}
else if(isset($_POST['registrer'], $_POST['epost'], $_POST['passord'])) {
  returner($bruker->registrer($_POST['epost'], $_POST['passord']));                                                 // Registrer bruker
}
else {                                                                                                              // Dersom ingenting sendes
  header('Content-Type: text/html; charset=utf-8');
  require_once 'hjem.html';                                                                                         // Vis at siden er tilgjengelig
}

/**
 * Funksjon for å returnere data på korrekt format til applikasjonen
 * 
 * @param array $data En array med nøkkel-verdi par som skal returneres til applikasjonen på JSON-format
 */
function returner($data) {
  echo json_encode($data);
}