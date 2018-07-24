package com.example.root.gogetit;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.example.root.gogetit.Interface.ItemClickListener;
import com.example.root.gogetit.model.Food;
import com.example.root.gogetit.viewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FoodListActivity extends AppCompatActivity {

    RecyclerView food_recycler_view;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference food_list;

    String categoryId = "";

    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;


    //Searchbar functionality
    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //init firebase
        database = FirebaseDatabase.getInstance();
        food_list = database.getReference("Food");

        food_recycler_view = findViewById(R.id.recycler_food_list);
        food_recycler_view.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        food_recycler_view.setLayoutManager(layoutManager);

        //get posn
        if (getIntent() != null){
            categoryId = getIntent().getStringExtra("CategoryId");

        }
        if (!categoryId.isEmpty() && categoryId!= null){
            loadListFood(categoryId);
        }
        //Search
        materialSearchBar = findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter the food category");
        loadSuggest();
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                List<String> suggest = new ArrayList<>();
                for (String search: suggestList){
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase())){
                        suggest.add(search);

                    }
                    materialSearchBar.setLastSuggestions(suggest);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                // when searchbar is closed
                //restore original adapter

                if (!enabled)
                    food_recycler_view.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

                //when search is finished
                //show result of search adapter
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
    }

    private void startSearch(CharSequence text) {
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                food_list.orderByChild("Name").equalTo(text.toString())
        ) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {

                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.food_image);

                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Toast.makeText(FoodListActivity.this, "You clicked: "+ local.getName(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(FoodListActivity.this,FoodDetailActivity.class);
                        intent.putExtra("FoodId",searchAdapter.getRef(position).getKey());
                        startActivity(intent);
                    }
                });
            }
        };
        food_recycler_view.setAdapter(searchAdapter); //set Adapter for recycler view is search result
    }

    private void loadSuggest() {
        food_list.orderByChild("MenuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                            Food item = postSnapshot.getValue(Food.class);
                            suggestList.add(item.getName());//Add name of food to suggest list


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadListFood(String categoryId) {

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                food_list.orderByChild("MenuId").equalTo(categoryId) //Like select * from Food where MenuId = category Id
        ) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {

                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.food_image);

                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Toast.makeText(FoodListActivity.this, "You clicked: "+ local.getName(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(FoodListActivity.this,FoodDetailActivity.class);
                        intent.putExtra("FoodId",adapter.getRef(position).getKey());
                        startActivity(intent);
                    }
                });
            }
        };

        food_recycler_view.setAdapter(adapter);
    }
}
