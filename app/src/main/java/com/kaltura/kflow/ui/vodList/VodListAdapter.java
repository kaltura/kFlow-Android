package com.kaltura.kflow.ui.vodList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kaltura.client.types.Asset;
import com.kaltura.kflow.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
public class VodListAdapter extends RecyclerView.Adapter<VodListAdapter.MyViewHolder> {
    private ArrayList<Asset> mAssets = new ArrayList<>();
    private VodListAdapter.OnAssetClickListener mListener;

    VodListAdapter(ArrayList<Asset> assets, VodListAdapter.OnAssetClickListener listener) {
        mAssets.addAll(assets);
        mListener = listener;
    }

    @NonNull
    @Override
    public VodListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VodListAdapter.MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asset, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VodListAdapter.MyViewHolder holder, int position) {
        holder.bind(mAssets.get(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mAssets.size();
    }

    public interface OnAssetClickListener {
        void onAssetClicked(Asset asset);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView mName;
        private AppCompatTextView mId;
        private View mContainer;

        MyViewHolder(View v) {
            super(v);
            mContainer = v.findViewById(R.id.asset_container);
            mName = v.findViewById(R.id.asset_name);
            mId = v.findViewById(R.id.asset_id);
        }

        void bind(final Asset asset, final VodListAdapter.OnAssetClickListener clickListener) {
            mName.setText(asset.getName());
            mId.setText("Asset ID: " + asset.getId());
            mContainer.setOnClickListener(view -> clickListener.onAssetClicked(asset));
        }
    }
}
