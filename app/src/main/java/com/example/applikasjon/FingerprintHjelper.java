package com.example.applikasjon;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import java.security.Signature;
import java.security.SignatureException;
import static com.example.applikasjon.MainActivity.uuid;

/**
 * Klasse som håndterer fingeravtrykkautentiseringen
 */
@RequiresApi(api = Build.VERSION_CODES.M)
class FingerprintHjelper extends FingerprintManager.AuthenticationCallback {

    private Context kontekst;
    public static FingerprintManager.CryptoObject kryptOb;
    public static String pemSign;

    /**
     * Constructor som setter konteksten til klassen
     * @param kon Context Konteksten som elementene skal vises i
     */
    FingerprintHjelper(Context kon) {
        this.kontekst = kon;
    }

    /**
     * Funksjon for å autentisere brukeren for klienten
     * @param fManager FingerprintManager Håndterer aksess til fingeravtrykkshardware.
     * @param cObject CryptoObject Wrapper for krypterings APIer.
     */
    public void startAutentisering(FingerprintManager fManager, FingerprintManager.CryptoObject cObject) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { //Sjekker at dette kallet er støttet av enheten som kjører programmet
            CancellationSignal cSignal = new CancellationSignal();     //Setter opp cancellationsignal til bruk av authenticate funksjonen
            settKryptoObjekt(cObject);
            if (ActivityCompat.checkSelfPermission(this.kontekst, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) { //Sjekker at riktige permissions er gitt
                return;                                                                                                                        //Avslutt
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //Sjekker at dette kallet er støttet av enheten som kjører programmet
                fManager.authenticate(cObject, cSignal, 0 , this, null);
            }
            else return;
        }
        else return;
    }

    /**
     * Hvis autentiseringen er godkjent startes LogginnActivity hvis uuid er null, ellers så kalles startOkt hvis uuid allerede er satt
     * @param resultat Resultaten fra autentiseringen
     */
    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult resultat) {
        super.onAuthenticationSucceeded(resultat);                                //Kaller parentfunksjonen
        if (uuid == null) {
            kontekst.startActivity(new Intent(kontekst, LogginnActivity.class));
        }
        else {
            LogginnActivity.startOkt(kontekst);
        }
    }


    /**
     * Hvis autentiseringen ikke er godkjent vises en feilmelding som også starter fingerprintactivity på nytt
     */
    @Override  //Hvis autentiseringen feilet
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();                                                                     //Kall parentfunksjonen
        MainActivity.visFeilMelding(this.kontekst.getString(R.string.autentiseringsfeil), this.kontekst);     //Vis feilmelding
        return;
    }

    /**
     * Setter klassens kryptOb
     * @param cr CryptoObject som sendes med
     */
    private void settKryptoObjekt(FingerprintManager.CryptoObject cr) {
        this.kryptOb = cr;
    }

    /**
     * Funksjon for å signere en medsendt String
     * @param skalSigneres String Det som skal signeres
     * @return String signaturen til skalSigneres i Base64 format. Returnerer null hvis signaturen ikke kunne opprettes.
     */
    public static String sign(String skalSigneres) {
        Signature signatur = FingerprintActivity.signatur;
        byte[] forSigning = skalSigneres.getBytes();   //Henter ut bytes så man kan signere
        try {
            signatur.update(forSigning);              //Gjør klar bytene til signering
            byte[] signert = signatur.sign();         //Signerer
            return Base64.encodeToString(signert, Base64.DEFAULT);  //Gjør klar til bruk i string-form
        } catch (SignatureException e) {
           MainActivity.visFeilMelding("Autentisering feilet, Vennligst prøv igjen", MainActivity.ct);
        }
        return null;
    }
}
