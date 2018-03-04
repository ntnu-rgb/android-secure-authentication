package com.example.applikasjon;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.database.Cursor;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.app.LoaderManager.LoaderCallbacks;


import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static java.lang.System.exit;

public class FingerprintActivity extends AppCompatActivity {


    private KeyStore fNokkel;
    private static final String KEYNAME = "NOKKEL";
    private Cipher cipher = null;
    private TextView tekst;


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        KeyguardManager kManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fManager = (FingerprintManager)getSystemService(FINGERPRINT_SERVICE);

        //Sjekk at telefonen støtter lesing av fingeravtrykk
        if (!fManager.isHardwareDetected()) {
            Toast.makeText(this, "Fingeravtrykk autentisering er ikke aktivert", Toast.LENGTH_SHORT).show();
        }
        else {  //Sjekk at det finnes minst ett fingeravtrykk registrert på telefonen
            if (!fManager.hasEnrolledFingerprints()) {
                Toast.makeText(this, "Ingen fingeravtrykk registrert. Registrer fingeravtrykk i innstillinger", Toast.LENGTH_SHORT).show();
            }
            else { //Sjekker at det er sikkerhet rundt opplåsning av mobil.
                if (!kManager.isKeyguardSecure()) {
                    Toast.makeText(this, "Låseskjermen er ikke sikret", Toast.LENGTH_SHORT).show();
                }
                else { //Generer nøkkel
                    genererNokkel();
                }
                if (cipherInit()) {

                    FingerprintManager.CryptoObject cObject = new FingerprintManager.CryptoObject(cipher);
                    FingerprintHjelper hjelper = new FingerprintHjelper(this);
                    hjelper.startAutentisering(fManager, cObject);
                }
            }
        }

    }

    private boolean cipherInit() {
        try {

            //Hent ut et cipherobjekt som implementerer AES med CBC og PKCS7
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            }
            else return false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        try { //TODO: Fjern stacktracene før ferdigstilling

            if (cipher == null) {
                Log.d("myTag", "Cipher er fortsatt null...");
                return false;
            }

                fNokkel.load(null);                                                     //Last inn keystore
                SecretKey nokkel = (SecretKey) fNokkel.getKey(KEYNAME, null);        //Henter ut nøkkel fra keystore

                cipher.init(Cipher.ENCRYPT_MODE, nokkel);                                      //Initier cipher utfra uthentet nøkkel

                return true;
            } catch (IOException e1) { //Hvis en exception oppstår, return false.
                e1.printStackTrace();
                return false;

            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
                return false;

            } catch (CertificateException e1) {
                e1.printStackTrace();
                return false;

            } catch (UnrecoverableKeyException e1) {
                e1.printStackTrace();
                return false;

            } catch (KeyStoreException e1) {
                e1.printStackTrace();
                return false;

            } catch (InvalidKeyException e1) {
                e1.printStackTrace();
                return false;

            }
        }


    private void genererNokkel() {
        KeyGenerator nokkelGenerator = null;

        try {
            fNokkel = KeyStore.getInstance("AndroidKeyStore");  //Last inn en android keystore instance
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }


        try {
            nokkelGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"); //Hent et KeyGenerator objekt som genererer hemmelige nøkler for AES
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        try {
            fNokkel.load(null);                                 //Last inn keystore
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                nokkelGenerator.init(new KeyGenParameterSpec.Builder(KEYNAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build());
            }

            nokkelGenerator.generateKey();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

}
