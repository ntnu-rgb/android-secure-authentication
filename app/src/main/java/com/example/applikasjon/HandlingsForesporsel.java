package com.example.applikasjon;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;

/**
 *  Klasse for å gjøre klar en handlingsforespørsel før den sendes til server
 */
public class HandlingsForesporsel extends StringRequest {
    private static final String HANDLINGSURL = "https://folk.ntnu.no/sturlaba/sfa/";
    private Map<String, String> parametere;   //Brukes av Volley for å sende data til siden

    @RequiresApi(api = Build.VERSION_CODES.M)
    public HandlingsForesporsel(Response.Listener<String> listener) {
        super(Request.Method.POST, HANDLINGSURL, listener, null);

        //Setter opp verdiene som skal sendes til server
        parametere = new HashMap<>();
        parametere.put("uuid", MainActivity.uuid);
        parametere.put("oktNr", FingerprintHjelper.OktNr);
        parametere.put("transaksjon", "true");
        parametere.put("signatur", FingerprintHjelper.pemSign);
    }

    @Override
    public Map<String, String> getParams(){
        return parametere;
    }
}
