   package com.example.root.gogetit;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.root.gogetit.common.Common;
import com.example.root.gogetit.model.Request;
import com.example.root.gogetit.viewHolder.FoodViewHolder;
import com.example.root.gogetit.viewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import static com.example.root.gogetit.common.Common.convertCodeToStatus;

   public class OrderStatusActivity extends AppCompatActivity {

    public RecyclerView recyclerView;
    public LinearLayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //init firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = findViewById(R.id.list_orders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        if (getIntent() == null)
            loadOrders(Common.current_user.getPhone());
        else
            loadOrders(getIntent().getStringExtra("userPhone"));
    }

       private void loadOrders(String phone) {
//           adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
//                   Request.class,
//                   R.layout.order_item_layout,
//                   OrderViewHolder.class,
//                   requests.orderByChild("phone").equalTo(phone)
//
//           ) {
//               @Override
//               protected void populateViewHolder(OrderViewHolder viewHolder, Request model, int position) {
//
//                   viewHolder.textOrderId.setText(adapter.getRef(position).getKey());
//                   viewHolder.textOrderPhone.setText(model.getPhone());
//                   viewHolder.textOrderStatus.setText(convertCodeToStatus(model.getStatus()));
//                   viewHolder.textOrderAddress.setText(model.getAddress());
//               }
//
//           };

           Query getOrderByUser = requests.orderByChild("phone").equalTo(phone);
           FirebaseRecyclerOptions<Request> orderOptions = new FirebaseRecyclerOptions.Builder<Request>()
                   .setQuery(getOrderByUser,Request.class)
                   .build();

           adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(orderOptions) {
               @Override
               protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, int position, @NonNull Request model) {
                   viewHolder.textOrderId.setText(adapter.getRef(position).getKey());
                   viewHolder.textOrderPhone.setText(model.getPhone());
                   viewHolder.textOrderStatus.setText(convertCodeToStatus(model.getStatus()));
                   viewHolder.textOrderAddress.setText(model.getAddress());
               }

               @NonNull
               @Override
               public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                   View itemView = LayoutInflater.from(viewGroup.getContext())
                           .inflate(R.layout.order_item_layout,viewGroup,false);

                   return new OrderViewHolder(itemView);
               }
           };
           adapter.startListening();
           recyclerView.setAdapter(adapter);
       }


       @Override
       protected void onStop() {
           super.onStop();
           adapter.stopListening();
       }
   }
