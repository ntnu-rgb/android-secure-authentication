package com.example.applikasjon;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistrerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrer); //"Test"


        //Henter ut verdier fra XML filen
        final EditText brukerNavn = (EditText) findViewById(R.id.epost);
        final EditText pass = (EditText) findViewById(R.id.passord);
        final Button bRegistrer = (Button) findViewById(R.id.registrerOk);

        bRegistrer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String bnavn = brukerNavn.getText().toString();
                final String passord = pass.getText().toString();
                Response.Listener<String> respons = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String respons) {
                        Log.d("RESPONS", "UTHENTET"+respons);

                        JSONObject jsonRespons = null;
                        try {
                            jsonRespons = new JSONObject(respons);
                            boolean suksess = jsonRespons.getBoolean("suksess");

                            if (suksess) {
                               Intent intent = new Intent(RegistrerActivity.this, LogginnActivity.class);
                                RegistrerActivity.this.startActivity(intent);

                            }
                            else {
                                AlertDialog.Builder feil = new AlertDialog.Builder(RegistrerActivity.this);
                                String feilmelding = jsonRespons.getString("Feilmelding");
                                feil.setMessage("Feil ved innlogging").setNegativeButton(feilmelding, null).create().show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                RegistrerForesporsel regForesporsel = new RegistrerForesporsel(bnavn, passord, respons);
                RequestQueue queue = Volley.newRequestQueue(RegistrerActivity.this);             //Legg inn forespørselen i køen for å kjøre den
                queue.add(regForesporsel);
            }
        });
    }

}