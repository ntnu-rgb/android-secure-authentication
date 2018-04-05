<?php

require_once 'classes/DB.php';
require_once 'classes/Bruker.php';

$dbh = DB::hentDB();
$bruker = new Bruker($dbh);

header('Content-Type: application/json');

if(isset($_POST['forstegangsautentisering'], $_POST['epost'], $_POST['passord'], $_POST['offentlig_nokkel'])) {
  returner($bruker->forstegangsautentisering($_POST['epost'], $_POST['passord'], $_POST['offentlig_nokkel']));     // Autentiser og lagre offentlig nøkkel
}
else if(isset($_POST['start_okt'], $_POST['uuid'], $_POST['offentlig_oktnokkel'], $_POST['signatur'])) {
  returner($bruker->startOkt($_POST['uuid'], $_POST['offentlig_oktnokkel'], $_POST['signatur']));                  // Start økt
}
else if(isset($_POST['uuid'], $_POST['oktNr'], $_POST['transaksjon'], $_POST['signatur'])) {
  returner($bruker->utforHandling($_POST['uuid'], $_POST['oktNr'], $_POST['transaksjon'], $_POST['signatur']));    // Utfør handling i økt
}
else if(isset($_POST['registrer'], $_POST['epost'], $_POST['passord'])) {
  returner($bruker->registrer($_POST['epost'], $_POST['passord']));                                                // Registrer bruker
}

/**
 * Funksjon for å returnere data på korrekt format til applikasjonen
 * 
 * @param array $data En array med nøkkel-verdi par som skal returneres til applikasjonen på JSON-format
 */
function returner($data) {
  echo json_encode($data);
}