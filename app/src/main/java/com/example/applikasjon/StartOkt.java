package com.example.applikasjon;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

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

        Log.d("PARAMETERE", parametere.toString());
    }
    @Override
    public Map<String, String> getParams(){
        return parametere;
    }

}