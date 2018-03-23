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
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Signature;
import java.security.SignatureException;

import static com.example.applikasjon.MainActivity.uuid;

@RequiresApi(api = Build.VERSION_CODES.M)
class FingerprintHjelper extends FingerprintManager.AuthenticationCallback {

    private Context kontekst;
    public static FingerprintManager.CryptoObject kryptOb;
    private StartOkt okt = null;
    private String pemSign;

    FingerprintHjelper(Context kon) {
        this.kontekst = kon;
    }

    public void startAutentisering(FingerprintManager fManager, FingerprintManager.CryptoObject cObject) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            CancellationSignal cSignal = new CancellationSignal();
            settKryptoObjekt(cObject);
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
        //StartOkt okt = null;
        //Sender til innlogging hvis uuid ikke finnes (førstegangsautentisering er ikke gjennomført)
        if (uuid == null) {
            kontekst.startActivity(new Intent(kontekst, LogginnActivity.class));
        }

        else {
            Response.Listener<String> respons = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject jsonRespons = null;
                    try {
                        jsonRespons = new JSONObject(response);
                        boolean suksess = jsonRespons.getBoolean("suksess");

                        if (suksess) {
                            Log.d("OKT", "SUKSESS");

                        }
                        else {
                            MainActivity.visFeilMelding(jsonRespons.getString("feilmelding"), kontekst);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();  //TODO: Fjern før ferdigstilling

                    }
                }
            };
            Signature signatur = kryptOb.getSignature();
            try {
                byte[] forSigning = FingerprintActivity.pemOktKey.getBytes();
                signatur.update(forSigning); //TODO: VERIFY
                byte[] signert = signatur.sign();
                pemSign = Base64.encodeToString(signert, Base64.DEFAULT);
            } catch (SignatureException e) {
                e.printStackTrace();
            }
            okt = new StartOkt(uuid, pemSign, respons, this.kontekst);
            RequestQueue queue = Volley.newRequestQueue(this.kontekst);
            queue.add(okt);

        }
    }

    @Override  //Hvis autentiseringen feilet
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();                                                                     //Kall parentfunksjonen
        MainActivity.visFeilMelding(this.kontekst.getString(R.string.autentiseringsfeil), this.kontekst);     //Vis feilmelding
        return;
    }

    private void settKryptoObjekt(FingerprintManager.CryptoObject cr) {
        this.kryptOb = cr;
    }


}
