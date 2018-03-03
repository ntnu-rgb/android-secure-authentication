package com.example.applikasjon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class RegistrerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrer); //"Test"


        //Henter ut verdier fra XML filen
        final EditText brukerNavn = (EditText) findViewById(R.id.epost);
        final EditText pass = (EditText) findViewById(R.id.passord);
        final Button bRegistrer = (Button) findViewById(R.id.registrerOk);


    }

}