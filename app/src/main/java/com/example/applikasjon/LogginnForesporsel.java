package com.example.applikasjon;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class LogginnForesporsel extends StringRequest {

    private static final String LOGGINNURL = "https://folk.ntnu.no/sturlaba/sfa/";
    private Map<String, String> parametere;   //Brukes av Volley for å sende data til siden

    public LogginnForesporsel(String brukernavn, String passord, Response.Listener<String> listener) {
        super(Request.Method.POST, LOGGINNURL, listener, null);
        parametere = new HashMap<>();
        parametere.put("epost", brukernavn);
        parametere.put("passord", passord);
        parametere.put("offentlig_nokkel", "WIPsettinnnokkelher");
        parametere.put("forstegangsautentisering", "true");
    }

    @Override
    public Map<String, String> getParams(){
        return parametere;
    }

}
