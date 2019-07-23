package com.kaltura.kflow.presentation.productPrice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.kaltura.client.types.PpvPrice;
import com.kaltura.client.types.ProductPrice;
import com.kaltura.client.types.SubscriptionPrice;
import com.kaltura.kflow.R;

import java.util.ArrayList;

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
public class ProductPriceListAdapter extends RecyclerView.Adapter<ProductPriceListAdapter.MyViewHolder> {
    private ArrayList<ProductPrice> mProductPrices = new ArrayList<>();
    private ProductPriceListAdapter.OnProductPriceClickListener mListener;

    ProductPriceListAdapter(ArrayList<ProductPrice> assets, ProductPriceListAdapter.OnProductPriceClickListener listener) {
        mProductPrices.addAll(assets);
        mListener = listener;
    }

    @NonNull
    @Override
    public ProductPriceListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductPriceListAdapter.MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_price, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductPriceListAdapter.MyViewHolder holder, int position) {
        holder.bind(mProductPrices.get(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mProductPrices.size();
    }

    public interface OnProductPriceClickListener {
        void onSubscriptionPriceClicked(SubscriptionPrice subscriptionPrice);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView mType;
        private AppCompatTextView mPrice;

        MyViewHolder(View v) {
            super(v);
            mType = v.findViewById(R.id.product_price_type);
            mPrice = v.findViewById(R.id.price);
        }

        void bind(final ProductPrice productPrice, final ProductPriceListAdapter.OnProductPriceClickListener clickListener) {
            mType.setText("Product Type: " + productPrice.getProductType().getValue());
            mPrice.setText("Price: " + productPrice.getPrice().getCurrencySign() + productPrice.getPrice().getAmount());
            if (productPrice instanceof SubscriptionPrice) {
                itemView.setOnClickListener(view -> clickListener.onSubscriptionPriceClicked((SubscriptionPrice) productPrice));
            } else if (productPrice instanceof PpvPrice) {
                itemView.setOnClickListener(null);
            }
        }
    }
}
