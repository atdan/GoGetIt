package com.example.root.gogetit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.root.gogetit.common.Common;
import com.example.root.gogetit.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;

import info.hoang8f.widget.FButton;
import io.paperdb.Paper;

public class SignInActivity extends AppCompatActivity {

    MaterialEditText phone_no,password;
    FButton signInBtn;
    CheckBox chkRemember;
    TextView txtForgotPassword;

    //firebase
     FirebaseDatabase database;
     DatabaseReference table_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        phone_no = findViewById(R.id.edtPhone);
        password = findViewById(R.id.edtPassword);

        signInBtn = findViewById(R.id.btn_signIn_activity);

        chkRemember = findViewById(R.id.ckbRememberMe);

        txtForgotPassword = findViewById(R.id.txtForgotPassword);

        //init paper
        Paper.init(this);


        //init Firebase
        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");


        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPasswordDialog();
            }
        });
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Common.isConnectedToInternet(getBaseContext())){

                    // TODO: save user and password

                    if (chkRemember.isChecked()){
                        Paper.book().write(Common.USER_KEY, phone_no.getText().toString());
                        Paper.book().write(Common.PASSWORD_KEY,password.getText().toString());
                    }



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
                else {
                    Toast.makeText(SignInActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }


            }
        });

    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Forgot Password");
        dialog.setMessage("Enter your secure code");

        LayoutInflater inflater = this.getLayoutInflater();
        View forgot_view = inflater.inflate(R.layout.forgot_password_layout,null);

        dialog.setView(forgot_view);
        dialog.setIcon(R.drawable.ic_security_black_24dp);

        final MaterialEditText edtPhone = forgot_view.findViewById(R.id.edtPhone);
        final MaterialEditText edtSecureCode = forgot_view.findViewById(R.id.edtSecureCode);

        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // check if user is available
                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.child(edtPhone.getText().toString()).getValue(User.class);

                        if (user.getSecureCode().equals(edtSecureCode.getText().toString())){
                            Toast.makeText(SignInActivity.this, "Your Password: " + user.getPassword(), Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(SignInActivity.this, "Sorry, Wrong Secure Code", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        dialog.show();
    }
}
