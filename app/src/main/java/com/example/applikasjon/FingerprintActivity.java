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
import android.widget.TextView;
import android.widget.Toast;
import android.app.LoaderManager.LoaderCallbacks;


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

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static java.lang.System.exit;

public class FingerprintActivity extends AppCompatActivity {


    private KeyStore fNokkel;
    public static final String KEYNAME = "NOKKEL";
    private Cipher cipher = null;
    private KeyPairGenerator parGenerator;
    private TextView tekst;
    private Signature signatur;
    private FingerprintManager mngr;


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
      //  ((InjectedApplication) getApplication()).inject(this);
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
                else { //Generer asymmetriske nøkler
                    genererNokler();
                }
                if (initSignatur()) {
                    FingerprintManager.CryptoObject cObjekt = new FingerprintManager.CryptoObject(signatur);
                    FingerprintHjelper hjelper = new FingerprintHjelper(this);
                    hjelper.startAutentisering(fManager, cObjekt);
                    Log.d("SUKSESS", "Suksess, redirekt til neste vindu");
                }
            }
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

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }  catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
       return false;
    }


    /**
     * Funksjon for å generere asymmetriske nøkler
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void genererNokler() {

        try {
            fNokkel = KeyStore.getInstance("AndroidKeyStore");  //Last inn en android keystore instance
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        try {
            parGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore"); //Hent et KeyPairGenerator objekt som genererer hemmelige nøkler for AES
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        //Oppretter asymmetriske nøkler
        try {
            fNokkel.load(null);                                 //Last inn keystore
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                parGenerator.initialize(
                        new KeyGenParameterSpec.Builder(KEYNAME,
                                KeyProperties.PURPOSE_SIGN)
                                .setDigests(KeyProperties.DIGEST_SHA256)
                                .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                                .setUserAuthenticationRequired(true)
                                .setUserAuthenticationValidityDurationSeconds(-1)
                                .build());
            }
            parGenerator.generateKeyPair();

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
