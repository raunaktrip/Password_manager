package com.example.notesandpasswordmanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class list_password extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<Passwords> list;
    private Password_adapter adapter;
    private FirebaseFirestore db;
    ProgressBar progressBar;
    FloatingActionButton btn_float;
    TextView noData_tv;
    private static final  int REQUEST_CODE=123;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_password);

        recyclerView = findViewById(R.id.rec_password);
        progressBar = findViewById(R.id.progress_bar_password);
        btn_float = findViewById(R.id.add_password_activity_list);
        db = FirebaseFirestore.getInstance();
        noData_tv = findViewById(R.id.noData_tv);
        noData_tv.setVisibility(View.GONE);


        list = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new Password_adapter(list, this);
        recyclerView.setAdapter(adapter);

        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        db.collection("Users").document(user.getUid()).collection("Password").orderBy("password_name",Query.Direction.ASCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            progressBar.setVisibility(View.GONE);
                            List<DocumentSnapshot> list_snp = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot d : list_snp) {
                                Passwords p = d.toObject(Passwords.class);
                                list.add(p);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                           // Toast.makeText(list_password.this, "No Data found in database", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.GONE);
                            noData_tv.setVisibility(View.VISIBLE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(list_password.this, "Failed to get data", Toast.LENGTH_SHORT).show();
            }
        });
        // setting onclickListener on add_btn
        btn_float.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(list_password.this,MainActivity.class);
                //startActivity(intent);
                //finish();
                startActivityForResult(intent,REQUEST_CODE);

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =  getMenuInflater();
        inflater.inflate(R.menu.logout_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout_menu:
                FirebaseAuth.getInstance().signOut();
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
                mGoogleSignInClient.signOut().addOnCompleteListener(this,
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(list_password.this,SigninActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });



        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE){
            Intent i = new Intent(list_password.this,list_password.class);
            finish();
            overridePendingTransition(0, 0);
            startActivity(i);
            
            overridePendingTransition(0, 0);
        }
    }
}
