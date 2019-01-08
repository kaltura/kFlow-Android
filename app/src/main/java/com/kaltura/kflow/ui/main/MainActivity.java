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
        ApiHelper.getClient().setKs("djJ8NDg3fDFTlz_bE-LZIeWTHcW4y3WPjn9M2vt7nRh2PxpRvMHota2X8VjAJuEOg80t7F6WEsPEyzMr8SogvOXs3sh-axJG9qQ19Qq6zuiuk6KAjw3JrUGSX7vrnXXU4Tr-N-J7oSoPQveaMG_RBOAfpQlEW1vl4ABTCXW6NmIdTMu8BSruERW_o0qz6rwlD_N_EQF8tQ==");
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
