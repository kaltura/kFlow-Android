package com.kaltura.kflow.presentation.transactionHistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.kaltura.client.types.BillingTransaction;
import com.kaltura.kflow.R;

import java.util.ArrayList;

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
public class TransactionHistoryListAdapter extends RecyclerView.Adapter<TransactionHistoryListAdapter.MyViewHolder> {
    private ArrayList<BillingTransaction> mTransactions = new ArrayList<>();

    TransactionHistoryListAdapter(ArrayList<BillingTransaction> transactions) {
        mTransactions.addAll(transactions);
    }

    @NonNull
    @Override
    public TransactionHistoryListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransactionHistoryListAdapter.MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_price, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionHistoryListAdapter.MyViewHolder holder, int position) {
        holder.bind(mTransactions.get(position));
    }

    @Override
    public int getItemCount() {
        return mTransactions.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView mType;
        private AppCompatTextView mPrice;

        MyViewHolder(View v) {
            super(v);
            mType = v.findViewById(R.id.product_price_type);
            mPrice = v.findViewById(R.id.price);
        }

        void bind(final BillingTransaction billingTransaction) {
//            mType.setText("Product Type: " + productPrice.getProductType().getValue());
//            mPrice.setText("Price: " + productPrice.getPrice().getCurrencySign() + productPrice.getPrice().getAmount());
        }
    }
}
