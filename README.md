# Konseptbevis for fingeravtrykkautentisering på Android

En Android-applikasjon og server som implementerer sikker autentisering ved hjelp av fingeravtrykk og sikkert lagrede nøkler. 


## Bakgrunn

Som en del av bachelor-oppgaven skulle vi utvikle en applikasjon som benytter seg av fingeravtrykk og nøkler lagret i et maskinvarestøttet nøkkellager for å autentisere brukere ovenfor en server.
Applikasjonen har ingen funksjon annet enn som et konseptbevis for fingeravtrykk-autentisering.


## Krav

* En Android-telefon med fingeravtrykk-leser og Android 6.0 eller nyere.
* En HTTPS webserver med PHP-prosessering 
* En MariaDB/MySQL databaseserver

## Oppsett

Dersom serveren fortsatt er tilgjengelig kan man plukke opp nyeste ferdigbygde APK fra [releases](releases).
For å sjekke om serveren er tilgjengelig, sjekk status på <https://folk.ntnu.no/sturlaba/sfa/>

Dersom serveren ikke er tilgjengelig må man sette opp server og bygge applikasjonen selv. Fremgangsmåten for dette er beskrevet under.

### Manuelt oppsett

Følgende skritt må følges for å sette opp applikasjonen og kjøre konseptbeviset

#### Databaseserver

1. Opprett en database med navnet ``sfa``.
2. Importer ``server/DB/opprett.sql`` til sfa-databasen.
3. Opprett en bruker som har følgende rettigheter for sfa-databasen: ``SELECT, INSERT, UPDATE, DELETE``.

#### Applikasjons/Webserver

1. Kopier innholdet fra ``server``-mappen til webserveren.
2. Endre brukernavn, passord og hostname i ``DB/sql_bruker.php`` slik at feltene stemmer med databasebrukeren som ble opprettet. Eventuelt fjern require og endre verdiene direkte i ``classes/DB.php``, linje 19 til 23.

#### Applikasjon

1. Åpne repository-mappen med Android Studio
2. La Gradle synkronisere prosjektet
3. Åpne filen ``app/src/main/java/com/example/applikasjon/MainActivity.java`` og endre verdien til ``HandlingsURL`` (linje 22) til å være lik URLen til webserveren. Merk at kun HTTPS er støttet.
4. Kjør applikasjonen på en virtuell enhet eller en enhet som er koblet til via ADB.

## Forfattere

* **Linn-Mari Kristiansen**
* **Henriette Kolby Rohde Garder**
* **Sturla Høgdahl Bae**

## Lisens

Koden i dette repositoriet er lisensiert under Apache lisensen, versjon 2.0 dersom ikke annet er nevnt. Se filen [LICENSE](LICENSE) for detaljer

## Anerkjennelser

Vi vil takke alle som har assistert oss under arbeidet med prosjektet:

* Vår veileder Prof. Dr. Basel Katt
* Våre kontaktpersoner ved Eika Gruppen, Thomas Eriksson og Jon Hagen.
