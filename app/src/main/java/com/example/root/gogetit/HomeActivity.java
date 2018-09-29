package com.example.root.gogetit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import com.example.root.gogetit.Interface.ItemClickListener;
import com.example.root.gogetit.common.Common;
import com.example.root.gogetit.model.Category;
import com.example.root.gogetit.model.Token;
import com.example.root.gogetit.viewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category;

    TextView txtFullName;

    RecyclerView recyclerView_menu;

    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);


        // init Firebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                Intent cartIntent = new Intent(HomeActivity.this, CartActivity.class);
                startActivity(cartIntent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        swipeRefreshLayout = findViewById(R.id.swipe_layout_home_activity);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category,Category.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category model) {
                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.imageView);
                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Toast.makeText(HomeActivity.this,"You clicked: "+ clickItem.getName(),Toast.LENGTH_SHORT).show();
                        Intent food_intent = new Intent(HomeActivity.this,FoodListActivity.class);
                        food_intent.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(food_intent);


                    }
                });
            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.menu_item,viewGroup,false);
                return new MenuViewHolder(itemView);


            }
        };
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.isConnectedToInternet(getBaseContext())){
                    loadMenu();
                }else
                    Toast.makeText(getBaseContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show();

            }
        });

        //default load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (Common.isConnectedToInternet(getBaseContext())){
                    loadMenu();
                }else
                    Toast.makeText(getBaseContext(), "Please check your internet connection", Toast.LENGTH_SHORT).show();

            }
        });

        //Set name for user
        View headerView = navigationView.getHeaderView(0);

        txtFullName = headerView.findViewById(R.id.nav_Name_textview);
        txtFullName.setText(Common.current_user.getName());

        //load menu
        recyclerView_menu= findViewById(R.id.recyclerview_menu);
        //recyclerView_menu.setHasFixedSize(true);
        recyclerView_menu.setLayoutManager(new GridLayoutManager(this,2));

        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView_menu.getContext(),
                R.anim.layout_fall_down);
        recyclerView_menu.setLayoutAnimation(controller);

        Paper.init(this);

//        if (Common.isConnectedToInternet(this)){
//            loadMenu();
//        }else
//            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();

        // update token for messaging on startup
        updateToken(FirebaseInstanceId.getInstance().getToken());
//
    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokensReference =  db.getReference("Tokens");

        Token data = new Token(token,false); //false because this token is from client

        tokensReference.child(Common.current_user.getPhone()).setValue(data);
    }

    private void loadMenu() {

        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category,Category.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category model) {
                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.imageView);
                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Toast.makeText(HomeActivity.this,"You clicked: "+ clickItem.getName(),Toast.LENGTH_SHORT).show();
                        Intent food_intent = new Intent(HomeActivity.this,FoodListActivity.class);
                        food_intent.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(food_intent);


                    }
                });
            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.menu_item,viewGroup,false);
                return new MenuViewHolder(itemView);


            }
        };

        adapter.startListening();
        recyclerView_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

        //animation
        recyclerView_menu.getAdapter()  .notifyDataSetChanged();
        recyclerView_menu.scheduleLayoutAnimation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        if (item.getItemId() == R.id.refresh)
            loadMenu();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            // Handle the camera action
        } else if (id == R.id.nav_cart) {

            Intent cartIntent = new Intent(HomeActivity.this,CartActivity.class);
            startActivity(cartIntent);

        } else if (id == R.id.nav_orders) {

            Intent orderIntent = new Intent(HomeActivity.this,OrderStatusActivity.class);
            startActivity(orderIntent);

        } else if (id == R.id.nav_view) {

        } else if (id == R.id.nav_log_out) {

            //delete remembered password and user
            Paper.book().destroy();


            //logout
            Intent signInIntent = new Intent(HomeActivity.this, MainActivity.class);
            signInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signInIntent);

        }else if (id == R.id.nav_change_password){
            showChangePasswordDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Change Password");
        alertDialog.setMessage("Please fill in all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_password = inflater.inflate(R.layout.change_password_layout,null);

        final TextInputEditText edtPassword = layout_password.findViewById(R.id.edtPasswordChangePassword);
        final TextInputEditText edtNewPassword = layout_password.findViewById(R.id.edtNewPasswordChangePassword);
        final TextInputEditText edtRepeatPassword = layout_password.findViewById(R.id.edtRepeatNewPasswordChangePassword);

        alertDialog.setView(layout_password);

        //set Buttons
        alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                final android.app.AlertDialog waitingDialog = new SpotsDialog(HomeActivity.this);
                waitingDialog.show();

                //check old password
                if (edtPassword.getText().toString().equals(Common.current_user.getPassword())){
                    //check new password and repeat password
                    if (edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString())){
                        Map<String,Object> passwordUpdate = new HashMap<>();
                        passwordUpdate.put("Password",edtNewPassword.getText().toString());//check database that key is correct

                        //make update
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("User");
                        userRef.child(Common.current_user.getPhone())
                                .updateChildren(passwordUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        waitingDialog.dismiss();
                                        Toast.makeText(HomeActivity.this, "Password Updated", Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }else {
                        waitingDialog.dismiss();
                        Toast.makeText(HomeActivity.this, "New Passwords don't match", Toast.LENGTH_SHORT).show();

                    }
                }else {
                    waitingDialog.dismiss();
                    Toast.makeText(HomeActivity.this, "Incorrect Old Password", Toast.LENGTH_SHORT).show();

                }
            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();


    }
}
