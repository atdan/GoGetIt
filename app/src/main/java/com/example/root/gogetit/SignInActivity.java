package com.example.root.gogetit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.root.gogetit.common.Common;
import com.example.root.gogetit.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import info.hoang8f.widget.FButton;

public class SignInActivity extends AppCompatActivity {

    MaterialEditText phone_no,password;
    FButton signInBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        phone_no = findViewById(R.id.edtPhone);
        password = findViewById(R.id.edtPassword);

        signInBtn = findViewById(R.id.btn_signIn_activity);


        //init Firebase
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog progressDialog = new ProgressDialog(SignInActivity.this);
                progressDialog.setMessage("Please Wait...");
                progressDialog.show();
                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        // To check if user does not exist in db
                        if (dataSnapshot.child(phone_no.getText().toString()).exists()) {


                            // Get user info

                            progressDialog.dismiss();
                            User user = dataSnapshot.child(phone_no.getText().toString()).getValue(User.class);
                            user.setPhone(phone_no.getText().toString()); // set phone
                            if (user.getPassword().equals(password.getText().toString())) {
                                {
                                    Toast.makeText(SignInActivity.this, "Signed in successfully", Toast.LENGTH_SHORT).show();
                                    Intent homeIntent = new Intent(SignInActivity.this, HomeActivity.class);
                                    Common.current_user = user;
                                    startActivity(homeIntent);
                                    finish();
                                }



                            } else {
                                Toast.makeText(SignInActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();

                            }
                        }else {
                            progressDialog.dismiss();
                            Toast.makeText(SignInActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }
}
