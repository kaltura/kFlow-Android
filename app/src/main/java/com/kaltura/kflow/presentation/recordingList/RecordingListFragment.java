package com.kaltura.kflow.presentation.recordingList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kaltura.client.enums.RecordingStatus;
import com.kaltura.client.types.Recording;
import com.kaltura.kflow.R;
import com.kaltura.kflow.presentation.main.MainActivity;
import com.kaltura.kflow.presentation.player.PlayerFragment;

import java.util.ArrayList;

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
public class RecordingListFragment extends Fragment implements RecordingListAdapter.OnRecordingClickListener {

    public static final String ARG_RECORDINGS = "extra_recordings";

    public static RecordingListFragment newInstance(ArrayList<Recording> recordings) {
        RecordingListFragment recordingListFragment = new RecordingListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_RECORDINGS, recordings);
        recordingListFragment.setArguments(bundle);
        return recordingListFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recording_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Recording list");

        initList();
    }

    private void initList() {
        RecyclerView list = getView().findViewById(R.id.list);
        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));

        Bundle savedState = getArguments();
        ArrayList<Recording> recordings = savedState != null ? (ArrayList<Recording>) savedState.getSerializable(ARG_RECORDINGS) : null;

        RecordingListAdapter adapter = new RecordingListAdapter(recordings, this);
        list.setAdapter(adapter);
    }

    @Override
    public void onRecordingClicked(Recording recording) {
        if (recording.getStatus() == RecordingStatus.RECORDED) {
            PlayerFragment playerFragment = PlayerFragment.newInstance(recording);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, playerFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
