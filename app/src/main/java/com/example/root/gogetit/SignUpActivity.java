package com.example.root.gogetit;

import android.app.ProgressDialog;
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

public class SignUpActivity extends AppCompatActivity {

    MaterialEditText edtPhoneSignUp, edtNameSignUp, edtPasswordSignUp, edtSecureCodeSignUp;
    Button signUpBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtPhoneSignUp = findViewById(R.id.edtPhoneSignUp);
        edtNameSignUp = findViewById(R.id.edtNameSignUp);
        edtPasswordSignUp = findViewById(R.id.edtPasswordSignUp);
        signUpBtn = findViewById(R.id.btn_signUp_activity);
        edtSecureCodeSignUp = findViewById(R.id.edtSecureCodeSignUp);

        //init Firebase
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Common.isConnectedToInternet(getBaseContext())){
                    final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
                    progressDialog.setMessage("Please Wait...");
                    progressDialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // check the phone

                            if(dataSnapshot.child(edtPhoneSignUp.getText().toString()).exists()){
                                progressDialog.dismiss();

                                Toast.makeText(SignUpActivity.this, "Phone number already registered", Toast.LENGTH_SHORT).show();

                            }else {
                                progressDialog.dismiss();

                                User user = new User(edtNameSignUp.getText().toString(),
                                        edtPasswordSignUp.getText().toString(),
                                        edtSecureCodeSignUp.getText().toString());
                                table_user.child(edtPhoneSignUp.getText().toString()).setValue(user);
                                Toast.makeText(SignUpActivity.this, "Signed up successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    Toast.makeText(SignUpActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}
