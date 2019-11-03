package com.kaltura.kflow.presentation.assetList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kaltura.client.types.Asset;
import com.kaltura.client.types.ProgramAsset;
import com.kaltura.kflow.R;
import com.kaltura.kflow.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
public class AssetListAdapter extends RecyclerView.Adapter<AssetListAdapter.MyViewHolder> {
    private ArrayList<Asset> mAssets = new ArrayList<>();
    private AssetListAdapter.OnAssetClickListener mListener;

    AssetListAdapter(ArrayList<Asset> assets, AssetListAdapter.OnAssetClickListener listener) {
        mAssets.addAll(assets);
        mListener = listener;
    }

    @NonNull
    @Override
    public AssetListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AssetListAdapter.MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asset, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AssetListAdapter.MyViewHolder holder, int position) {
        holder.bind(mAssets.get(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mAssets.size();
    }

    public interface OnAssetClickListener {
        void onAssetClicked(Asset asset);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView mName;
        private AppCompatTextView mId;
        private View mContainer;

        MyViewHolder(View v) {
            super(v);
            mContainer = v.findViewById(R.id.asset_container);
            mName = v.findViewById(R.id.asset_name);
            mId = v.findViewById(R.id.asset_id);
        }

        void bind(final Asset asset, final AssetListAdapter.OnAssetClickListener clickListener) {
            StringBuilder title = new StringBuilder(asset.getName());
            if (asset instanceof ProgramAsset) {
                SimpleDateFormat format = new SimpleDateFormat("d MMM, HH:mm", Locale.US);
                Calendar startDayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                startDayCalendar.setTimeInMillis(Utils.utcToLocal(asset.getStartDate() * 1000));
                Calendar endDayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                endDayCalendar.setTimeInMillis(Utils.utcToLocal(asset.getEndDate() * 1000));
                title.append(" (")
                        .append(format.format(startDayCalendar.getTime()))
                        .append(" - ")
                        .append(format.format(endDayCalendar.getTime()))
                        .append(")");
            }
            mName.setText(title);
            mId.setText("Asset ID: " + asset.getId());
            mContainer.setOnClickListener(view -> clickListener.onAssetClicked(asset));
        }
    }
}
