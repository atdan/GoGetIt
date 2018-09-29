package com.example.root.gogetit;

import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.root.gogetit.common.Common;
import com.example.root.gogetit.database.Database;
import com.example.root.gogetit.model.Food;
import com.example.root.gogetit.model.Order;
import com.example.root.gogetit.model.Rating;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;

public class FoodDetailActivity extends AppCompatActivity implements RatingDialogListener{

    TextView foodName, foodDescription, foodPrice;
    ImageView foodImage;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart, btnRating;
    ElegantNumberButton numberButton;

    RatingBar rating_bar;

    String foodId= "";

    Food current_food;

    FirebaseDatabase database;
    DatabaseReference foods;

    DatabaseReference ratingsTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);


        //init Firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Food");
        ratingsTable = database.getReference("Rating");

        //init views
        numberButton = findViewById(R.id.number_button);
        btnCart = findViewById(R.id.btn_cart);

        foodDescription = findViewById(R.id.food_description);
        foodName = findViewById(R.id.food_name);
        foodPrice = findViewById(R.id.food_price);
        foodImage = findViewById(R.id.img_food);
        collapsingToolbarLayout = findViewById(R.id.collapsing);

        btnRating = findViewById(R.id.btn_rating);
        rating_bar = findViewById(R.id.rating_bar);


        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //get food id
        if (getIntent() != null)
            foodId = getIntent().getStringExtra("FoodId");
        if (!foodId.isEmpty() || foodId!= null){
            if (Common.isConnectedToInternet(this)){
                getDetailFood(foodId);
                getRatingFood(foodId);
            }else {
                Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

        }


        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRatingDialog();
            }
        });


        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        current_food.getName(),
                        numberButton.getNumber(),
                        current_food.getPrice(),
                        current_food.getDiscount(),
                        current_food.getImage()
                ));
                Toast.makeText(FoodDetailActivity.this, "Added to cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getRatingFood(String foodId) {

        Query foodRating = ratingsTable.orderByChild(foodId).equalTo(foodId);

        foodRating.addValueEventListener(new ValueEventListener() {
            int count = 0, sum = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postsnapshot: dataSnapshot.getChildren()){
                    Rating item = postsnapshot.getValue(Rating.class);
                    assert item != null;
                    sum += Integer.parseInt(item.getRateValue());
                    count++;
                }

                if (count != 0){
                    float average = sum/count;
                    rating_bar.setRating(average);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very bad","Not Good",
                        "Quite Ok", "Very Good","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Please select some stars and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .create(FoodDetailActivity.this)
                .show();
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

    @Override
    public void onPositiveButtonClicked(int value, String comments) {
        //TODO: Get rating and upload to firebase
        final Rating rating = new Rating(Common.current_user.getPhone(),foodId,
                String.valueOf(value),
                comments);

        ratingsTable.child(Common.current_user.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(Common.current_user.getPhone()).exists()){
                    //remove old value
                    ratingsTable.child(Common.current_user.getPhone()).removeValue();

                    //add new comment
                    ratingsTable.child(Common.current_user.getPhone()).setValue(rating);
                }else
                    //add new comment
                    ratingsTable.child(Common.current_user.getPhone()).setValue(rating);

                Toast.makeText(FoodDetailActivity.this, "Thank you for your feedback", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }
}
