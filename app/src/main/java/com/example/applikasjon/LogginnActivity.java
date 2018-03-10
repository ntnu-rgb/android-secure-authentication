package com.example.applikasjon;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class LogginnActivity extends AppCompatActivity {


    // UI references.
    private AutoCompleteTextView ePost;
    private EditText passord;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logginn);
        // Set up the login form.
        ePost = (AutoCompleteTextView) findViewById(R.id.epost);
        passord = (EditText) findViewById(R.id.passord);

        final Button loggInnKnapp = (Button) findViewById(R.id.loggInnKnapp);


        loggInnKnapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("LOGGINN", "AKTIVERT");
                final String brukernavn = ePost.getText().toString();
                final String pass = passord.getText().toString();

                Response.Listener<String> respons = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonRespons = new JSONObject(response);
                            boolean suksess = jsonRespons.getBoolean("suksess");
                            Log.d("RESPONS", "UTHENTET"+response);

                            if (suksess) { //TODO: Meldingen her fjernes da testingen er over, bytt til riktig funksjonalitet


                                String uuid = jsonRespons.getString("uuid");

                                //Setter i gang editor for å lagre uuid

                                SharedPreferences.Editor editor = MainActivity.pref.edit();
                                editor.putString(getString(R.string.lagret_uuid), uuid);
                                editor.commit();

                                AlertDialog.Builder riktig = new AlertDialog.Builder(LogginnActivity.this);

                                riktig.setMessage("Riktig innlogging").setNegativeButton("Overfør til neste vindu", null).create().show();


                                //Intent intent = new Intent(LogginnActivity.this, null); //TODO: Erstatt null med det logginn viderefører til

                            }
                            else {
                                AlertDialog.Builder feil = new AlertDialog.Builder(LogginnActivity.this);
                                String feilmelding = jsonRespons.getString("Feilmelding");
                                feil.setMessage("Feil ved innlogging").setNegativeButton(feilmelding, null).create().show();
                            }
                        } catch (JSONException e) {
                            Log.d("JSONFEIL", "JSON kunne ikke leses");
                        }
                    }
                };

                LogginnForesporsel foresporsel = new LogginnForesporsel(brukernavn, pass, respons);
                RequestQueue queue = Volley.newRequestQueue(LogginnActivity.this);
                queue.add(foresporsel);
            }
        });





        //Knapp som sender deg til registreringssiden ved klikk
        Button regKnapp = (Button) findViewById(R.id.registrerKnapp);
        regKnapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View x) {
                Log.d("REGISTRER", "AKTIVERT");
             Intent regIntent = new Intent (LogginnActivity.this, RegistrerActivity.class);
             LogginnActivity.this.startActivity(regIntent);
            }
        });
    }


    @Override
    public void onBackPressed() {
        //Ikke gjør noe
    }



}

