package com.example.applikasjon;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Klasse som håndterer innlogging av bruker
 */
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
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View view) {
                    final String brukernavn = ePost.getText().toString();
                    final String pass = passord.getText().toString();

                    Response.Listener<String> respons = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonRespons = new JSONObject(response);
                                boolean suksess = jsonRespons.getBoolean("suksess");  //Henter ut verdien som sier om forespørselen var vellykket eller ikke
                                if (suksess) {
                                    String uuid = jsonRespons.getString("uuid");
                                    //Lagrer uuid
                                    MainActivity.setUuid(uuid);
                                    AlertDialog.Builder riktig = new AlertDialog.Builder(LogginnActivity.this);
                                    riktig.setMessage("Riktig innlogging").setNegativeButton("Overfør til neste vindu", null).create().show();

                                    Intent intent = new Intent(LogginnActivity.this, StartOkt.class); //TODO: Erstatt null med det logginn viderefører til
                                } else {
                                    MainActivity.visFeilMelding("Feil ved innlogging", LogginnActivity.this);
                                }
                            } catch (JSONException e) {
                                MainActivity.visFeilMelding("Feil ved lesing av serverdata", LogginnActivity.this);
                            }
                        }
                    };
                    //Setter opp en forespørsel som skal sendes til serveren via Volley
                    LogginnForesporsel foresporsel = new LogginnForesporsel(brukernavn, pass, respons, LogginnActivity.this);
                    RequestQueue queue = Volley.newRequestQueue(LogginnActivity.this);
                    queue.add(foresporsel);
                }
            });

            //Knapp som sender deg til registreringssiden ved klikk
            Button regKnapp = (Button) findViewById(R.id.registrerKnapp);
            regKnapp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View x) {
                    Intent regIntent = new Intent(LogginnActivity.this, RegistrerActivity.class);
                    LogginnActivity.this.startActivity(regIntent);
                }
            });
        }

    @Override
    public void onBackPressed() {
        //Ikke gjør noe
    }
}

