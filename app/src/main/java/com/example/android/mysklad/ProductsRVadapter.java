package com.example.android.mysklad;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductsRVadapter extends RecyclerView.Adapter<ProductsRVadapter.CustomViewHolder> {
    private ArrayList<Product> products;
    private OnItemClickListener onItemClickListener;

    public ProductsRVadapter(ArrayList products){
        this.products = products;

    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_product, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CustomViewHolder holder, final int position) {
        final Product product = products.get(position);
        String price = product.getPrice()+"";

        holder.titleProduct.setText(product.getTitle());
        holder.priceProduct.setText(price);
        holder.progressBar.setVisibility(View.VISIBLE);
        holder.imageProduct.setVisibility(View.INVISIBLE);
        Picasso.get().load(product.getPhotoUrl()).
                into(holder.imageProduct, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.imageProduct.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(products.get(position));
            }
        };


        holder.layout.setOnClickListener(listener);


    }

    @Override
    public int getItemCount() {
        return (null != products ? products.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imageProduct;
        protected TextView titleProduct, priceProduct;
        protected RelativeLayout layout;
        protected ProgressBar progressBar;

        public CustomViewHolder(View view) {
            super(view);
            this.layout= (RelativeLayout) view.findViewById(R.id.product_container);
            this.imageProduct = (ImageView) view.findViewById(R.id.image);
            this.titleProduct = (TextView) view.findViewById(R.id.title_tv);
            this.priceProduct = (TextView) view.findViewById(R.id.price_tv);
            this.progressBar = (ProgressBar)view.findViewById(R.id.progress_circular);

        }
    }
    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener {
        void onItemClick(Product item);
    }

}
