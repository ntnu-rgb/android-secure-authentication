package com.example.applikasjon;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Signature;
import java.util.Calendar;

/**
 * Klasse for å utføre en handling i en økt
 */
public class UtforHandlingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utfor_handling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Button utfor = (Button) findViewById(R.id.utfor);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab); //TODO: nødvendig?
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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
                                //TODO
                                MainActivity.visMelding(jsonRespons.toString(), UtforHandlingActivity.this);

                            }
                            else { //Hvis serveren returnerer suksess=false vises en feilmelding
                                MainActivity.visFeilMelding("Utforhandling"+jsonRespons.toString(), UtforHandlingActivity.this );
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();  //TODO: Fjern før ferdigstilling

                        }
                    }
                };
                //Setter opp en forespørsel som skal sendes til serveren via Volley
                String transaksjon = opprettTransaksjon();

                String handlingSign = FingerprintHjelper.sign(transaksjon);
                if (handlingSign == null) {
                    MainActivity.visFeilMelding("Feil ved autentisering", UtforHandlingActivity.this);
                    }
                else {
                    HandlingsForesporsel hForesporsel = new HandlingsForesporsel(respons, transaksjon, handlingSign); //TODO endre parameterverdier
                    RequestQueue queue = Volley.newRequestQueue(UtforHandlingActivity.this);             //Legg inn forespørselen i køen for å kjøre den
                    queue.add(hForesporsel);
                }
            }
        });
    }

    public String opprettTransaksjon() {
        String handling = "TODO";
        String tidspunkt = (Calendar.getInstance().getTime()).toString();
        return handling+tidspunkt;
    }

    @Override
    public void onBackPressed() {
        //Ikke gjør noe
    }

}
