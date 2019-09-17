package com.kaltura.kflow.presentation.recordingList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.kaltura.client.types.Recording;
import com.kaltura.kflow.R;

import java.util.ArrayList;

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
public class RecordingListAdapter extends RecyclerView.Adapter<RecordingListAdapter.MyViewHolder> {
    private ArrayList<Recording> mRecordings = new ArrayList<>();
    private RecordingListAdapter.OnRecordingClickListener mListener;

    RecordingListAdapter(ArrayList<Recording> recordings, RecordingListAdapter.OnRecordingClickListener listener) {
        mRecordings.addAll(recordings);
        mListener = listener;
    }

    @NonNull
    @Override
    public RecordingListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecordingListAdapter.MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recording, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecordingListAdapter.MyViewHolder holder, int position) {
        holder.bind(mRecordings.get(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mRecordings.size();
    }

    public interface OnRecordingClickListener {
        void onRecordingClicked(Recording recording);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView mStatus;
        private AppCompatTextView mId;
        private View mContainer;

        MyViewHolder(View v) {
            super(v);
            mContainer = v.findViewById(R.id.recording_container);
            mStatus = v.findViewById(R.id.recording_status);
            mId = v.findViewById(R.id.recording_id);
        }

        void bind(final Recording recording, final RecordingListAdapter.OnRecordingClickListener clickListener) {
            mStatus.setText(recording.getStatus().getValue());
            mId.setText("Asset ID: " + recording.getAssetId());
            mContainer.setOnClickListener(view -> clickListener.onRecordingClicked(recording));
        }
    }
}
