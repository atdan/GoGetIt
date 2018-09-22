package com.example.root.gogetit.service;

import com.example.root.gogetit.common.Common;
import com.example.root.gogetit.model.Token;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService{

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String tokenRefreshId = FirebaseInstanceId.getInstance().getToken();

        if (Common.current_user != null)
            updateTokenToFirebase(tokenRefreshId);


    }

    private void updateTokenToFirebase(String tokenRefreshId) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokensReference =  db.getReference("Tokens");

        Token token = new Token(tokenRefreshId,false); //false because this token is from client

        tokensReference.child(Common.current_user.getPhone()).setValue(token);



    }
}
