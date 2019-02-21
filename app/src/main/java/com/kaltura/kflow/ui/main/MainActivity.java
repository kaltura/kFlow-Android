package com.kaltura.kflow.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.kaltura.kflow.R;
import com.kaltura.kflow.utils.ApiHelper;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new MainFragment()).commit();
        }

        // hardcode KS
        ApiHelper.getClient().setKs("djJ8NDc4fAZJAdWQSR7yYW2X7K0Jvqk0MKkHNHngtalT2WA5n5ZYSBXaDxyc9_6gmMl3wkqn4uU2-wU_rTb4s79rKLL0lZ67lIvPx9gApmy_IDFS8a7kvbF03ZY0B7a2SOj8HnQiiQqIl_lxTp7AX3ZdM1s5uTDnUpMtZlba8Zy1wItmTgWtE-v51s8jZrQNPza5QSlq8xURcnR5S97rfIeuX4zvP9wlJFfZGX6osak8iQ4xD__2xcD-ZptPV-UlxENhL3TZOMtZujzHqX4zNnN6Tne1hr8=");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackStackChanged() {
        boolean isRoot = getSupportFragmentManager().getBackStackEntryCount() == 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(!isRoot);
        if (isRoot) getSupportActionBar().setTitle(R.string.app_name);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
    }
}
