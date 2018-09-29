package com.example.root.gogetit;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telecom.ConnectionRequest;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.root.gogetit.common.Common;
import com.example.root.gogetit.database.Database;
import com.example.root.gogetit.model.MyResponse;
import com.example.root.gogetit.model.Notifications;
import com.example.root.gogetit.model.Order;
import com.example.root.gogetit.model.Request;
import com.example.root.gogetit.model.Sender;
import com.example.root.gogetit.model.Token;
import com.example.root.gogetit.ramote.APIService;
import com.example.root.gogetit.ramote.IGoogleService;
import com.example.root.gogetit.viewHolder.CartAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener, LocationListener{

    //TODO: Add APIService to manifest


    private static final String TAG = "CartActivity";
    private static final int LOCATION_REQUEST_CODE = 9999;
    private static final int PLAY_SERVICE_REQUEST = 9997;
    RecyclerView recyclerView;
    RecyclerView.ViewHolder viewHolder;
    LinearLayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView textTotalPrice;
    FButton btnPlaceOrder;

    List<Order> carts = new ArrayList<>();
    CartAdapter adapter;

    APIService mService;

    Place shipAddress;

    //Location
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    private static final int UPDATE_INTERVAL=5000;
    private static final int FASTEST_INTERVAL=3000;
    private static final int SMALLEST_DISPLACEMENT=10;

    //google map api Retrofit
    IGoogleService googleMapService; //init in oncreate

    String address;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);


        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //init views
        recyclerView = findViewById(R.id.list_cart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        textTotalPrice= findViewById(R.id.totalPrice);
        btnPlaceOrder = findViewById(R.id.btn_place_order);


        //init Api Service

        mService = Common.getFCMService();

        //init map
        googleMapService = Common.getGoogleMapApi();

        //Runtime Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED  &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            },LOCATION_REQUEST_CODE);
        }else {
            if (checkServices()){//if play service in on device
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (carts.size() >0)
                    showAlertDialog();
                else
                    Toast.makeText(CartActivity.this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            }
        });

        loadListCart();
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
            {
                if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    if (checkServices()){//if play service in on device
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
            }
            break;

        }
    }

    private synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    private boolean checkServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICE_REQUEST).show();
            else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void showAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(CartActivity.this);
        alertDialog.setTitle("One more step");
        alertDialog.setMessage("Enter your address: ");

