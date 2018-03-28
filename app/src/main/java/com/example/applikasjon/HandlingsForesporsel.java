package com.example.applikasjon;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;

import static com.example.applikasjon.FingerprintActivity.pemOktKey;

/**
 *  Klasse for å gjøre klar en handlingsforespørsel før den sendes til server
 */
public class HandlingsForesporsel extends StringRequest {
    private static final String HANDLINGSURL = "https://folk.ntnu.no/sturlaba/sfa/";
    private Map<String, String> parametere;   //Brukes av Volley for å sende data til siden

    @RequiresApi(api = Build.VERSION_CODES.M)
    public HandlingsForesporsel(Response.Listener<String> listener, String transak, String sign) {
        super(Request.Method.POST, HANDLINGSURL, listener, null);

        Log.d("PARAMETERE I ØKT", MainActivity.uuid+"\n"+FingerprintHjelper.OktNr+"\n"+transak+sign);
        //Setter opp verdiene som skal sendes til server
        parametere = new HashMap<>();
        parametere.put("uuid", MainActivity.uuid);
        parametere.put("oktNr", FingerprintHjelper.OktNr);
        parametere.put("transaksjon", transak);
        parametere.put("signatur", sign);
    }

    @Override
    public Map<String, String> getParams(){
        return parametere;
    }


}
