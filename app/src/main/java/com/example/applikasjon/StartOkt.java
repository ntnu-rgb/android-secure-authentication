package com.example.applikasjon;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;
import static com.example.applikasjon.FingerprintActivity.*;

/**
 * Klasse som brukes til å gjøre klar en økt før den sendes til server
 */
public class StartOkt extends StringRequest {

    private static final String LOGGINNURL = "https://folk.ntnu.no/sturlaba/sfa/";
    private Map<String, String> parametere;   //Brukes av Volley for å sende data til siden
    private Context kontekst = null;
    private String okt = "OktNokkel";

    /**
     * Constructor som setter riktige verdier inn i parameter arrayen som skal sendes til server
     * @param pemSign String Signaturen som skal sendes til server
     * @param listener Response.Listener<String> Lytter til responsen
     * @param con Context Konteksten som elementene til denne klassen skal vises i
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public StartOkt(String pemSign, Response.Listener<String> listener, Context con) {
        super(Request.Method.POST, LOGGINNURL, listener, null);
        this.kontekst = con;

        //Setter inn de forskjellige parameterene som skal sendes til server
        parametere = new HashMap<>();
        parametere.put("start_okt", "true");
        parametere.put("uuid", MainActivity.uuid);
        parametere.put("offentlig_oktnokkel", pemOktKey);
        parametere.put("signatur", pemSign);
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