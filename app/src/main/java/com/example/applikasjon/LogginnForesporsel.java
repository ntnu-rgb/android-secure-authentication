package com.example.applikasjon;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class LogginnForesporsel extends StringRequest {

    private static final String LOGGINNURL = "https://folk.ntnu.no/sturlaba/sfa/";
    private Map<String, String> parametere;   //Brukes av Volley for å sende data til siden


    @RequiresApi(api = Build.VERSION_CODES.O)
    public LogginnForesporsel(String brukernavn, String passord, Response.Listener<String> listener) {
        super(Request.Method.POST, LOGGINNURL, listener, null);

        KeyStore keyStore = null;
        PublicKey offentligNokkel = null;
        PublicKey verificationKey = null;
        String keystring = null;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            offentligNokkel =
                    keyStore.getCertificate(FingerprintActivity.KEYNAME).getPublicKey();
            KeyFactory factory = KeyFactory.getInstance(offentligNokkel.getAlgorithm());
            X509EncodedKeySpec spec = new X509EncodedKeySpec(offentligNokkel.getEncoded());
            verificationKey = factory.generatePublic(spec);
            keystring = (Base64.getEncoder().encode(verificationKey.getEncoded())).toString();
            Log.d("OFFENTLIG", keystring);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException |
        InvalidKeySpecException e ) {
            Log.d("NØKKELERROR", "Error ved henting av offentlig nøkkel ");
        }


        parametere = new HashMap<>();
        parametere.put("epost", brukernavn);
        parametere.put("passord", passord);
        parametere.put("offentlig_nokkel", keystring);
        parametere.put("forstegangsautentisering", "true");
    }

    @Override
    public Map<String, String> getParams(){
        return parametere;
    }

}
