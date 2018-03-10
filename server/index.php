<?php

require_once 'classes/DB.php';
require_once 'classes/Bruker.php';

$dbh = DB::hentDB();
$bruker = new Bruker($dbh);

/**
 * Dette er ikke en anbefalt måte å behandle den initielle registreringen/innloggingen på!
 * 
 * Applikasjonen skal kun være et konseptbevis for sikker fingeravtrykk-autentisering, 
 * og vi bruker derfor grunnleggende registrering og innlogging for den mer permanente kontoen.
 * 
 * Sikkerheten er kun like god som den svakeste aksesskontrollen, og vi anbefaler derfor 
 * å bruke en annen autentiseringsmetode som gjerne omfatter flere (sikre) faktorer.
 * Dersom det skal eksistere metoder for å gjenopprette en konto, må denne også være sikker.
 * 
 * Tips til sikker autentisering er tilgjengelig på: https://www.owasp.org/index.php/Authentication_Cheat_Sheet
 */

header('Content-Type: application/json');

if(isset($_POST['forstegangsautentisering'], $_POST['epost'], $_POST['passord'], $_POST['offentlig_nokkel'])) {
  echo $bruker->loggInn($_POST['epost'], $_POST['passord'], $_POST['offentlig_nokkel']);                      // Autentiser og lagre offentlig nøkkel
}
else if(isset($_POST['registrer'], $_POST['epost'], $_POST['passord'])) {
  echo $bruker->registrer($_POST['epost'], $_POST['passord']);                                                // Registrer bruker
}
else if(isset($_POST['start_okt'], $_POST['uuid'], $_POST['offentlig_oktnokkel'], $_POST['signatur'])) {
  echo $bruker->startOkt($_POST['uuid'], $_POST['offentlig_oktnokkel'], $_POST['signatur']);                  // Start økt
}
else if(isset($_POST['uuid'], $_POST['oktNr'], $_POST['handlingsdata'], $_POST['signatur'])) {
  echo $bruker->utforHandling($_POST['uuid'], $_POST['oktNr'], $_POST['handlingsdata'], $_POST['signatur']);  // Utfør handling i økt
}
else {
  header('Content-Type: text/html; charset=utf-8');
  include 'skjema.html';
}