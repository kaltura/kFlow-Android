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
        ApiHelper.getClient().setKs("djJ8NDc4fOQ-OdvMVAn2rV1Y3g2brimefnJFj_oj6onZWU0aj_6fBEKFlSBf83GGxlP8C6FQm5AW0dKLiQPDdyGzBNLpu2wo6RT5HN8KUTF6R05pdvb8DFj5yE5AZcLyQrbRERTZhC_QtctqVSCK5zVuY-6dTrucCoCC8-kypTZUmEGDIk7H");
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
