package com.example.root.gogetit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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

import info.hoang8f.widget.FButton;
import io.paperdb.Paper;


public class MainActivity extends AppCompatActivity {

    FButton btnSignUp, btnSignIn;

    TextView txtSlogan, txtlogo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = findViewById(R.id.btn_signIn);
        btnSignUp = findViewById(R.id.btn_signUp);

        txtSlogan = findViewById(R.id.textSlogan);
        txtlogo = findViewById(R.id.textLogo);

        Typeface typeface = Typeface.createFromAsset(getAssets(),"fonts/Pacifico.ttf");
        txtSlogan.setTypeface(typeface);
        txtlogo.setTypeface(typeface);

        //init paper
        Paper.init(this);



        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this,SignInActivity.class);
                startActivity(intent);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this,SignUpActivity.class);
                startActivity(intent);
            }
        });

        //check user
        String user = Paper.book().read(Common.USER_KEY);
        String password = Paper.book().read(Common.PASSWORD_KEY);
        if (user != null  && password != null){
            if (!user.isEmpty() && !password.isEmpty())
                login(user,password);
        }

    }

    private void login(final String user_phone, final String password) {

        //copy login code from ssignin class
        //init Firebase
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        if (Common.isConnectedToInternet(getBaseContext())){

            final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();
            table_user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    // To check if user does not exist in db
                    if (dataSnapshot.child(user_phone).exists()) {


                        // Get user info

                        progressDialog.dismiss();
                        User user = dataSnapshot.child(user_phone).getValue(User.class);
                        user.setPhone(user_phone); // set phone
                        if (user.getPassword().equals(password)) {
                            {
                                Toast.makeText(MainActivity.this, "Signed in successfully", Toast.LENGTH_SHORT).show();
                                Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
                                Common.current_user = user;
                                startActivity(homeIntent);
                                finish();
                            }



                        } else {
                            Toast.makeText(MainActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();

                        }
                    }else {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            Toast.makeText(MainActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

    }


}
