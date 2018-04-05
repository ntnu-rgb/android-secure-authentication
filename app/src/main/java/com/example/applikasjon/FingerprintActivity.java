package com.example.applikasjon;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.KeyPair;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;

import static java.lang.System.exit;

/**
 * Klasse som håndterer fingeravtrykksautentisering
 */
public class FingerprintActivity extends AppCompatActivity {

    public static KeyStore fNokkel;
    public static KeyPair permpar = null;
    public static String pemOktKey = null;
    public static KeyPair oktpar = null;
    public static final String KEYNAME = "NOKKEL";
    public static KeyPairGenerator parGenerator;
    public static Signature signatur;
    /**
     * Constructor som setter variabler, sjekker at finger
     */
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
            MainActivity.visFeilMelding(this.getString(R.string.ingenfingerautentisering), this);
        }
        else if (!fManager.hasEnrolledFingerprints()) { //Sjekk at det finnes minst ett fingeravtrykk registrert på telefonen
            MainActivity.visFeilMelding(this.getString(R.string.manglerfingeravtrykk), this);
        }
        else if(!kManager.isKeyguardSecure()){ //Sjekker at det er sikkerhet rundt opplåsning av mobil.
            MainActivity.visFeilMelding(this.getString(R.string.laaseskjerm), this);
        }
        else if (!isNetworkAvailable()) {
            MainActivity.visFeilMelding(this.getString(R.string.internettfeil), this);
        }
        else { //Generer asymmetriske nøkler
            try {
                fNokkel = KeyStore.getInstance("AndroidKeyStore");
                fNokkel.load(null);
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
                MainActivity.visFeilMelding("En feil har oppstått", this);
            }
            if (MainActivity.uuid == null) { //Sjekker om brukeren har gjennomført førstegangsautentisering
                genererNokler(null);
            }
            genererNokler("OktNokkel"); //TEST, fjern sjekken hvis dette ikke fungerer
            if ((initSignatur(KEYNAME)) != null) {
                FingerprintManager.CryptoObject cObjekt = new FingerprintManager.CryptoObject(signatur);
                FingerprintHjelper hjelper = new FingerprintHjelper(this);
                try { //Setter opp pemOktKey på et format som kan leses av server
                    pemOktKey = "-----BEGIN PUBLIC KEY-----\n"+android.util.Base64.encodeToString(fNokkel.getCertificate("OktNokkel").getPublicKey().getEncoded(), android.util.Base64.DEFAULT)+"-----END PUBLIC KEY-----";
                } catch (KeyStoreException e) {
                    MainActivity.visFeilMelding("En feil har oppstått", FingerprintActivity.this);
                }
                hjelper.startAutentisering(fManager, cObjekt); //Sender objektene videre til autentiseringen
                initSignatur("OktNokkel"); //TEST: Authenticated?
            } else exit(0);
        }
    }

    /**
     * Gjør klar en signatur til signering
     * @param nokkelnavn String Navnet til nøkkelen som skal hentes ut
     * @return Signature signatur
     */
    public Signature initSignatur(String nokkelnavn) {
        try {
            //Initialiser signaturen ved hjelp av privatnøkkelen
            signatur = Signature.getInstance("SHA256withECDSA");
            PrivateKey priv = (PrivateKey) fNokkel.getKey(nokkelnavn, null);
            signatur.initSign(priv);
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | InvalidKeyException e) {
            MainActivity.visFeilMelding(this.getString(R.string.feil), this);
        }
       return signatur;
    }

    /**
     * Funksjon for å generere asymmetriske nøkler
     * Skiller mellom generering av øktnøkler og de permanente nøklene
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void genererNokler(String name) {
        try {
            parGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore"); //Hent et KeyPairGenerator objekt som genererer hemmelige nøkler for AES
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            System.exit(0);
        }
        //Oppretter asymmetriske nøkler
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (name == null) {  //null = generer det permanente paret
                    parGenerator.initialize(
                            new KeyGenParameterSpec.Builder(KEYNAME,
                                    KeyProperties.PURPOSE_SIGN)
                                    .setDigests(KeyProperties.DIGEST_SHA256)
                                    .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                                    .setUserAuthenticationRequired(true)
                                    .setUserAuthenticationValidityDurationSeconds(-1)
                                    .build());
                    permpar = parGenerator.generateKeyPair();
                }
                else { //generer øktparet
                    parGenerator.initialize(
                        new KeyGenParameterSpec.Builder(name,
                                KeyProperties.PURPOSE_SIGN)
                                .setDigests(KeyProperties.DIGEST_SHA256)
                                .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                                .setUserAuthenticationRequired(false)
                                .build());
                    oktpar = parGenerator.generateKeyPair();
                }
            }
        } catch ( InvalidAlgorithmParameterException e) {
            System.exit(0);
        }
    }
    /**
     * Sjekker om man har en aktiv tilkobling til internett
     * Hentet fra https://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
     * @return boolean true hvis man har tilkobling til internett
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Hindrer brukeren fra å gå bakover i autentiseringen
     */
    @Override
    public void onBackPressed() {
        //Ikke gjør noe
    }

}
