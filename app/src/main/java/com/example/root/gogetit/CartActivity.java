package com.example.root.gogetit;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.example.root.gogetit.viewHolder.CartAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {

    //TODO: Add APIService to manifest


    private static final String TAG = "CartActivity";
    RecyclerView recyclerView;
    RecyclerView.ViewHolder viewHolder;
    LinearLayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    TextView textTotalPrice;
    FButton btnPlaceOrder;

    List<Order> carts = new ArrayList<>();
    CartAdapter adapter;

    APIService mService;
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

        final MaterialEditText edtAddress = order_address_comment.findViewById(R.id.edtAddress);
        final MaterialEditText edtComment = order_address_comment.findViewById(R.id.edtComment);


        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //create new request
                Request request = new Request(Common.current_user.getPhone(),
                        edtAddress.getText().toString(),
                        Common.current_user.getName(),
                        textTotalPrice.getText().toString(),
                        edtComment.getText().toString() ,
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
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
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
}
