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
        ApiHelper.getClient().setKs("djJ8NDg3fFDcpSJjfsM4v3F8VKCt5MDXpUzJqbddZ5rvOC8WIzFZV3zf26pevfvb3iegP7LiiqmSizVr3XZIxj7odpOuHDqGhSYxL_Gz3lngeb6d04UG_QAbQcvPg1MMKc-aw6cHHFIJL5Anb3WRvwHu7BsYUqnRyZC66hYVtv2Slitn3GHTuRfhsmtBN1DnLRfGJenFvA==");
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
