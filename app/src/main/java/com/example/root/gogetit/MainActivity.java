package com.example.root.gogetit;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import info.hoang8f.widget.FButton;


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


    }




}
