package com.example.notesandpasswordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.scottyab.aescrypt.AESCrypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Password_details extends AppCompatActivity {
    private TextView password,password_name;
    KeyGenerator keyGenerator = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_details);
        password = findViewById(R.id.password_detail_password);
        password_name = findViewById(R.id.password_detail_name);

        Intent intent = getIntent();
        String password_dec = null;
        try {
            password_dec = decrypt(intent.getExtras().getString("password"));
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            System.out.println("Error " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("Error " + e.getMessage());
        } catch (KeyStoreException e) {
            e.printStackTrace();
            System.out.println("Error " + e.getMessage());
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
            System.out.println("Error " + e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            System.out.println("Error " + e.getMessage());
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            System.out.println("Error " + e.getMessage());
        } catch (BadPaddingException e) {
            e.printStackTrace();
            System.out.println("Error " + e.getMessage());
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            System.out.println("Error " + e.getMessage());
        } catch (CertificateException e) {
            e.printStackTrace();
            System.out.println("Error " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error " + e.getMessage());
        }
        String password_name_recieved = intent.getExtras().getString("password_name");
       password.setText(password_dec);
       password_name.setText(password_name_recieved);

       //

        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        KeyGenParameterSpec kgps= new KeyGenParameterSpec.Builder("MyAlias",KeyProperties.PURPOSE_ENCRYPT |KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build();
        try {
            keyGenerator.init(kgps);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }


    }

    SecretKey getKey() throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null,null);
        if(!keyStore.containsAlias("MyAlias")){
            keyGenerator.generateKey();
        }


       KeyStore.SecretKeyEntry secretKeyEntry= (KeyStore.SecretKeyEntry) keyStore.getEntry("MyAlias",null );
        return secretKeyEntry.getSecretKey();
       /// return (SecretKey) keyStore.getKey("MyAlias",null);
    }

   /* private String decrypt(String text) {
        String key_dec ="1234";
        String  dec ;
        try {
            dec = AESCrypt.decrypt(key_dec,text);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return "-1";
        }
        return  dec;
    }*/
    String decrypt(String text) throws NoSuchPaddingException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableEntryException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, CertificateException, IOException {
        byte [] decoded_text= hexStringToBytes(text);
        ArrayList<byte[]> al = retreiveIvFromByteArray(decoded_text);
        byte[] eData = al.get(0);
        byte[] iv = al.get(1);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        IvParameterSpec ivParameterSpec= new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE,getKey(),ivParameterSpec);
        return new String(cipher.doFinal(eData),"UTF-8");

    }
    private static ArrayList<byte[]> retreiveIvFromByteArray(byte[] dataPlusIv) {
        ByteArrayOutputStream iv = new ByteArrayOutputStream(16);
        ByteArrayOutputStream eData = new ByteArrayOutputStream();

        iv.write(dataPlusIv, dataPlusIv.length - 16, 16);
        eData.write(dataPlusIv, 0, dataPlusIv.length - 16);

        ArrayList<byte[]> al = new ArrayList<byte[]>();
        al.add(eData.toByteArray());
        al.add(iv.toByteArray());

        return al;
    }
    public static byte[]
    hexStringToBytes(String s) {
        byte[] ret;

        if (s == null) return null;

        int sz = s.length();

        ret = new byte[sz/2];

        for (int i=0 ; i <sz ; i+=2) {
            ret[i/2] = (byte) ((hexCharToInt(s.charAt(i)) << 4)
                    | hexCharToInt(s.charAt(i+1)));
        }

        return ret;
    }
    static int
    hexCharToInt(char c) {
        if (c >= '0' && c <= '9') return (c - '0');
        if (c >= 'A' && c <= 'F') return (c - 'A' + 10);
        if (c >= 'a' && c <= 'f') return (c - 'a' + 10);

        throw new RuntimeException ("invalid hex char '" + c + "'");
    }
    public static String
    bytesToHexString(byte[] bytes) {
        if (bytes == null) return null;

        StringBuilder ret = new StringBuilder(2*bytes.length);

        for (int i = 0 ; i < bytes.length ; i++) {
            int b;

            b = 0x0f & (bytes[i] >> 4);

            ret.append("0123456789abcdef".charAt(b));

            b = 0x0f & bytes[i];

            ret.append("0123456789abcdef".charAt(b));
        }

        return ret.toString();
    }
}