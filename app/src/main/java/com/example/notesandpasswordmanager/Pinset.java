package com.example.notesandpasswordmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chaos.view.PinView;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Pinset extends AppCompatActivity {
  PinView pin1;
  PinView pin2;
  Button submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinset);

        pin1= findViewById(R.id.pinview1);
        pin2= findViewById(R.id.pinview2);
        submit= findViewById(R.id.btn_pin);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String p1= pin1.getText().toString();
                String p2= pin2.getText().toString();
                if(p1.trim().length()<4){
                    Toast.makeText(getApplicationContext(),"Pin must be of 4 digit",Toast.LENGTH_SHORT).show();
                }else if(!p1.equals(p2)){
                    Toast.makeText(getApplicationContext(),"Pin doest not matched",Toast.LENGTH_SHORT).show();
                }else{

                    String masterKeyAlias = null;
                    try {
                        masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    SharedPreferences sharedPreferences = null;
                    try {
                        sharedPreferences = EncryptedSharedPreferences.create(
                                "secret_shared_prefs",
                                masterKeyAlias,
                                Pinset.this,
                                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                        );
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

// use the shared preferences and editor as you normally would
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("MyPin", p1);
                    editor.apply();
                    Toast.makeText(getApplicationContext(),"Pin saved successfully",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Pinset.this,list_password.class));
                }
            }
        });
    }
}