package com.example.applikasjon;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import static com.example.applikasjon.FingerprintActivity.KEYNAME;

public class LogginnForesporsel extends StringRequest {

    private static final String LOGGINNURL = "https://folk.ntnu.no/sturlaba/sfa/";
    private Map<String, String> parametere;   //Brukes av Volley for å sende data til siden
    private Context kontekst = null;


    @RequiresApi(api = Build.VERSION_CODES.M)
    public LogginnForesporsel(String brukernavn, String passord, Response.Listener<String> listener, Context con) {
        super(Request.Method.POST, LOGGINNURL, listener, null);

        KeyStore keyStore = null;
        PublicKey offentligNokkel = null;
        String pemKey = null;
        kontekst = con;

        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            offentligNokkel =
                    keyStore.getCertificate(KEYNAME).getPublicKey();
            pemKey = "-----BEGIN PUBLIC KEY-----\n"+android.util.Base64.encodeToString(keyStore.getCertificate(KEYNAME).getPublicKey().getEncoded(), android.util.Base64.DEFAULT)+"-----END PUBLIC KEY-----";

        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e ) {
            Log.d("NØKKELERROR", "Error ved henting av offentlig nøkkel ");  //TODO: Fjern før ferdigstilling
            MainActivity.visFeilMelding("Feil ved serverkommunikasjon", this.kontekst);
        }
        parametere = new HashMap<>();
        parametere.put("epost", brukernavn);
        parametere.put("passord", passord);
        parametere.put("offentlig_nokkel", pemKey);
        parametere.put("forstegangsautentisering", "true");
    }

    @Override
    public Map<String, String> getParams(){
        return parametere;
    }

}
