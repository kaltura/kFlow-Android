package com.kaltura.kflow.presentation.assetList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kaltura.client.types.Asset;
import com.kaltura.client.types.ProgramAsset;
import com.kaltura.kflow.R;
import com.kaltura.kflow.utils.Utils;
import com.kaltura.playkit.providers.api.phoenix.APIDefines;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
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
        void onVodAssetClicked(Asset asset);

        void onProgramAssetClicked(Asset asset, APIDefines.PlaybackContextType contextType);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView mName;
        private AppCompatTextView mId;
        private AppCompatButton mPlayback;
        private AppCompatButton mStartover;
        private AppCompatButton mCatchup;

        MyViewHolder(View v) {
            super(v);
            mName = v.findViewById(R.id.asset_name);
            mId = v.findViewById(R.id.asset_id);
            mPlayback = v.findViewById(R.id.playback);
            mStartover = v.findViewById(R.id.startover);
            mCatchup = v.findViewById(R.id.catch_up);
        }

        void bind(final Asset asset, final AssetListAdapter.OnAssetClickListener clickListener) {
            StringBuilder title = new StringBuilder(asset.getName());
            if (asset instanceof ProgramAsset) {
                SimpleDateFormat format = new SimpleDateFormat("d MMM, HH:mm", Locale.US);
                Calendar startDayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                startDayCalendar.setTimeInMillis(asset.getStartDate() * 1000);
                Calendar endDayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                endDayCalendar.setTimeInMillis(asset.getEndDate() * 1000);
                title.append(" (")
                        .append(format.format(startDayCalendar.getTime()))
                        .append(" - ")
                        .append(format.format(endDayCalendar.getTime()))
                        .append(" UTC)");
            }
            mName.setText(title);
            mId.setText("Asset ID: " + asset.getId());

            if (asset instanceof ProgramAsset && Utils.isProgramInPast(asset)) {
                mPlayback.setVisibility(View.GONE);
                mStartover.setVisibility(View.GONE);
                mCatchup.setVisibility(View.VISIBLE);
            } else if (asset instanceof ProgramAsset && Utils.isProgramInLive(asset)) {
                mPlayback.setVisibility(View.VISIBLE);
                mStartover.setVisibility(View.VISIBLE);
                mCatchup.setVisibility(View.GONE);
            } else if (asset instanceof ProgramAsset && Utils.isProgramInFuture(asset)) {
                mPlayback.setVisibility(View.GONE);
                mStartover.setVisibility(View.GONE);
                mCatchup.setVisibility(View.GONE);
            } else {
                mPlayback.setVisibility(View.GONE);
                mStartover.setVisibility(View.VISIBLE);
                mCatchup.setVisibility(View.GONE);
            }

            mPlayback.setOnClickListener(view -> {
                if (asset instanceof ProgramAsset)
                    clickListener.onProgramAssetClicked(asset, APIDefines.PlaybackContextType.Playback);
                else clickListener.onVodAssetClicked(asset);
            });
            mStartover.setOnClickListener(view -> clickListener.onProgramAssetClicked(asset, APIDefines.PlaybackContextType.StartOver));
            mCatchup.setOnClickListener(view -> clickListener.onProgramAssetClicked(asset, APIDefines.PlaybackContextType.Catchup));
        }
    }
}
