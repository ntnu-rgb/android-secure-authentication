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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        uuid = getUuid();

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
                System.exit(0);
            }
        }).create().show();
    }

    /**
     * Lagrer uuid i sharedpreferences
     */
    public static void setUuid(String id) {
        SharedPreferences.Editor editor = MainActivity.pref.edit();
        editor.putString(Resources.getSystem().getString(R.string.lagret_uuid), id);
        editor.commit();
        uuid = id;
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


}
