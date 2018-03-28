package com.example.applikasjon;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
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

import java.security.Signature;
import java.security.SignatureException;

import static com.example.applikasjon.MainActivity.uuid;

/**
 * Klasse som håndterer innlogging av bruker
 */
public class LogginnActivity extends AppCompatActivity {

    // UI references.
    private AutoCompleteTextView ePost;
    private EditText passord;
    private StartOkt okt = null;

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
                    loggInnKnapp.setEnabled(false);  //Skrur av knappen mens forespørselen prosesserer
                    final String brukernavn = ePost.getText().toString();
                    final String pass = passord.getText().toString();
                    Response.Listener<String> responsOkt = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String res) {
                            JSONObject jsonRes = null;
                            try {
                                jsonRes = new JSONObject(res);
                                Log.d("TESTJSON-ØKTNUMMER", jsonRes.toString());
                                boolean suksess = jsonRes.getBoolean("suksess");
                                if (suksess) {
                                    FingerprintHjelper.OktNr = jsonRes.getString("oktNr");
                                    Intent regIntent = new Intent(LogginnActivity.this, UtforHandlingActivity.class);
                                    LogginnActivity.this.startActivity(regIntent);
                                }
                                else {
                                    FingerprintHjelper.OktNr = null;
                                    MainActivity.visFeilMelding("En feil har oppstått"+FingerprintHjelper.kryptOb, LogginnActivity.this);
                                    loggInnKnapp.setEnabled(true); //Setter på igjen knappen
                                    Intent regIntent = new Intent(LogginnActivity.this, LogginnActivity.class);
                                    LogginnActivity.this.startActivity(regIntent);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();  //TODO: Fjern før ferdigstilling
                            }
                        }
                    };
                    Response.Listener<String> respons = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonRespons = new JSONObject(response);
                                boolean suksess = jsonRespons.getBoolean("suksess");  //Henter ut verdien som sier om forespørselen var vellykket eller ikke
                                Log.d("TESTJSON", jsonRespons.toString());

                                if (suksess) {
                                    String uuid = jsonRespons.getString("uuid");
                                    //Lagrer uuid
                                    MainActivity.setUuid(uuid);
                                    //AlertDialog.Builder riktig = new AlertDialog.Builder(LogginnActivity.this);
                                    //  riktig.setMessage("Riktig innlogging").setNegativeButton("Overfør til neste vindu", null).create().show();

                                    Signature signatur = FingerprintHjelper.kryptOb.getSignature();  //TODO: Endre de neste linjene ved å kalle sign funksjonen i stedet
                                    try {
                                        byte[] forSigning = FingerprintActivity.pemOktKey.getBytes();
                                        signatur.update(forSigning);
                                        byte[] signert = signatur.sign();
                                        FingerprintHjelper.pemSign = Base64.encodeToString(signert, Base64.DEFAULT);
                                    } catch (SignatureException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    MainActivity.visFeilMelding("Kunne ikke oppdatere data", LogginnActivity.this);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            okt = new StartOkt(FingerprintHjelper.pemSign, responsOkt, LogginnActivity.this);
                            RequestQueue q = Volley.newRequestQueue(LogginnActivity.this);
                            q.add(okt);

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

