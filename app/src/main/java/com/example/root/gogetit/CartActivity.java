package com.example.root.gogetit;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.root.gogetit.common.Common;
import com.example.root.gogetit.database.Database;
import com.example.root.gogetit.model.Order;
import com.example.root.gogetit.model.Request;
import com.example.root.gogetit.viewHolder.CartAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import info.hoang8f.widget.FButton;

public class CartActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.ViewHolder viewHolder;
    LinearLayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    TextView textTotalPrice;
    FButton btnPlaceOrder;

    List<Order> carts = new ArrayList<>();
    CartAdapter adapter;
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

        btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showAlertDialog();
            }
        });

        loadListCart();
    }

    private void showAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(CartActivity.this);
        alertDialog.setTitle("One more step");
        alertDialog.setMessage("Enter your address: ");

        final EditText edtAdress = new EditText(CartActivity.this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        edtAdress.setLayoutParams(layoutParams);
        alertDialog.setView(edtAdress);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //create new request
                Request request = new Request(Common.current_user.getPhone(),
                        edtAdress.getText().toString(),
                        Common.current_user.getName(),
                        textTotalPrice.getText().toString(),
                        carts);
                //submit to firebase
                //use System.getCurrentMilli to key

                requests.child(String.valueOf(System.currentTimeMillis()))
                        .setValue(request);

                //Delete cart
                new Database(getBaseContext()).cleanCart();
                Toast.makeText(CartActivity.this, "Thank you, Your order has been placed", Toast.LENGTH_SHORT).show();
                finish();
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

    private void loadListCart() {

        carts = new Database(this).getCarts();
        adapter = new CartAdapter(carts,this);
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
}
