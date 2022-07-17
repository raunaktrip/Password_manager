package com.example.notesandpasswordmanager;

import android.content.Context;
import android.content.Intent;
import android.icu.number.CompactNotation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.ArrayList;

public class Password_adapter extends RecyclerView.Adapter<Password_adapter.ViewHolder>{

    private  ArrayList<Passwords>  list;
    private Context context;
    // constructor
    public  Password_adapter(ArrayList<Passwords> list,Context context){
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_password,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull  Password_adapter.ViewHolder holder, int position) {
      Passwords passwords = list.get(position);
      //String password_name_dec = decrypt_text(passwords.getPassword_name());
      holder.password_name.setText(passwords.getPassword_name());


      holder.itemView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Intent intent = new Intent(context,Password_details.class);
              intent.putExtra("password_name",passwords.getPassword_name());
              intent.putExtra("password",passwords.getPassword());
              context.startActivity(intent);
          }
      });
    }

  /*  private String decrypt_text(String text) {
        String dec_key = "1234";
        String  decrypted;
        try {
            decrypted = AESCrypt.decrypt(dec_key,text);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return  "-1";
        }
        return  decrypted;
    }*/

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
      private TextView password_name;

        public ViewHolder(@NonNull  View itemView) {
            super(itemView);
            password_name = itemView.findViewById(R.id.item_password_tv);

        }
    }
}
