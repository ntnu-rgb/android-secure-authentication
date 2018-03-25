package com.example.applikasjon;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Klasse for å gjøre klar en registreringsforespørsel før den sendes til server
 */
public class RegistrerForesporsel extends StringRequest {

    private static final String REGISTRERINGSURL = "https://folk.ntnu.no/sturlaba/sfa/";
    private Map<String, String> parametere;   //Brukes av Volley for å sende data til siden

    public RegistrerForesporsel(String brukernavn, String passord, Response.Listener<String> listener) {
        super(Method.POST, REGISTRERINGSURL, listener, null);
        parametere = new HashMap<>();
        parametere.put("epost", brukernavn);
        parametere.put("passord", passord);
        parametere.put("registrer", "true");
    }

    @Override
    public Map<String, String> getParams(){
        return parametere;
    }
}