//        final EditText edtAdress = new EditText(CartActivity.this);
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT
//        );
//        edtAdress.setLayoutParams(layoutParams);
//        alertDialog.setView(edtAdress);

        LayoutInflater inflater = this.getLayoutInflater();

        View order_address_comment = inflater.inflate(R.layout.order_address_comment,null);

        //final MaterialEditText edtAddress = order_address_comment.findViewById(R.id.edtAddress);
        final PlaceAutocompleteFragment edtAddress = (PlaceAutocompleteFragment)getFragmentManager().
                findFragmentById(R.id.place_autocomplete_fragmet);
        //hide search icon before entering text
        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
        //set hint for autocomplete
        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setHint("Enter your address");
        //set text size
        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                .setTextSize(14);

        //get address for places autocomplete
        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                shipAddress = place;
            }

            @Override
            public void onError(Status status) {
                Log.e(TAG, "onError: "+status.getStatusMessage().toString());
            }
        });

        final MaterialEditText edtComment = order_address_comment.findViewById(R.id.edtComment);

        //Radio
        final RadioButton radioBtnShipToAddress = order_address_comment.findViewById(R.id.radioShipToAddress);
        final RadioButton radioBtnShipToHome = order_address_comment.findViewById(R.id.radioHomeAddress);

        //Event Radio
        radioBtnShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //ship to this adress features
                if (b){

                    googleMapService.getAddress(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=false",
                            lastLocation.getLatitude(),lastLocation.getLongitude())).
                            enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {

                                    //if api is ok
                                    //get result address
                                    try {
                                        JSONObject jsonObject = new JSONObject(response.body().toString());

                                        JSONArray resultsArray = jsonObject.getJSONArray("results");

                                        JSONObject firstObject = resultsArray.getJSONObject(0);

                                        address = firstObject.getString("formatted_address");

                                        //set this address to edtAddress
                                        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                                .setText(address);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {

                                    Toast.makeText(CartActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });




        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //add check condition here
                //if user selects address from place fragment,use it
                //if user select ship to this address, get address from location and use it
                //if user selects home address , get address from profile and use it
                if (!radioBtnShipToAddress.isChecked() && !radioBtnShipToHome.isChecked()){
                    //if both radio are not selected
                    if (shipAddress != null)
                        address = shipAddress.getAddress().toString();
                    else
                        Toast.makeText(CartActivity.this, "Please enter address or select one of the options", Toast.LENGTH_SHORT).show();
                        //fix crash fragment
                        getFragmentManager().beginTransaction()
                                .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragmet))
                                .commit();
                        return;
                }
                if (!TextUtils.isEmpty(address)){
                    Toast.makeText(CartActivity.this, "Please enter address", Toast.LENGTH_SHORT).show();
                    //fix crash fragment
                    getFragmentManager().beginTransaction()
                            .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragmet))
                            .commit();
                    return;
                }

                //create new request
                Request request = new Request(Common.current_user.getPhone(),
                        address,
                        Common.current_user.getName(),
                        textTotalPrice.getText().toString(),
                        edtComment.getText().toString(),
                        String.format("%s,%s",shipAddress.getLatLng().latitude,
                                shipAddress.getLatLng().longitude),
                        carts,
                        "0");
                //submit to firebase
                //use System.getCurrentMilli to key


                String order_number = String.valueOf(System.currentTimeMillis());
                requests.child(order_number)
                        .setValue(request);

                //Delete cart
                new Database(getBaseContext()).cleanCart();

                //send the notification when order is placed
                sendNotificationOrder(order_number);
//                Toast.makeText(CartActivity.this, "Thank you, Your order has been placed", Toast.LENGTH_SHORT).show();
//                finish();

                //remove fragmen
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragmet))
                        .commit();
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                //remove fragmen
                getFragmentManager().beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragmet))
                        .commit();
            }
        });

        alertDialog.show();


    }




    private void sendNotificationOrder(final String order_number) {
        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("Tokens");
        final Query data = tokenRef.orderByChild("isServerToken").equalTo(true);//get all nodes with isServerToken is true

        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    Token serverToken = postSnapshot.getValue( Token.class);

                    //create raw payload to send
                    Notifications notifications = new Notifications("You have a new order "+order_number,"Atuma Notify");
                    Sender content = new Sender(serverToken.getToken(),notifications);

                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    if (response.code() == 200){//to prevent from crashing
                                        if (response.body().success == 1){
                                            Toast.makeText(CartActivity.this, "Thank you, Your order has been placed", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                        else{
                                            Toast.makeText(CartActivity.this, "Order Failed!", Toast.LENGTH_SHORT).show();

                                        }
                                    }


                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                    Log.e(TAG, "onFailure: "+ t.getMessage() );
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadListCart() {

        carts = new Database(this).getCarts();
        adapter = new CartAdapter(carts,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //calculate total price
        int total = 0;
        for (Order order: carts){
            total += (Integer.parseInt(order.getPrice()) * Integer.parseInt(order.getQuantity()));

        }

        Locale locale = new Locale("en","US");
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);

        textTotalPrice.setText(numberFormat.format(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE)){
            deleteCart(item.getOrder());
        }
        return true;
    }

    private void deleteCart(int position) {
        // we need to remove item at List<> order by position
        carts.remove(position);

        //After that we need to delete all old data from SQLite
        new Database(this).cleanCart();

        //now update new data from List<Order> to SQLite
        for (Order item: carts){
            new Database(this).addToCart(item);
        }

        //refresh list
        loadListCart();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED  &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED){
            return;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null){
            Log.d(TAG, "displayLocation: Your Location: "+lastLocation.getLatitude() +"," +
                        lastLocation.getLongitude());
        }else {
            Log.d(TAG, "displayLocation: Could not get your location");
        }
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED  &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED){
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,
                this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;
        displayLocation();
    }
}
