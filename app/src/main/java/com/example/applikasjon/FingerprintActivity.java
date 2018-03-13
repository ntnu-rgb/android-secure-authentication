package com.example.applikasjon;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;

public class FingerprintActivity extends AppCompatActivity {


    public static KeyStore fNokkel;
    public static final String KEYNAME = "NOKKEL";
    public static KeyPairGenerator parGenerator;
    private Signature signatur;


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        KeyguardManager kManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fManager = (FingerprintManager)getSystemService(FINGERPRINT_SERVICE);

        //Sjekk at telefonen støtter lesing av fingeravtrykk
        if (!fManager.isHardwareDetected()) {
            Toast.makeText(this, "Fingeravtrykk autentisering er ikke aktivert", Toast.LENGTH_SHORT).show();
        }
        else if (!fManager.hasEnrolledFingerprints()) { //Sjekk at det finnes minst ett fingeravtrykk registrert på telefonen
                Toast.makeText(this, "Ingen fingeravtrykk registrert. Registrer fingeravtrykk i innstillinger", Toast.LENGTH_SHORT).show();
        }
        else if(!kManager.isKeyguardSecure()){ //Sjekker at det er sikkerhet rundt opplåsning av mobil.
        Toast.makeText(this, "Låseskjermen er ikke sikret", Toast.LENGTH_SHORT).show();
        }
        else { //Generer asymmetriske nøkler
            genererNokler(null);
        }
        if (initSignatur()) {
            FingerprintManager.CryptoObject cObjekt = new FingerprintManager.CryptoObject(signatur);
            FingerprintHjelper hjelper = new FingerprintHjelper(this);
            hjelper.startAutentisering(fManager, cObjekt);
        }

    }


    private boolean initSignatur() {
        try {
            //Initialiser signaturen ved hjelp av privatnøkkelen
            fNokkel.load(null);
            signatur = Signature.getInstance("SHA256withECDSA");
            PrivateKey priv = (PrivateKey) fNokkel.getKey(KEYNAME, null);
            signatur.initSign(priv);
            return true;

        } catch (NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableKeyException | KeyStoreException | InvalidKeyException e) {
           Log.d("FEIL", "Kunne ikke initiere signatur");
        }
       return false;
    }


    /**
     * Funksjon for å generere asymmetriske nøkler
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void genererNokler(String name) {

        try {
            fNokkel = KeyStore.getInstance("AndroidKeyStore");  //Last inn en android keystore instance
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        try {
            parGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore"); //Hent et KeyPairGenerator objekt som genererer hemmelige nøkler for AES
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }


        //Oppretter asymmetriske nøkler
        try {
            fNokkel.load(null);                                 //Last inn keystore
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                parGenerator.initialize(
                        new KeyGenParameterSpec.Builder((name == null) ? KEYNAME : name,
                                KeyProperties.PURPOSE_SIGN)
                                .setDigests(KeyProperties.DIGEST_SHA256)
                                .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                                .setUserAuthenticationRequired(true)
                                .setUserAuthenticationValidityDurationSeconds(-1)
                                .build());
            }
            parGenerator.generateKeyPair();

        } catch (IOException | NoSuchAlgorithmException | CertificateException | InvalidAlgorithmParameterException e) {
            Log.d("FEIL", "Kunne ikke generere nøkler i FingerprintActivity");
        }
    }
}
