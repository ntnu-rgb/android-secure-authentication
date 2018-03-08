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
  $resultat = $bruker->loggInn($_POST['epost'], $_POST['passord'], $_POST['offentlig_nokkel']);
  echo json_encode($resultat);
}
else if(isset($_POST['registrer'], $_POST['epost'], $_POST['passord'])) {
  $resultat = $bruker->registrer($_POST['epost'], $_POST['passord']);
  echo json_encode($resultat);
}
else if(isset($_POST['hent_utfordring'], $_POST['uuid'])) {
  $resultat = $bruker->genererUtfordring($_POST['uuid']);
  echo json_encode($resultat);
}
else {
  header('Content-Type: text/html; charset=utf-8');
  include 'skjema.html';
}
