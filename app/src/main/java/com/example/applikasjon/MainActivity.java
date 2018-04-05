package com.example.applikasjon;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Hovedklassen for å starte appen og sette i gang riktig aktivitet
 */
public class MainActivity extends AppCompatActivity {

   public static SharedPreferences pref = null;
   public static String uuid = null;
   public static Context ct = null;
   public static String OktNr = null;
   public static final String HandlingsURL = "https://example.com"; //Endre denne til riktig server

    /**
     * Constructor som setter opp de forskjellige variablene som MainActivity bruker og sender brukeren videre til FingerprintActivity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ct = this.getApplicationContext();
        uuid = getUuid();
        OktNr = getOktNr();

        //Starter opp aktiviteten for fingeravtrykk autentisering
        this.startActivity(new Intent(this, FingerprintActivity.class));
    }

    /**
     * Brukes av forskjellige klasser for å vise feilmelding
     * @param str String Innholdet i feilmeldingen
     * @param con Context Konteksten som feilmeldingen skal vises i
     */
    public static void visFeilMelding(String str, Context con) {
        AlertDialog.Builder feil = new AlertDialog.Builder(con);
        feil.setMessage(str).setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                con.startActivity(new Intent(con, FingerprintActivity.class));
            }
        }).create().show();
    }

    /**
     * Lagrer uuid i sharedpreferences
     * @param id String Medsendt uuid
     */
    public static void setUuid(String id) {
        pref = ct.getSharedPreferences("FingerPrintAuth", Context.MODE_PRIVATE);
        try {
            SharedPreferences.Editor editor = MainActivity.pref.edit();
            editor.putString("lagretuuid", id); //Bruker plaintext på grunn av static method
            editor.commit();
            uuid = id;
        } catch (Resources.NotFoundException e){
            MainActivity.visFeilMelding("En feil har oppstått", MainActivity.ct);
        }
    }

    /**
     * Henter ut uuid
     * @return String uuid som brukes for å identifisere bruker
     */
    public String getUuid() {
        pref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if (uuid != null) {
            return uuid;
        }
        else return pref.getString(getString(R.string.lagret_uuid), null);
    }

    /**
     * Henter ut øktnummer
     * @return String OktNr som brukes for å identifisere en økt. null hvis den ikke finnes
     */
    public String getOktNr() {
        if (OktNr != null) {
            return OktNr;
        }
        else return null;
    }

    /**
     * Viser en melding uten å avslutte programmet.
     * Brukes for informasjonsmeldinger, ikke feilmeldinger.
     * @param s String Meldingen som skal vises
     * @param con Context Konteksten som meldingen skal vises i
     */
    public static void visMelding(String s, Context con) {
        AlertDialog.Builder feil = new AlertDialog.Builder(con);
        feil.setMessage(s).setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Ikke gjør noe
            }
        }).create().show();
    }
}
