package com.example.root.gogetit;

import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.root.gogetit.database.Database;
import com.example.root.gogetit.model.Food;
import com.example.root.gogetit.model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class FoodDetailActivity extends AppCompatActivity {

    TextView foodName, foodDescription, foodPrice;
    ImageView foodImage;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart;
    ElegantNumberButton numberButton;

    String foodId= "";

    Food current_food;

    FirebaseDatabase database;
    DatabaseReference foods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);


        //init Firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Food");

        //init views
        numberButton = findViewById(R.id.number_button);
        btnCart = findViewById(R.id.btn_cart);

        foodDescription = findViewById(R.id.food_description);
        foodName = findViewById(R.id.food_name);
        foodPrice = findViewById(R.id.food_price);
        foodImage = findViewById(R.id.img_food);
        collapsingToolbarLayout = findViewById(R.id.collapsing);


        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //get food id
        if (getIntent() != null)
            foodId = getIntent().getStringExtra("FoodId");
        if (!foodId.isEmpty() || foodId!= null)
            getDetailFood(foodId);

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        current_food.getName(),
                        numberButton.getNumber(),
                        current_food.getPrice(),
                        current_food.getDiscount()
                ));
                Toast.makeText(FoodDetailActivity.this, "Added to cart", Toast.LENGTH_SHORT).show();
            }
        });
    }
    

    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 current_food = dataSnapshot.getValue(Food.class);

                //set Image
                Picasso.with(getBaseContext()).load(current_food.getImage())
                        .into(foodImage);

                foodPrice.setText(current_food.getPrice());
                foodName.setText(current_food.getName());
                foodDescription.setText(current_food.getDescription());
                collapsingToolbarLayout.setTitle(current_food.getName());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
