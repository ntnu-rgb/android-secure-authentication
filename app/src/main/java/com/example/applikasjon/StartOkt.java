package com.example.applikasjon;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class StartOkt extends StringRequest {

    private static final String LOGGINNURL = "https://folk.ntnu.no/sturlaba/sfa/";
    private Map<String, String> parametere;   //Brukes av Volley for å sende data til siden
    private Context kontekst = null;
    PublicKey verificationKey = null;
    String keystring = null;
    private String okt = "OktNokkel";


    @RequiresApi(api = Build.VERSION_CODES.M)
    public StartOkt(String uuid, Response.Listener<String> listener, Context con) {
        super(Request.Method.POST, LOGGINNURL, listener, null);

        this.kontekst = con;
        FingerprintActivity.genererNokler(okt);
        byte[] encodedString = null;
        KeyStore keyStore = null;
        PublicKey offentligNokkel = null;
        String sig = null;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            offentligNokkel =
                    keyStore.getCertificate(okt).getPublicKey();
            KeyFactory factory = KeyFactory.getInstance(offentligNokkel.getAlgorithm());
            X509EncodedKeySpec spec = new X509EncodedKeySpec(offentligNokkel.getEncoded());
            verificationKey = factory.generatePublic(spec);
            encodedString = Base64.encode(verificationKey.getEncoded(), Base64.DEFAULT);
            keystring = encodedString.toString();
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException |
                InvalidKeySpecException e ) {
            MainActivity.visFeilMelding("Feil ved serverkommunikasjon", con);
        }
        Signature signatur = null;
        PrivateKey priv = null;
        try {
            signatur = Signature.getInstance("SHA256withECDSA");
            priv = (PrivateKey) keyStore.getKey(FingerprintActivity.KEYNAME, null);
            signatur.initSign(priv);
            signatur.update(encodedString);
            byte[] signaturBytes = signatur.sign();
            sig =  (Base64.encode(verificationKey.getEncoded(), Base64.DEFAULT)).toString();
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | InvalidKeyException | SignatureException e) {
            MainActivity.visFeilMelding("Feil ved serverkommunikasjon", con);
        }
        parametere = new HashMap<>();
        parametere.put("start_okt", "true");
        parametere.put("uuid", uuid);
        parametere.put("offentlig_nokkel", keystring);
        parametere.put("signatur", sig);
    }

    @Override
    public Map<String, String> getParams(){
        return parametere;
    }

}