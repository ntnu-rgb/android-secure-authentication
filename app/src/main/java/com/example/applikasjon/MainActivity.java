package com.example.applikasjon;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

   private String UuID = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

  //IF UUID IKKE ER SATT, SEND TIL LOGG INN, ELLERS GJØR DET EIKA VIL VI SKAL GJØRE

        if (UuID == null) {
            Toast.makeText(this, "Viderefører til autentisering", Toast.LENGTH_SHORT).show();
            this.startActivity(new Intent(this, FingerprintActivity.class));
        }

    }

}
