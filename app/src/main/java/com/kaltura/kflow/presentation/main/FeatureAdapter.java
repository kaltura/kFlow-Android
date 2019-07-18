package com.kaltura.kflow.presentation.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kaltura.kflow.R;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by alex_lytvynenko on 11/16/18.
 */
public class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.MyViewHolder> {
    private Feature[] mFeatures;
    private OnFeatureClickListener mListener;

    FeatureAdapter(Feature[] features, OnFeatureClickListener listener) {
        mFeatures = features;
        mListener = listener;
    }

    @NonNull
    @Override
    public FeatureAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feature, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(mFeatures[position], mListener);
    }

    @Override
    public int getItemCount() {
        return mFeatures.length;
    }

    public interface OnFeatureClickListener {
        void onFeatureClicked(Feature feature);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView mTextView;

        MyViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.feature_text);
        }

        void bind(final Feature feature, final OnFeatureClickListener clickListener) {
            mTextView.setText(feature.getText());
            mTextView.setOnClickListener(view -> clickListener.onFeatureClicked(feature));
        }
    }
}
