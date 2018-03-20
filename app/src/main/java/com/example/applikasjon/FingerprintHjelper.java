package com.example.applikasjon;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.android.volley.Response;

import java.io.Serializable;

import static com.example.applikasjon.MainActivity.uuid;

@RequiresApi(api = Build.VERSION_CODES.M)
class FingerprintHjelper extends FingerprintManager.AuthenticationCallback {

    private Context kontekst;

    FingerprintHjelper(Context kon) {
        this.kontekst = kon;
    }

    public void startAutentisering(FingerprintManager fManager, FingerprintManager.CryptoObject cObject) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            CancellationSignal cSignal = new CancellationSignal();
            if (ActivityCompat.checkSelfPermission(this.kontekst, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return;                                                                                                                             //Avslutt
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fManager.authenticate(cObject, cSignal, 0 , this, null);
            }
            else return;
        }
        else return;
    }

    @Override  //Hvis autentiseringen er godkjent
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult resultat) {
        super.onAuthenticationSucceeded(resultat);                                                          //Kall parentfunksjonen
        StartOkt okt = null;
        //Sender til innlogging hvis uuid ikke finnes
        if (uuid == null) {
            kontekst.startActivity(new Intent(kontekst, LogginnActivity.class));
        }

        else {
            Response.Listener<String> respons = new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d("RESPONS", "RESPONS");
                }
            };
            okt = new StartOkt(uuid, respons, this.kontekst);

        }
         }

    @Override  //Hvis autentiseringen feilet
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();                                                                     //Kall parentfunksjonen
        MainActivity.visFeilMelding(this.kontekst.getString(R.string.autentiseringsfeil), this.kontekst);     //Vis feilmelding
        return;
    }
}
