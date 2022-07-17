package com.example.notesandpasswordmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.MasterKeys;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import okio.Utf8;

import static com.example.notesandpasswordmanager.Password_details.bytesToHexString;

public class MainActivity extends AppCompatActivity {
   private Button submit;
   private EditText password_nameEdt,passwordEdt;
   private String password,password_name;
   private FirebaseFirestore db;
    KeyGenerator keyGenerator = null;


    public MainActivity() throws NoSuchProviderException, NoSuchAlgorithmException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        submit = findViewById(R.id.submit_button);
        passwordEdt = findViewById(R.id.password);
        password_nameEdt = findViewById(R.id.password_name);

        db = FirebaseFirestore.getInstance();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password = passwordEdt.getText().toString();
                password_name = password_nameEdt.getText().toString();

                // validating data of text fields if they are empty or not
                if(TextUtils.isEmpty(password)){
                    passwordEdt.setError("Please enter password");
                } else if (TextUtils.isEmpty(password_name)) {
                    password_nameEdt.setError("Please enter password name");
                }else{
                    String password_encpt= null;
                    try {
                        password_encpt = encrypt(password);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Error " + e.getMessage());
                    }

                    if(password_encpt==null) password_encpt="-1";
                    if(password_encpt=="-1" || password_name=="-1"){
                        Toast.makeText(MainActivity.this,"Could not upload",Toast.LENGTH_SHORT).show();

                    }else {
                        addDataToFirestore(password_name,password_encpt);
                    }

                }
            }
        });
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
    }
  String encrypt(String plainText) throws Exception {
      Cipher cipher= Cipher.getInstance("AES/CBC/PKCS7Padding");
      cipher.init(Cipher.ENCRYPT_MODE,getKey());
      byte[] ivBytes =cipher.getIV();
     byte[] encptBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
     return bytesToHexString(appendIvToEncryptedData(encptBytes,ivBytes));
  }

    private static byte[] appendIvToEncryptedData(byte[] eData, byte[] iv) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(eData);
        os.write(iv);
        return os.toByteArray();
    }
    public static String bytesToHexString(byte[] bytes) {
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
   /* private String encrypt_data(String text) {
        // Although you can define your own key generation parameter specification, it's
// recommended that you use the value specified here.


     String encryption_key = "1234";
      String encrypted;
        try {
            encrypted  = AESCrypt.encrypt(encryption_key,text);

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return "-1";
        }
        return encrypted;
    }*/


    private void addDataToFirestore(String password_name, String password) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
       CollectionReference dbReference = db.collection("Users").document(user.getUid()).collection("Password");
        //CollectionReference dbReference = db.collection("Passwords");
        Passwords passwords = new Passwords(password,password_name);
        dbReference.add(passwords).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(MainActivity.this,"Password Added Successfully",Toast.LENGTH_SHORT).show();
                Intent intent= new Intent(MainActivity.this,list_password.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Failed to add Password\n"+e,Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    }
