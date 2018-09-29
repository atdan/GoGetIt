package com.example.root.gogetit.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.root.gogetit.model.User;
import com.example.root.gogetit.ramote.APIService;
import com.example.root.gogetit.ramote.IGoogleService;
import com.example.root.gogetit.ramote.RetrofitClient;

public class Common {
    public static User current_user;

    public static String PHONE_TEXT = "userPhone";

    public static final String BASE_URL = "https://fcm.googleapis.com/";

    public static final String GOOGLE_API_URL = "https://maps.googleapis.com/";


    public static APIService getFCMService(){
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
    public static IGoogleService getGoogleMapApi(){
        return RetrofitClient.getGoogleApiClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    public static String convertCodeToStatus(String status) {
        if (status.equals("0")){
            return "Order Placed";
        }else if (status.equals("1")){
            return "On my way!";
        }else return "shipped";
    }

    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null){
            NetworkInfo[] infos = connectivityManager.getAllNetworkInfo();

            if (infos != null){
                for (int i = 0; i< infos.length; i++){
                    if (infos[i].getState() == NetworkInfo.State.CONNECTED){
                        return  true;
                    }
                }
            }
        }
        return false;
    }

    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PASSWORD_KEY = "Password";

}
