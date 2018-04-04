package com.example.applikasjon;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Klasse som håndterer registrering av en bruker
 */
public class RegistrerActivity extends AppCompatActivity {

    /**
     * Constructor som håndterer kommunikasjon med server når registreringsknappen blir trykket
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrer); //"Test"

        //Henter ut verdier fra XML filen
        final EditText brukerNavn = (EditText) findViewById(R.id.epost);
        final EditText pass = (EditText) findViewById(R.id.passord);
        final Button bRegistrer = (Button) findViewById(R.id.registrerOk);

        //Setter opp en clicklistener som utfører registreringen når knappen blir trykket på
        bRegistrer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String bnavn = brukerNavn.getText().toString();
                final String passord = pass.getText().toString();
                Response.Listener<String> respons = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String respons) {
                        JSONObject jsonRespons = null;
                        try {
                            jsonRespons = new JSONObject(respons);
                            boolean suksess = jsonRespons.getBoolean("suksess");
                            if (suksess) { //Hvis serveren har returnert suksess starter innloggingen
                               Intent intent = new Intent(RegistrerActivity.this, LogginnActivity.class);
                               RegistrerActivity.this.startActivity(intent);
                            }
                            else { //Hvis serveren returnerer suksess=false vises en feilmelding
                                MainActivity.visFeilMelding(jsonRespons.getString("Feilmelding"), RegistrerActivity.this);
                            }
                        } catch (JSONException e) {
                            MainActivity.visFeilMelding("En feil har oppstått", RegistrerActivity.this);
                        }
                    }
                };
                //Setter opp en forespørsel som skal sendes til serveren via Volley
                RegistrerForesporsel regForesporsel = new RegistrerForesporsel(bnavn, passord, respons);
                RequestQueue queue = Volley.newRequestQueue(RegistrerActivity.this);             //Legg inn forespørselen i køen for å kjøre den
                queue.add(regForesporsel);
            }
        });
    }

}