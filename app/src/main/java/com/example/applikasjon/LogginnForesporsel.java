package com.example.applikasjon;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
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

/**
 * Klasse for å gjøre klar en innloggingsforespørsel til serveren
 */
public class LogginnForesporsel extends StringRequest {

    private static final String LOGGINNURL = "https://folk.ntnu.no/sturlaba/sfa/";
    private Map<String, String> parametere;   //Brukes av Volley for å sende data til siden
    private Context kontekst = null;

    /**
     * Constructor som setter opp en forespørsel
     * @param brukernavn String Brukernavnet til brukeren
     * @param passord String Passordet til brukeren
     * @param listener Resonse.Listener<String> Lytter til svar fra serveren
     * @param con Context Konteksten som elementer skal vises i
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public LogginnForesporsel(String brukernavn, String passord, Response.Listener<String> listener, Context con) {
        super(Request.Method.POST, LOGGINNURL, listener, null);

        KeyStore keyStore = null;
        PublicKey offentligNokkel = null;
        String pemKey = null;
        kontekst = con;
        try {  //Last inn den offentlige permanente nøkkelen og gjør det om til et format som serveren kan lese
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            offentligNokkel =
                    keyStore.getCertificate(KEYNAME).getPublicKey();
            pemKey = "-----BEGIN PUBLIC KEY-----\n"+android.util.Base64.encodeToString(keyStore.getCertificate(KEYNAME).getPublicKey().getEncoded(), android.util.Base64.DEFAULT)+"-----END PUBLIC KEY-----";

        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e ) {
            MainActivity.visFeilMelding("Feil ved serverkommunikasjon", this.kontekst);
        }

        //Setter alle verdiene som serveren forventer
        parametere = new HashMap<>();
        parametere.put("epost", brukernavn);
        parametere.put("passord", passord);
        parametere.put("offentlig_nokkel", pemKey);
        parametere.put("forstegangsautentisering", "true");
    }

    /**
     * Henter ut innholdet til parameterarrayen(Map)
     * @return Map parameterene til forespørselen
     */
    @Override
    public Map<String, String> getParams(){
        return parametere;
    }

}
