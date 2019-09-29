package com.example.android.mysklad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView recyclerView;
    private ArrayList<Product> products;
    private ImageButton addButton;
    private ProductsRVadapter rVadapter;
    private DatabaseReference mydatabase;
    private ProgressBar progressBar;

    @Override
    protected void onStart() {
        super.onStart();
        updateDB();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(getString(R.string.all_products));

        mydatabase = FirebaseDatabase.getInstance().getReference();

        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        addButton = (ImageButton)findViewById(R.id.addButton);
        recyclerView = (RecyclerView)findViewById(R.id.products_list);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(llm);

        addButton.setOnClickListener(this);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy<0 && !addButton.isShown())
                    addButton.setVisibility(View.VISIBLE);
                else if(dy>0 && addButton.isShown())
                    addButton.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });





    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.addButton:
                startActivity(new Intent(MainActivity.this, NewProductActivity.class));
                break;
        }
    }

    private void updateDB(){
        mydatabase.child("products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    products = new ArrayList<>();
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                        if (snapshot.exists()){
                            Product product = snapshot.getValue(Product.class);
                            if (product!=null){
                                products.add(product);
                            }

                        }
                    }
                    findViewById(R.id.not_product_tv).setVisibility(View.GONE);
                    updateRV();
                    progressBar.setVisibility(View.GONE);

                }else {
                    findViewById(R.id.not_product_tv).setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void updateRV(){
        rVadapter = new ProductsRVadapter(products);
        recyclerView.setAdapter(rVadapter);
        rVadapter.setOnItemClickListener(new ProductsRVadapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product item) {
                Buffer.product = item;
                startActivity(new Intent(MainActivity.this, ProductDetailsActivity.class));
            }
        });

    }
}
