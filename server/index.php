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
if(isset($_POST['forstegangsautentisering'], $_POST['epost'], $_POST['passord'], $_POST['offentlig_nokkel'])) {
  $resultat = $bruker->loggInn($_POST['epost'], $_POST['passord'], $_POST['offentlig_nokkel']);
  if($resultat['suksess']) {                   // Dersom brukeren kunne logges inn og nøkkelen kunne lagres
    http_response_code(200);                   // 200 OK
    echo $resultat['uuid'];                    // Skriver ut UUID til nøkkelen
  }
  else {                                       // Dersom brukeren ikke kunne logges inn eller nøkkelen ikke kunne lagres
    if($resultat['feilmelding'] == 'Feil e-postadresse eller passord.') {
      http_response_code(403);                 // 403 Forbidden
      echo $resultat['feilmelding'];
    }
    else {
      http_response_code(400);                 // 400 Bad Request
      echo $resultat['feilmelding'];
    }
  }
}
else if(isset($_POST['registrer'], $_POST['epost'], $_POST['passord'])) {
  $resultat = $bruker->registrer($_POST['epost'], $_POST['passord']);
  if($resultat['suksess']) {                    // Dersom brukeren kunne opprettes
    http_response_code(201);                    // 201 Created
  }
  else {                                        // Dersom brukeren ikke kunne opprettes
    http_response_code(409);                    // 409 Conflict
    echo $resultat['feilmelding'];
  }
}
else if(isset($_POST['hent_utfordring'], $_POST['uuid'])) {
  $resultat = $bruker->genererUtfordring($_POST['uuid']);
  if($resultat['suksess'] === true) {            // Dersom utfordringen ble opprettet
    http_response_code(201);                     // 201 Created
    echo $resultat['utfordring'];                // Sender utfordringen
  }
  else {
    if($resultat['feilmelding'] == 'Fant ingen sikker kilde til tilfeldighet') {
      http_response_code(500);                   // 500 Internal Server Error
      echo 'En feil har oppstått. Vennligst kontakt systemansvarlig';
    }
    else {
      echo $resultat['feilmelding'];
    }
  }
}
else {
  include 'skjema.html';
}
