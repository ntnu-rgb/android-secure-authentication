package com.example.applikasjon;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Klasse for å utføre en handling i en økt
 */
public class UtforHandlingActivity extends AppCompatActivity {

    /**
     * Constructor som oppretter transaksjonen og sender den til serveren.
     * Håndterer også svaret fra server, sender tilbake til fingerprinting hvis økten har utløpt for å opprette ny økt
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utfor_handling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Button utfor = (Button) findViewById(R.id.utfor);
        //Når man trykker på utfor knappen skal det sendes en forespørsel til server
        utfor.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                Response.Listener<String> respons = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String respons) {
                        JSONObject jsonRespons = null;
                        try {
                            jsonRespons = new JSONObject(respons);
                            boolean suksess = jsonRespons.getBoolean("suksess");
                            if (suksess) { //Hvis serveren har returnert suksess
                                MainActivity.visMelding("Suksess! Transaksjonen har blitt gjennomført. Økten utløper: "+jsonRespons.getString("utloper"), UtforHandlingActivity.this);
                            }
                            else { //Hvis serveren returnerer suksess=false vises en feilmelding
                                MainActivity.visFeilMelding("Utforhandling"+jsonRespons.toString(), UtforHandlingActivity.this );
                            }
                        } catch (JSONException e) {
                            MainActivity.visFeilMelding("En feil har oppstått ved serverkommunikasjon", UtforHandlingActivity.this);
                        }
                    }
                };
                //Setter opp en forespørsel som skal sendes til serveren via Volley
                JSONObject transaksjon = opprettTransaksjon();
                String handlingSign = FingerprintHjelper.sign(transaksjon.toString());
                if (handlingSign == null) {
                    MainActivity.visFeilMelding("Feil ved autentisering", UtforHandlingActivity.this);
                    }
                else {
                    HandlingsForesporsel hForesporsel = new HandlingsForesporsel(respons, transaksjon.toString(), handlingSign);
                    RequestQueue queue = Volley.newRequestQueue(UtforHandlingActivity.this);             //Legg inn forespørselen i køen for å kjøre den
                    queue.add(hForesporsel);
                }
            }
        });
    }

    /**
     * Oppretter en transaksjon som inneholder en handling og et tidspunkt
     * @return JSONObject bestående av handlingen og tidspunktet
     */
    public JSONObject opprettTransaksjon() {
        Map<String, String> res = new HashMap<>();
        res.put("Handling", "Transaksjon gjennomført ");
        res.put("tidspunkt", Calendar.getInstance().getTime().toString());
        return new JSONObject(res);
    }
    /**
     * Overrider tilbakeknappen så den ikke kan trykkes for å gå bakover i autentiseringen
     */
    @Override
    public void onBackPressed() {
        //Ikke gjør noe
    }

}
